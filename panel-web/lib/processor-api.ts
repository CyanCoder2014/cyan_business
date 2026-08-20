import { platformFetch } from "@/lib/platform-auth";
import { platformErrorFromResponse } from "@/lib/api-error";

export type ProcessorDefinition = {
  id?: number;
  processorKey: string;
  targetType?: string;
  validatorsJson?: string;
  operatorsJson?: string;
  description?: string;
  active?: boolean;
};

const base = "/api/platform/service/processor-service/api/processor-service/processors";

export async function listProcessors(): Promise<ProcessorDefinition[]> {
  const response = await platformFetch(base, { cache: "no-store" });
  if (!response.ok) throw await platformErrorFromResponse(response);
  return response.json() as Promise<ProcessorDefinition[]>;
}

export async function createProcessor(definition: ProcessorDefinition): Promise<ProcessorDefinition> {
  const response = await platformFetch(base, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(definition),
  });
  if (!response.ok) throw await platformErrorFromResponse(response);
  return response.json() as Promise<ProcessorDefinition>;
}
