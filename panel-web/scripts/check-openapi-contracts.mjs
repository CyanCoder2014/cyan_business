import { readFile } from "node:fs/promises";

const contracts = JSON.parse(await readFile(new URL("../contracts/platform-api-contracts.json", import.meta.url), "utf8"));
const duplicates = contracts.filter((entry, index) => contracts.findIndex(candidate => candidate.service === entry.service && candidate.method === entry.method && candidate.path === entry.path) !== index);
if (duplicates.length) throw new Error(`Duplicate API contracts: ${JSON.stringify(duplicates)}`);
const invalid = contracts.filter(entry => !/^[a-z0-9-]+$/.test(entry.service) || !/^(GET|POST|PUT|PATCH|DELETE)$/.test(entry.method) || !entry.path.startsWith("/"));
if (invalid.length) throw new Error(`Invalid API contracts: ${JSON.stringify(invalid)}`);

const catalogBase = process.env.PLATFORM_API_DOCS_URL;
if (!catalogBase) {
  console.log(`Validated ${contracts.length} checked-in client contracts. Set PLATFORM_API_DOCS_URL to verify them against live OpenAPI documents.`);
  process.exit(0);
}
const token = process.env.PLATFORM_API_TOKEN;
const headers = token ? { Authorization: `Bearer ${token}` } : {};
for (const service of [...new Set(contracts.map(entry => entry.service))]) {
  const response = await fetch(`${catalogBase.replace(/\/$/, "")}/endpoint/api-docs/services/${encodeURIComponent(service)}?refresh=true`, { headers });
  if (!response.ok) throw new Error(`${service} OpenAPI unavailable (${response.status})`);
  const document = await response.json();
  for (const entry of contracts.filter(value => value.service === service)) {
    const operation = document.paths?.[entry.path]?.[entry.method.toLowerCase()];
    if (!operation) throw new Error(`${service} is missing ${entry.method} ${entry.path}`);
  }
}
console.log(`Verified ${contracts.length} client contracts against live OpenAPI documents.`);
