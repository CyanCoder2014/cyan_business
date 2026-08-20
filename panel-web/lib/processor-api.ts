import { platformFetch } from "@/lib/platform-auth";
import { platformErrorFromResponse } from "@/lib/api-error";

export type ProcessorDefinition = {
  id?: number;
  processorKey: string;
  targetType?: string;
  description?: string;
  active?: boolean;
};

export async function listProcessors(): Promise<ProcessorDefinition[]> {
  const response = await platformFetch("/api/platform/service/processor-service/api/processor-service/processors", { cache: "no-store" });
  if (!response.ok) throw await platformErrorFromResponse(response);
  return response.json() as Promise<ProcessorDefinition[]>;
}
