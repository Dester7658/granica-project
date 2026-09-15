import { CrossingConfig } from "../types";

const SOURCE_URL = "https://granica.gov.pl/index_wait.php";
const CACHE_TTL_MS = 5 * 60 * 1000;

type PolandWaitMap = Map<string, { waitMinutes?: number; waitUpdatedAt?: string }>;

let cache: { expiresAt: number; waits: PolandWaitMap } | undefined;

function clean(value: string): string {
  return value
    .replace(/<[^>]+>/g, " ")
    .replace(/&nbsp;/gi, " ")
    .replace(/&ndash;/gi, "-")
    .replace(/&amp;/gi, "&")
    .replace(/\s+/g, " ")
    .trim();
}

function toMinutes(value: string): number | undefined {
  const normalized = value.replace(/[^\d:]/g, " ");
  const match = normalized.match(/(\d{1,3})\s*:\s*(\d{2})/);
  if (!match) return undefined;
  return Number(match[1]) * 60 + Number(match[2]);
}

function parseCells(row: string): string[] {
  return [...row.matchAll(/<td\b[^>]*>([\s\S]*?)<\/td>/gi)].map((match) => clean(match[1]));
}

function parsePolandWaits(html: string): PolandWaitMap {
  const waits: PolandWaitMap = new Map();
  const header = html.match(/<th[^>]*>[\s\S]*?Ku(?:ź|Åº)nica[\s\S]*?<\/tr>/i)?.[0];
  if (!header) return waits;

  const waitRow = html.match(/<tr>[\s\S]*?alt="[^"]*OSOBOWE[^"]*"[\s\S]*?<\/tr>/i)?.[0];
  if (!waitRow) return waits;

  const values = [...waitRow.matchAll(/<td\b[^>]*>[\s\S]*?(\d{1,3}:\d{2})[\s\S]*?<\/td>/gi)].map(
    (match) => match[1],
  );
  const updateMarker = html.search(/Godzina aktualizacji/i);
  const updateSection = updateMarker >= 0 ? html.slice(updateMarker, updateMarker + 1200) : "";
  const updateTimes = parseCells(updateSection);
  const crossingIds = [
    ["by-bruzgi-exit", "by-bruzgi-entry"],
    ["by-brestovitsa-exit", "by-brestovitsa-entry"],
    [],
    [],
    ["by-warsaw-bridge"],
    [],
  ];

  for (let index = 0; index < Math.min(values.length, crossingIds.length); index += 1) {
    const waitMinutes = toMinutes(values[index]);
    const updated = updateTimes[index + 1] || undefined;
    for (const crossingId of crossingIds[index]) {
      waits.set(crossingId, { waitMinutes, waitUpdatedAt: updated });
    }
  }
  return waits;
}

export async function getPolandWaits(): Promise<PolandWaitMap> {
  if (cache && cache.expiresAt > Date.now()) return cache.waits;

  const response = await fetch(SOURCE_URL, {
    headers: { "User-Agent": "GranicaApp/1.0 (+border-wait-information)" },
  });
  if (!response.ok) throw new Error(`Poland wait source returned ${response.status}`);

  const waits = parsePolandWaits(await response.text());
  cache = { expiresAt: Date.now() + CACHE_TTL_MS, waits };
  return waits;
}

export function applyPolandWait(crossing: CrossingConfig, waits: PolandWaitMap): CrossingConfig {
  const value = waits.get(crossing.id);
  if (!value || value.waitMinutes === undefined) return crossing;
  return {
    ...crossing,
    waitMinutes: value.waitMinutes,
    waitUpdatedAt: value.waitUpdatedAt,
    source: "poland",
  };
}
