import { proxyTo } from "@/lib/bff-proxy";

const base = process.env.STOREFRONT_SERVICE_BASE_URL ?? "http://localhost:9115";
export const GET = (request: Request, context: { params: { slug: string } }) => proxyTo(request, `${base}/public/forms/${encodeURIComponent(context.params.slug)}`);
