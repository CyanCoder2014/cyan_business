const STATIC_CACHE = "cyan-shell-v1";
const STATIC_ASSETS = ["/manifest.json", "/icon.svg", "/fonts/Vazir.woff2", "/fonts/Vazir-Medium.woff2", "/fonts/Vazir-Bold.woff2"];
self.addEventListener("install", (event) => event.waitUntil(caches.open(STATIC_CACHE).then((cache) => cache.addAll(STATIC_ASSETS))));
self.addEventListener("activate", (event) => event.waitUntil(caches.keys().then((keys) => Promise.all(keys.filter((key) => key !== STATIC_CACHE).map((key) => caches.delete(key))))));
self.addEventListener("fetch", (event) => {
  const request = event.request;
  if (request.method !== "GET") return;
  const url = new URL(request.url);
  if (url.origin !== self.location.origin || url.pathname.startsWith("/api/")) return;
  if (!STATIC_ASSETS.includes(url.pathname) && !url.pathname.startsWith("/_next/static/")) return;
  event.respondWith(caches.match(request).then((cached) => cached || fetch(request).then((response) => { if (response.ok) caches.open(STATIC_CACHE).then((cache) => cache.put(request, response.clone())); return response; })));
});
