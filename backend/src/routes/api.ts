import { Router } from "express";
import {
  getBorderById,
  getBordersForCountry,
  getCameraById,
  getCountries,
  getCrossingById,
} from "../services/registryService";
import { fetchSnapshot } from "../services/snapshotCache";
import { estimateWaitFromCamera } from "../services/aiWaitService";

export const apiRouter = Router();

apiRouter.get("/countries", (_req, res) => {
  res.json(getCountries());
});

apiRouter.get("/borders", async (req, res) => {
  const country = typeof req.query.country === "string" ? req.query.country : undefined;
  if (!country) {
    res.status(400).json({ error: "Query param 'country' is required" });
    return;
  }
  res.json(await getBordersForCountry(country));
});

apiRouter.get("/borders/:id", async (req, res) => {
  const border = await getBorderById(req.params.id);
  if (!border) {
    res.status(404).json({ error: "Border not found" });
    return;
  }
  res.json(border);
});

apiRouter.get("/crossings/:id", async (req, res) => {
  const crossing = await getCrossingById(req.params.id);
  if (!crossing) {
    res.status(404).json({ error: "Crossing not found" });
    return;
  }
  res.json(crossing);
});

apiRouter.get("/crossings/:id/cameras/:cameraId/snapshot", async (req, res) => {
  const camera = await getCameraById(req.params.id, req.params.cameraId);
  if (!camera || !camera.snapshotUrl) {
    res.status(404).json({ error: "Camera or snapshot URL not found" });
    return;
  }
  try {
    const image = await fetchSnapshot(camera.snapshotUrl, camera.refreshSeconds ?? 15);
    res.setHeader("Content-Type", image.contentType);
    res.setHeader("Cache-Control", `public, max-age=${camera.refreshSeconds ?? 15}`);
    res.send(image.buffer);
  } catch (err) {
    res.status(502).json({ error: "Failed to fetch camera snapshot", detail: (err as Error).message });
  }
});

apiRouter.get("/crossings/:id/ai-estimate", async (req, res) => {
  const crossing = await getCrossingById(req.params.id);
  if (!crossing) {
    res.status(404).json({ error: "Crossing not found" });
    return;
  }

  const requestedCameraId = typeof req.query.cameraId === "string" ? req.query.cameraId : undefined;
  const camera = requestedCameraId
    ? crossing.cameras.find((item) => item.id === requestedCameraId)
    : crossing.cameras.find((item) => item.snapshotUrl || item.pageUrl);
  if (!camera) {
    res.status(422).json({ error: "No direct image camera is available for AI analysis" });
    return;
  }

  try {
    res.json(await estimateWaitFromCamera(crossing, camera));
  } catch (err) {
    res.status(503).json({ error: "AI estimate unavailable", detail: (err as Error).message });
  }
});
