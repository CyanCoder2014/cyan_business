import { proxyTo } from "@/lib/bff-proxy";

const base = process.env.STOREFRONT_SERVICE_BASE_URL ?? "http://localhost:9115";
export const POST = (request: Request, context: { params: { slug: string } }) => proxyTo(request, `${base}/public/forms/${encodeURIComponent(context.params.slug)}/submissions`);
