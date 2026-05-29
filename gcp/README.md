# FastAPI Cloud Run Service

This folder contains a minimal FastAPI app ready for Google Cloud Run deployment.

## Endpoints

- `GET /health` returns the health check response:

```json
{ "status": "ok" }
```

- `POST /process` sends payload to Gemini and returns a JSON result.

Example request:

```bash
curl -X POST http://localhost:8080/process \
  -H "Content-Type: application/json" \
  -d '{
    "user": "agos",
    "value": "Write a one-line summary of Cloud Run."
  }'
```

Example response:

```json
{ "user": "agos", "result": "Cloud Run executes your containerized apps on demand without managing servers." }
```

Required environment variable:

- `GEMINI_API_KEY`

Optional environment variable:

- `GEMINI_MODEL` (default: `gemini-1.5-flash`)

## Run locally

```bash
cd gcp
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
export GEMINI_API_KEY="your_api_key"
uvicorn app.main:app --reload --host 0.0.0.0 --port 8080
```

Test endpoint:

```bash
curl http://localhost:8080/health
```

## Run locally with Docker Compose

```bash
cd gcp
export GEMINI_API_KEY="your_api_key"
docker compose up --build
```

Run in detached mode:

```bash
docker compose up -d --build
```

Stop services:

```bash
docker compose down
```

View logs:

```bash
docker compose logs -f api
```

Note:

- The `/process` endpoint uses internal system and user instructions defined in `app/main.py`.
- Send only `user` and `value` in the request body.

## Deploy with GitHub Actions

Workflow file: `.github/workflows/deploy-cloud-run.yml`

Deploy triggers:

- Push to `master` with changes under `gcp/**`
- Manual run via `workflow_dispatch`

Required GitHub repository secrets:

- `GCP_PROJECT_ID`
- `GCP_WORKLOAD_IDENTITY_PROVIDER`
- `GCP_SERVICE_ACCOUNT`

Optional GitHub repository variables:

- `CLOUD_RUN_SERVICE` (default: `ambiguity-killer-api`)
- `CLOUD_RUN_REGION` (default: `us-central1`)

The workflow deploys from the `gcp` folder as source code to Cloud Run.
