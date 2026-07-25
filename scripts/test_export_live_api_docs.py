#!/usr/bin/env python3
import unittest

import export_live_api_docs as exporter


class LiveApiDocsExporterTest(unittest.TestCase):
    def test_postman_is_derived_from_paths_schemas_headers_and_auth(self):
        spec = {
            "openapi": "3.1.0",
            "paths": {
                "/internal/orders/{orderKey}": {
                    "get": {
                        "summary": "Get Order",
                        "security": [{"basicAuth": []}],
                        "parameters": [
                            {"name": "orderKey", "in": "path", "required": True},
                            {"name": "X-Tenant-Key", "in": "header"},
                        ],
                    }
                },
                "/endpoint/orders": {
                    "post": {
                        "summary": "Create Order",
                        "security": [{"bearerAuth": []}],
                        "requestBody": {
                            "content": {
                                "application/json": {
                                    "schema": {"$ref": "#/components/schemas/OrderRequest"}
                                }
                            }
                        },
                    }
                },
            },
            "components": {
                "schemas": {
                    "OrderRequest": {
                        "type": "object",
                        "properties": {
                            "customerKey": {"type": "string"},
                            "amount": {"type": "number", "minimum": 0},
                        },
                    }
                }
            },
        }
        collection = exporter.build_postman_collection(
            {"commerce-service": spec},
            [{"serviceKey": "commerce-service", "baseUrl": "http://commerce-service:9104"}],
        )

        requests = collection["item"][0]["item"]
        get_order = next(item for item in requests if item["name"] == "Get Order")
        create_order = next(item for item in requests if item["name"] == "Create Order")
        self.assertEqual(get_order["request"]["auth"]["type"], "basic")
        self.assertIn("{{orderKey}}", get_order["request"]["url"]["raw"])
        self.assertIn("X-Tenant-Key", str(get_order["request"]["header"]))
        self.assertEqual(create_order["request"]["auth"]["type"], "bearer")
        self.assertEqual(
            create_order["request"]["body"]["raw"],
            '{\n  "customerKey": "string",\n  "amount": 0\n}',
        )

    def test_public_operation_uses_no_auth(self):
        self.assertEqual(exporter.auth_for({"security": []}), {"type": "noauth"})
        self.assertEqual(
            exporter.auth_for({"x-platform-auth": "NONE"}),
            {"type": "noauth"},
        )

    def test_platform_auth_extension_can_select_basic(self):
        auth = exporter.auth_for(
            {"x-platform-auth": "BASIC"},
            "processor-service",
        )
        self.assertEqual(auth["type"], "basic")
        self.assertIn(
            "{{processor_service_internal_username}}",
            str(auth),
        )


if __name__ == "__main__":
    unittest.main()
