#!/usr/bin/env python3
"""Export controller-derived OpenAPI and Postman artifacts from api-docs-service."""

import argparse
import base64
import json
import os
from pathlib import Path
from urllib.parse import quote, urljoin
from urllib.request import Request, urlopen


HTTP_METHODS = ("get", "post", "put", "patch", "delete", "head", "options", "trace")


def request_json(url, username=None, password=None, bearer_token=None):
    request = Request(url, headers={"Accept": "application/json"})
    if bearer_token:
        request.add_header("Authorization", f"Bearer {bearer_token}")
    elif username:
        encoded = base64.b64encode(f"{username}:{password or ''}".encode()).decode()
        request.add_header("Authorization", f"Basic {encoded}")
    with urlopen(request, timeout=30) as response:
        return json.load(response)


def service_variable(service_key):
    return service_key.replace("-", "_") + "_base_url"


def auth_for(operation, service_key=None):
    platform_auth = operation.get("x-platform-auth")
    security = operation.get("security")
    if platform_auth == "NONE" or security == []:
        return {"type": "noauth"}
    schemes = {
        name
        for requirement in security or []
        for name in requirement
    }
    if platform_auth == "BASIC" or "basicAuth" in schemes:
        prefix = (service_key or "service").replace("-", "_")
        return {
            "type": "basic",
            "basic": [
                {
                    "key": "username",
                    "value": "{{" + prefix + "_internal_username}}",
                    "type": "string",
                },
                {
                    "key": "password",
                    "value": "{{" + prefix + "_internal_password}}",
                    "type": "string",
                },
            ],
        }
    return {
        "type": "bearer",
        "bearer": [{"key": "token", "value": "{{access_token}}", "type": "string"}],
    }


def sample_from_schema(schema, components, seen=None):
    if not isinstance(schema, dict):
        return {}
    seen = set() if seen is None else set(seen)
    ref = schema.get("$ref")
    if ref:
        name = ref.rsplit("/", 1)[-1]
        if name in seen:
            return {}
        seen.add(name)
        return sample_from_schema(components.get(name, {}), components, seen)
    if "example" in schema:
        return schema["example"]
    if "default" in schema:
        return schema["default"]
    enum = schema.get("enum")
    if enum:
        return enum[0]
    schema_type = schema.get("type")
    if schema_type == "array":
        return [sample_from_schema(schema.get("items", {}), components, seen)]
    if schema_type == "object" or "properties" in schema:
        return {
            name: sample_from_schema(value, components, seen)
            for name, value in schema.get("properties", {}).items()
        }
    if schema_type in ("integer", "number"):
        return schema.get("minimum", 0)
    if schema_type == "boolean":
        return False
    return "string"


def postman_request(service_key, path, method, operation, spec):
    variables = []
    raw_path = path
    for parameter in operation.get("parameters", []):
        location = parameter.get("in")
        name = parameter.get("name")
        if not name:
            continue
        if location == "path":
            raw_path = raw_path.replace("{" + name + "}", "{{" + name + "}}")
            variables.append({"key": name, "value": parameter.get("example", "")})
    query = []
    headers = []
    for parameter in operation.get("parameters", []):
        name = parameter.get("name")
        schema = parameter.get("schema", {})
        value = parameter.get("example", schema.get("default", ""))
        if parameter.get("in") == "query":
            query.append({"key": name, "value": str(value), "disabled": not parameter.get("required", False)})
        elif parameter.get("in") == "header":
            headers.append({"key": name, "value": "{{" + name.lower().replace("-", "_") + "}}"})
    request = {
        "method": method.upper(),
        "header": headers,
        "auth": auth_for(operation, service_key),
        "url": {
            "raw": "{{" + service_variable(service_key) + "}}" + raw_path,
            "host": ["{{" + service_variable(service_key) + "}}"],
            "path": [part for part in raw_path.strip("/").split("/") if part],
            "query": query,
            "variable": variables,
        },
        "description": operation.get("description", operation.get("summary", "")),
    }
    content = operation.get("requestBody", {}).get("content", {})
    media = content.get("application/json")
    if media:
        components = spec.get("components", {}).get("schemas", {})
        request["header"].append({"key": "Content-Type", "value": "application/json"})
        request["body"] = {
            "mode": "raw",
            "raw": json.dumps(
                sample_from_schema(media.get("schema", {}), components),
                indent=2,
            ),
            "options": {"raw": {"language": "json"}},
        }
    return {
        "name": operation.get("summary") or operation.get("operationId") or f"{method.upper()} {path}",
        "request": request,
    }


def build_postman_collection(documents, service_summaries):
    summary_by_key = {item["serviceKey"]: item for item in service_summaries}
    folders = []
    variables = [
        {"key": "access_token", "value": "", "type": "secret"},
        {"key": "x_tenant_key", "value": "demo-tenant", "type": "default"},
        {"key": "x_site_key", "value": "main-site", "type": "default"},
    ]
    for service_key, spec in sorted(documents.items()):
        summary = summary_by_key.get(service_key, {})
        variables.append({
            "key": service_variable(service_key),
            "value": summary.get("baseUrl", ""),
            "type": "default",
        })
        prefix = service_key.replace("-", "_")
        variables.extend([
            {
                "key": prefix + "_internal_username",
                "value": "",
                "type": "default",
            },
            {
                "key": prefix + "_internal_password",
                "value": "",
                "type": "secret",
            },
        ])
        requests = []
        for path, path_item in sorted(spec.get("paths", {}).items()):
            inherited = path_item.get("parameters", [])
            for method in HTTP_METHODS:
                operation = path_item.get(method)
                if not isinstance(operation, dict):
                    continue
                operation = dict(operation)
                operation["parameters"] = inherited + operation.get("parameters", [])
                requests.append(postman_request(service_key, path, method, operation, spec))
        folders.append({"name": service_key, "item": requests})
    return {
        "info": {
            "name": "Cyan Business Platform — Live Controller APIs",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
            "description": "Generated from live Spring controller OpenAPI; do not edit manually.",
        },
        "variable": variables,
        "item": folders,
    }


def export(catalog_url, output_dir, username=None, password=None, bearer_token=None, refresh=False):
    base = catalog_url.rstrip("/") + "/"
    query = "?refresh=true" if refresh else ""
    summaries = request_json(urljoin(base, "services"), username, password, bearer_token)
    available = [item for item in summaries if item.get("status") == "AVAILABLE"]
    documents = {}
    services_dir = output_dir / "openapi" / "services"
    services_dir.mkdir(parents=True, exist_ok=True)
    for summary in available:
        service_key = summary["serviceKey"]
        document = request_json(
            urljoin(base, f"services/{quote(service_key, safe='')}{query}"),
            username,
            password,
            bearer_token,
        )
        documents[service_key] = document
        (services_dir / f"{service_key}.openapi.json").write_text(
            json.dumps(document, indent=2) + "\n",
            encoding="utf-8",
        )
    aggregate = request_json(
        urljoin(base, f"aggregate{query}"),
        username,
        password,
        bearer_token,
    )
    (output_dir / "openapi").mkdir(parents=True, exist_ok=True)
    (output_dir / "openapi" / "platform.openapi.json").write_text(
        json.dumps(aggregate, indent=2) + "\n",
        encoding="utf-8",
    )
    (output_dir / "service-catalog.json").write_text(
        json.dumps(summaries, indent=2) + "\n",
        encoding="utf-8",
    )
    postman_dir = output_dir / "postman"
    postman_dir.mkdir(parents=True, exist_ok=True)
    collection = build_postman_collection(documents, summaries)
    (postman_dir / "cyan-business-platform.live.postman_collection.json").write_text(
        json.dumps(collection, indent=2) + "\n",
        encoding="utf-8",
    )
    return len(documents)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--catalog-url",
        default=os.environ.get(
            "API_DOCS_CATALOG_URL",
            "http://localhost:9128/internal/api-docs",
        ),
    )
    parser.add_argument("--output-dir", default="docs/runtime-api")
    parser.add_argument("--refresh", action="store_true")
    args = parser.parse_args()
    username = os.environ.get("API_DOCS_USERNAME")
    password = os.environ.get("API_DOCS_PASSWORD")
    bearer_token = os.environ.get("API_DOCS_BEARER_TOKEN")
    count = export(
        args.catalog_url,
        Path(args.output_dir),
        username,
        password,
        bearer_token,
        args.refresh,
    )
    print(f"Exported {count} live service specifications to {args.output_dir}")


if __name__ == "__main__":
    main()
