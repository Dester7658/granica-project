import registryJson from "../config/crossings.json";
import { BorderConfig, CameraConfig, CountryMeta, CrossingConfig, CrossingsRegistry } from "../types";
import { getWsdotBorder } from "./wsdotService";

const staticRegistry = registryJson as CrossingsRegistry;

async function getAllBorders(): Promise<BorderConfig[]> {
  const wsdotBorder = await getWsdotBorder();
  return wsdotBorder ? [...staticRegistry.borders, wsdotBorder] : staticRegistry.borders;
}

export function getCountries(): CountryMeta[] {
  return staticRegistry.countries;
}

export interface BorderSummary {
  id: string;
  otherCountryCode: string;
  otherCountryName: string;
  crossingCount: number;
}

export async function getBordersForCountry(countryCode: string): Promise<BorderSummary[]> {
  const borders = await getAllBorders();
  const countryByCode = new Map(staticRegistry.countries.map((c) => [c.code, c.name]));
  const code = countryCode.toUpperCase();

  return borders
    .filter((b) => b.countryA === code || b.countryB === code)
    .map((b) => {
      const otherCode = b.countryA === code ? b.countryB : b.countryA;
      return {
        id: b.id,
        otherCountryCode: otherCode,
        otherCountryName: countryByCode.get(otherCode) ?? otherCode,
        crossingCount: b.crossings.length,
      };
    });
}

export async function getBorderById(borderId: string): Promise<BorderConfig | undefined> {
  const borders = await getAllBorders();
  return borders.find((b) => b.id === borderId);
}

export async function getCrossingById(crossingId: string): Promise<CrossingConfig | undefined> {
  const borders = await getAllBorders();
  for (const border of borders) {
    const found = border.crossings.find((c) => c.id === crossingId);
    if (found) return found;
  }
  return undefined;
}

export async function getCameraById(crossingId: string, cameraId: string): Promise<CameraConfig | undefined> {
  const crossing = await getCrossingById(crossingId);
  return crossing?.cameras.find((cam) => cam.id === cameraId);
}
