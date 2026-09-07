export type CameraType = "snapshot" | "mjpeg" | "hls" | "webview";

export interface CameraConfig {
  id: string;
  name: string;
  type: CameraType;
  snapshotUrl?: string;
  streamUrl?: string;
  /** For type "webview": official page to open in an in-app WebView (used when the image CDN blocks server-side fetches but works fine from a real device/browser). */
  pageUrl?: string;
  refreshSeconds?: number;
}

export interface CrossingConfig {
  id: string;
  name: string;
  isDemo?: boolean;
  lat: number;
  lon: number;
  direction: "in" | "out" | "both";
  cameras: CameraConfig[];
  /** Minutes of current wait, when the source provides live traffic data (e.g. WSDOT). */
  waitMinutes?: number;
  waitUpdatedAt?: string;
  source?: "static" | "wsdot";
}

export interface CountryMeta {
  code: string;
  name: string;
}

export interface BorderConfig {
  id: string;
  /** ISO-ish country codes on each side of this border (order has no meaning). */
  countryA: string;
  countryB: string;
  crossings: CrossingConfig[];
}

export interface CrossingsRegistry {
  note?: string;
  countries: CountryMeta[];
  borders: BorderConfig[];
}
