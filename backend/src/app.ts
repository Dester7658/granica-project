import "dotenv/config";
import cors from "cors";
import express from "express";
import { apiRouter } from "./routes/api";

const app = express();

app.use(cors());
app.use(express.json());

app.get("/health", (_req, res) => res.json({ status: "ok" }));
app.use("/api", apiRouter);

export default app;
