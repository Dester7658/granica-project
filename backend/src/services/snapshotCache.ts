import NodeCache from "node-cache";

interface CachedImage {
  buffer: Buffer;
  contentType: string;
}

// Cache raw snapshot bytes for a short time so many app users hitting the
// same camera don't hammer the upstream source more than once per interval.
const cache = new NodeCache({ stdTTL: 10, checkperiod: 15 });

export async function fetchSnapshot(url: string, ttlSeconds: number): Promise<CachedImage> {
  const cacheKey = url;
  const cached = cache.get<CachedImage>(cacheKey);
  if (cached) return cached;

  const response = await fetch(url, {
    headers: { "User-Agent": "GranicaApp/1.0 (+border-camera-viewer)" },
  });
  if (!response.ok) {
    throw new Error(`Upstream camera returned ${response.status}`);
  }
  const arrayBuffer = await response.arrayBuffer();
  const image: CachedImage = {
    buffer: Buffer.from(arrayBuffer),
    contentType: response.headers.get("content-type") ?? "image/jpeg",
  };
  cache.set(cacheKey, image, Math.max(1, ttlSeconds));
  return image;
}
