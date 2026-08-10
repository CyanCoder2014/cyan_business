import { NextResponse } from "next/server";

const requestHeaders=["authorization","content-type","accept","accept-language","x-tenant-key","x-site-key","idempotency-key","if-match","if-none-match","range","x-correlation-id"];
const responseHeaders=["content-type","content-disposition","cache-control","etag","last-modified","content-range","accept-ranges","x-correlation-id","retry-after"];
const timeoutMs=Math.max(1000,Math.min(Number(process.env.PANEL_BFF_TIMEOUT_MS??20000),120000));

export async function proxyTo(request:Request,targetUrl:string){
 const correlation=request.headers.get("x-correlation-id")??crypto.randomUUID();
 const headers=new Headers();for(const key of requestHeaders){const value=request.headers.get(key);if(value)headers.set(key,value)}headers.set("x-correlation-id",correlation);
 const body=request.method==="GET"||request.method==="HEAD"?undefined:await request.arrayBuffer();
 try{const upstream=await fetch(targetUrl,{method:request.method,headers,body,cache:"no-store",redirect:"manual",signal:AbortSignal.timeout(timeoutMs)});const safe=new Headers();for(const key of responseHeaders){const value=upstream.headers.get(key);if(value)safe.set(key,value)}if(!safe.has("x-correlation-id"))safe.set("x-correlation-id",correlation);return new NextResponse(upstream.body,{status:upstream.status,headers:safe})}
 catch(error){const timeout=error instanceof DOMException&&error.name==="TimeoutError";return NextResponse.json({status:timeout?504:502,error:timeout?"Gateway Timeout":"Bad Gateway",errorCode:timeout?"BFF_UPSTREAM_TIMEOUT":"BFF_UPSTREAM_UNAVAILABLE",message:timeout?"The service did not respond before the request deadline.":"The service is currently unavailable.",details:{},fieldErrors:[],correlationId:correlation,retryable:true},{status:timeout?504:502,headers:{"X-Correlation-ID":correlation,"Cache-Control":"no-store"}})}
}
