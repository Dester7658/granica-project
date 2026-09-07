import { CameraConfig, BorderConfig, CrossingConfig } from "../types";

/**
 * Real, official, publicly documented WSDOT (Washington State DOT) Traveler
 * Information API. Verified endpoints (return HTTP 401 without a key, which
 * confirms they exist and are reachable, rather than 404):
 *   https://wsdot.wa.gov/traffic/api/Documentation/group___border_crossings.html
 *   https://wsdot.wa.gov/traffic/api/Documentation/group___highway_cameras.html
 * Covers the WA<->Canada border crossings on I-5, SR-543, SR-539 and SR-9
 * (Peace Arch, Pacific Highway/Blaine, Lynden, Sumas). Free AccessCode:
 * https://wsdot.wa.gov/traffic/api/
 */

const BORDER_CROSSINGS_URL =
  "http://www.wsdot.wa.gov/Traffic/api/BorderCrossings/BorderCrossingsREST.svc/GetBorderCrossingsAsJson";
const CAMERAS_URL =
  "http://www.wsdot.wa.gov/Traffic/api/HighwayCameras/HighwayCamerasREST.svc/GetCamerasAsJson";

const NEARBY_CAMERA_RADIUS_KM = 3;
const MAX_CAMERAS_PER_CROSSING = 3;
const CACHE_TTL_MS = 60_000;

interface WsdotBorderCrossing {
  BorderCrossingLocation: {
    Description: string | null;
    Latitude: number;
    Longitude: number;
    MilePost: number;
    RoadName: string;
  } | null;
  CrossingName: string;
  Time: string; // .NET "/Date(1234567890000-0700)/" format
  WaitTime: number;
}

interface WsdotCamera {
  CameraID: number;
  Description: string | null;
  DisplayLatitude: number;
  DisplayLongitude: number;
  ImageURL: string;
  IsActive: boolean;
  Title: string;
}

interface Cache {
  expiresAt: number;
  crossings: CrossingConfig[];
}

let cache: Cache | null = null;

function haversineKm(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371;
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180) * Math.sin(dLon / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function parseWsdotDate(value: string): string | undefined {
  const match = /\/Date\((\d+)/.exec(value);
  return match ? new Date(Number(match[1])).toISOString() : undefined;
}

function slugify(name: string): string {
  return name
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/(^-|-$)/g, "");
}

async function wsdotFetch<T>(url: string, accessCode: string): Promise<T> {
  const response = await fetch(`${url}?AccessCode=${encodeURIComponent(accessCode)}`);
  if (!response.ok) {
    throw new Error(`WSDOT API returned ${response.status} for ${url}`);
  }
  return (await response.json()) as T;
}

async function fetchLiveCrossings(accessCode: string): Promise<CrossingConfig[]> {
  const [crossings, cameras] = await Promise.all([
    wsdotFetch<WsdotBorderCrossing[]>(BORDER_CROSSINGS_URL, accessCode),
    wsdotFetch<WsdotCamera[]>(CAMERAS_URL, accessCode),
  ]);

  const activeCameras = cameras.filter((c) => c.IsActive && c.ImageURL);

  return crossings
    .filter((c) => c.BorderCrossingLocation)
    .map((c) => {
      const loc = c.BorderCrossingLocation!;
      const nearbyCameras: CameraConfig[] = activeCameras
        .map((cam) => ({
          cam,
          distanceKm: haversineKm(loc.Latitude, loc.Longitude, cam.DisplayLatitude, cam.DisplayLongitude),
        }))
        .filter((x) => x.distanceKm <= NEARBY_CAMERA_RADIUS_KM)
        .sort((a, b) => a.distanceKm - b.distanceKm)
        .slice(0, MAX_CAMERAS_PER_CROSSING)
        .map(({ cam }) => ({
          id: `cam-${cam.CameraID}`,
          name: cam.Title || cam.Description || `Camera ${cam.CameraID}`,
          type: "snapshot" as const,
          snapshotUrl: cam.ImageURL,
          refreshSeconds: 60,
        }));

      const crossing: CrossingConfig = {
        id: `wsdot-${slugify(c.CrossingName)}`,
        name: `${c.CrossingName} (US-Canada, I-5/WA)`,
        lat: loc.Latitude,
        lon: loc.Longitude,
        direction: "both",
        cameras: nearbyCameras,
        waitMinutes: c.WaitTime,
        waitUpdatedAt: parseWsdotDate(c.Time),
        source: "wsdot",
      };
      return crossing;
    });
}

export async function getWsdotBorder(): Promise<BorderConfig | undefined> {
  const accessCode = process.env.WSDOT_ACCESS_CODE;
  if (!accessCode) return undefined;

  if (!cache || cache.expiresAt < Date.now()) {
    try {
      const crossings = await fetchLiveCrossings(accessCode);
      cache = { crossings, expiresAt: Date.now() + CACHE_TTL_MS };
    } catch (err) {
      console.error("Failed to fetch WSDOT data:", (err as Error).message);
      if (!cache) return undefined; // no stale data to fall back on
    }
  }

  return {
    id: "us-ca",
    countryA: "US",
    countryB: "CA",
    crossings: cache!.crossings,
  };
}
