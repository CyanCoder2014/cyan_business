import { proxyTo } from "@/lib/bff-proxy";

const base = process.env.STOREFRONT_SERVICE_BASE_URL ?? "http://localhost:9115";

export const GET = (request: Request, context: { params: { slug: string; fieldName: string } }) => {
  const search = new URL(request.url).search;
  return proxyTo(request, `${base}/public/forms/${encodeURIComponent(context.params.slug)}/relations/${encodeURIComponent(context.params.fieldName)}${search}`);
};
