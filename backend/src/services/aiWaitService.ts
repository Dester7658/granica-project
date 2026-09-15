import { CameraConfig, CrossingConfig } from "../types";
import { fetchSnapshot } from "./snapshotCache";

export interface AiWaitEstimate {
  vehicleCount: number;
  estimatedWaitMinutes: number;
  confidence: "low" | "medium" | "high";
  analyzedAt: string;
  source: "ai_camera_estimate";
}

const MODEL = process.env.GEMINI_VISION_MODEL ?? "gemini-3.8-flash";
const CACHE_TTL_SECONDS = 600;
const cache = new Map<string, { expiresAt: number; value: AiWaitEstimate }>();

function imageUrlFor(camera: CameraConfig): string | undefined {
  const candidate = camera.snapshotUrl ?? camera.pageUrl;
  if (!candidate) return undefined;
  return /\.(jpe?g|png|webp)(\?|$)/i.test(candidate) ? candidate : undefined;
}

function extractJson(content: string): Record<string, unknown> {
  const fenced = content.match(/```(?:json)?\s*([\s\S]*?)\s*```/i)?.[1] ?? content;
  const parsed: unknown = JSON.parse(fenced);
  if (!parsed || typeof parsed !== "object") throw new Error("AI returned invalid JSON");
  return parsed as Record<string, unknown>;
}

export async function estimateWaitFromCamera(
  crossing: CrossingConfig,
  camera: CameraConfig,
): Promise<AiWaitEstimate> {
  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) throw new Error("AI wait estimation is not configured");

  const imageUrl = imageUrlFor(camera);
  if (!imageUrl) throw new Error("This camera does not provide a direct image");

  const cacheKey = `${crossing.id}:${camera.id}`;
  const cached = cache.get(cacheKey);
  if (cached && cached.expiresAt > Date.now()) return cached.value;

  const image = await fetchSnapshot(imageUrl, 15);
  const imageBase64 = image.buffer.toString("base64");
  const mediaType = image.contentType.startsWith("image/") ? image.contentType : "image/jpeg";

  const requestBody = JSON.stringify({
    generationConfig: {
      temperature: 0,
      responseMimeType: "application/json",
    },
    contents: [
      {
        parts: [
          {
            text: `You estimate border queue length from one traffic camera image. Count only clearly visible cars waiting in the border queue. Never invent hidden cars. Estimate minutes conservatively from visible queue size. Return only JSON with vehicleCount (integer), estimatedWaitMinutes (integer), confidence (low, medium, or high). This is an approximate AI estimate, not official data. Border crossing: ${crossing.name}. Analyze this current camera frame.`,
          },
          {
            inlineData: {
              mimeType: mediaType,
              data: imageBase64,
            },
          },
        ],
      },
    ],
  });

  let response: Response | undefined;
  for (let attempt = 0; attempt < 2; attempt += 1) {
    response = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/${MODEL}:generateContent`,
      {
        method: "POST",
        headers: {
          "x-goog-api-key": apiKey,
          "Content-Type": "application/json",
        },
        body: requestBody,
      },
    );
    if (response.ok || ![500, 503].includes(response.status) || attempt === 1) break;
    await new Promise((resolve) => setTimeout(resolve, 600));
  }

  const finalResponse = response;
  if (!finalResponse || !finalResponse.ok) {
    if (!finalResponse) throw new Error("Gemini returned no response");
    const providerError = await finalResponse.text();
    throw new Error(`Gemini returned ${finalResponse.status}: ${providerError.slice(0, 300)}`);
  }
  const payload = (await finalResponse.json()) as {
    candidates?: Array<{ content?: { parts?: Array<{ text?: string }> } }>;
  };
  const content = payload.candidates?.[0]?.content?.parts?.map((part) => part.text ?? "").join("");
  if (!content) throw new Error("Gemini returned no estimate");

  const result = extractJson(content);
  const vehicleCount = Number(result.vehicleCount);
  const estimatedWaitMinutes = Number(result.estimatedWaitMinutes);
  const confidence = result.confidence;
  if (
    !Number.isInteger(vehicleCount) || vehicleCount < 0 || vehicleCount > 500 ||
    !Number.isInteger(estimatedWaitMinutes) || estimatedWaitMinutes < 0 || estimatedWaitMinutes > 1440 ||
    !["low", "medium", "high"].includes(String(confidence))
  ) {
    throw new Error("AI returned an unsafe estimate");
  }

  const estimate: AiWaitEstimate = {
    vehicleCount,
    estimatedWaitMinutes,
    confidence: confidence as AiWaitEstimate["confidence"],
    analyzedAt: new Date().toISOString(),
    source: "ai_camera_estimate",
  };
  cache.set(cacheKey, { expiresAt: Date.now() + CACHE_TTL_SECONDS * 1000, value: estimate });
  return estimate;
}
