import "dotenv/config";
import cors from "cors";
import express from "express";
import { apiRouter } from "./routes/api";

const app = express();
const PORT = process.env.PORT ? Number(process.env.PORT) : 3000;

app.use(cors());
app.use(express.json());

app.get("/health", (_req, res) => res.json({ status: "ok" }));
app.use("/api", apiRouter);

app.listen(PORT, () => {
  console.log(`Granica backend listening on http://localhost:${PORT}`);
});
