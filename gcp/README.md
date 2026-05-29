# FastAPI Cloud Run Service

This folder contains a minimal FastAPI app ready for Google Cloud Run deployment.

## Endpoints

- `GET /` returns a simple service message.
- `GET /health` returns a health check response.

## Run locally

```bash
cd gcp
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8080
```

Health endpoint:

```bash
curl http://localhost:8080/health
```

## Deploy with GitHub Actions

Workflow file: `.github/workflows/deploy-cloud-run.yml`

Required GitHub repository secrets:

- `GCP_PROJECT_ID`
- `GCP_WORKLOAD_IDENTITY_PROVIDER`
- `GCP_SERVICE_ACCOUNT`

Optional GitHub repository variables:

- `CLOUD_RUN_SERVICE` (default: `ambiguity-killer-api`)
- `CLOUD_RUN_REGION` (default: `us-central1`)

The workflow deploys from the `gcp` folder as source code to Cloud Run.
