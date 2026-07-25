#!/usr/bin/env python3
import unittest

import generate_api_docs as generator


class ApiDocsInventoryTest(unittest.TestCase):
    def setUp(self):
        self.endpoints = generator.build_endpoints()
        self.routes = {
            (endpoint["method"], endpoint["path"])
            for endpoint in self.endpoints
        }

    def test_method_and_path_inventory_has_no_duplicates(self):
        keys = [
            (endpoint["method"], endpoint["path"])
            for endpoint in self.endpoints
        ]
        self.assertEqual(len(keys), len(set(keys)))

    def test_batch_worker_routes_are_complete(self):
        expected = set()
        for prefix in ("/endpoint/batch", "/internal/batch"):
            expected.update({
                ("POST", f"{prefix}/definitions"),
                ("GET", f"{prefix}/definitions/{{definitionKey}}"),
                ("POST", f"{prefix}/definitions/{{definitionKey}}/runs"),
                ("GET", f"{prefix}/runs/{{id}}"),
                ("GET", f"{prefix}/runs"),
                ("GET", f"{prefix}/runs/{{id}}/rejected-items"),
                ("POST", f"{prefix}/runs/{{id}}/retry"),
            })
        self.assertTrue(expected.issubset(self.routes), expected - self.routes)

    def test_batch_example_uses_platform_records_and_secret_safe_basic_auth(self):
        body = generator.example_batch_definition()
        source = body["spec"]["source"]
        destination = body["spec"]["destination"]
        self.assertIn("/internal/entities/records/importer-order", source["url"])
        self.assertEqual(source["itemsPath"], "content")
        self.assertEqual(source["authentication"]["type"], "BASIC")
        self.assertIn("secretEnvironmentVariable", source["authentication"])
        self.assertEqual(destination["authentication"]["type"], "BASIC")
        self.assertNotIn("password", source["authentication"])
        self.assertNotIn("secret", source["authentication"])

    def test_dynamic_record_lists_document_bounded_pagination(self):
        record_lists = [
            endpoint for endpoint in self.endpoints
            if endpoint["summary"] == "List Records"
        ]
        self.assertTrue(record_lists)
        for endpoint in record_lists:
            query_names = {item["name"] for item in endpoint["query"]}
            self.assertEqual(query_names, {"page", "size", "sort"})
            self.assertIn("legacy array", endpoint["description"])

    def test_durable_automation_routes_are_complete(self):
        expected = {
            ("POST", "/endpoint/automation-orchestrator/flows/{flowKey}/manual-run"),
            ("POST", "/endpoint/automation-orchestrator/credentials"),
            ("GET", "/endpoint/automation-orchestrator/credentials"),
            ("PATCH", "/endpoint/automation-orchestrator/credentials/{id}/rotate"),
            ("GET", "/public/automation-flows/node-structures"),
            ("GET", "/public/automation-flows/edge-structures"),
            ("POST", "/public/automation-orchestrator/webhooks/{flowKey}"),
            (
                "POST",
                "/public/automation-orchestrator/executions/"
                "{executionId}/nodes/{nodeId}/callback",
            ),
        }
        for prefix in (
            "/endpoint/automation-orchestrator",
            "/internal/automation-orchestrator",
        ):
            expected.update({
                ("POST", f"{prefix}/executions/start"),
                ("GET", f"{prefix}/executions/{{executionId}}"),
                ("POST", f"{prefix}/executions/{{executionId}}/cancel"),
                ("GET", f"{prefix}/executions/{{executionId}}/steps"),
                ("GET", f"{prefix}/executions/{{executionId}}/dead-letters"),
                (
                    "POST",
                    f"{prefix}/executions/{{executionId}}/dead-letters/"
                    "{deadLetterId}/requeue",
                ),
                ("GET", f"{prefix}/metrics"),
                ("GET", f"{prefix}/executions"),
                ("POST", f"{prefix}/executions/{{executionId}}/retry"),
            })
        for prefix in (
            "/endpoint/automation-flows",
            "/internal/automation-flows",
        ):
            expected.update({
                ("POST", prefix),
                ("GET", prefix),
                ("GET", f"{prefix}/{{flowKey}}/versions/{{version}}"),
                ("GET", f"{prefix}/{{flowKey}}/active"),
                (
                    "POST",
                    f"{prefix}/{{flowKey}}/versions/{{version}}/{{action}}",
                ),
                ("POST", f"{prefix}/n8n/analyze"),
                ("POST", f"{prefix}/n8n/import"),
                (
                    "GET",
                    f"{prefix}/{{flowKey}}/versions/{{version}}/n8n-export",
                ),
            })
        self.assertTrue(expected.issubset(self.routes), expected - self.routes)

    def test_recent_bpm_collaboration_and_metadata_routes_are_complete(self):
        expected = {
            ("GET", "/public/dynamic-flows/state-action-structures"),
            ("GET", "/public/dynamic-flows/transition-condition-structures"),
        }
        for prefix in (
            "/endpoint/bpm/managed-objects",
            "/internal/bpm/managed-objects",
        ):
            expected.update({
                ("POST", f"{prefix}/{{objectId}}/comments"),
                ("GET", f"{prefix}/{{objectId}}/comments"),
                ("POST", f"{prefix}/{{objectId}}/attachments"),
                ("GET", f"{prefix}/{{objectId}}/attachments"),
            })
        self.assertTrue(expected.issubset(self.routes), expected - self.routes)

    def test_postman_collection_has_recent_folders_variables_and_tests(self):
        collection = generator.build_postman_collection(self.endpoints)
        folders = {
            folder["name"]: folder["item"]
            for folder in collection["item"]
        }
        expected_counts = {
            "Batch Worker": 7,
            "Batch Worker Internal": 7,
            "Automation Orchestrator": 10,
            "Automation Orchestrator Internal": 9,
            "Automation Flows": 8,
            "Automation Flows Internal": 8,
            "Automation Credentials": 3,
            "Automation Public": 4,
            "BPM Public Metadata": 2,
        }
        for folder, count in expected_counts.items():
            self.assertEqual(len(folders[folder]), count)

        variables = {
            item["key"]: item["value"]
            for item in collection["variable"]
        }
        self.assertIn("batch_run_id", variables)
        self.assertIn("automation_flow_key", variables)
        self.assertIn("credential_id", variables)

        start_batch = next(
            item for item in folders["Batch Worker"]
            if item["name"] == "Start Batch Run"
        )
        self.assertIn("batch_run_id", str(start_batch["event"]))
        internal_batch = folders["Batch Worker Internal"][0]
        self.assertIn(
            "{{batch_internal_username}}",
            str(internal_batch["request"]["auth"]),
        )
        internal_automation = folders["Automation Orchestrator Internal"][0]
        self.assertIn(
            "{{automation_internal_username}}",
            str(internal_automation["request"]["auth"]),
        )

    def test_ai_examples_send_available_service_inventory(self):
        summaries = {
            "Generate Platform App",
            "Create Draft",
            "Patch Draft",
            "Create Conversation Session",
            "Send Conversation Message",
            "Generate Platform App Internal",
            "Create Draft Internal",
            "Patch Draft Internal",
            "Create Session Internal",
            "Message Session Internal",
        }
        selected = [
            endpoint for endpoint in self.endpoints
            if endpoint["summary"] in summaries
        ]
        self.assertEqual(len(selected), len(summaries))
        for endpoint in selected:
            self.assertIn(
                "availableServiceKeys",
                endpoint["body"],
                endpoint["summary"],
            )


if __name__ == "__main__":
    unittest.main()
