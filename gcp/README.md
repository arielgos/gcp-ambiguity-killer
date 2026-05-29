# FastAPI Cloud Run Service

This folder contains a minimal FastAPI app ready for Google Cloud Run deployment.

## Endpoints

- `GET /health` returns the health check response:

```json
{ "status": "ok" }
```

## Run locally

```bash
cd gcp
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8080
```

Test endpoint:

```bash
curl http://localhost:8080/health
```

## Run locally with Docker Compose

```bash
cd gcp
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
