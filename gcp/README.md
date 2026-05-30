# GCP Ambiguity Killer Service

This folder contains a FastAPI application that helps clarify Google Cloud Platform (GCP) concepts and services by analyzing user queries and providing clear explanations.

## Features

- Analyzes ambiguous GCP-related questions
- Provides clear, concise explanations of GCP concepts
- Uses Google Gemini AI for intelligent responses
- Includes input validation to prevent command injection
- Ready for deployment on Google Cloud Run

## Endpoints

### Health Check
- `GET /health` returns the health check response:

```json
{ "status": "ok" }
```

### Process Query
- `POST /process` analyzes user input and returns a GCP explanation.

Example request:

```bash
curl -X POST http://localhost:8080/process \
  -H "Content-Type: application/json" \
  -d '{
    "user": "agos",
    "value": "What is Cloud Run?"
  }'
```

Example response:

```json
{ "user": "agos", "result": "Cloud Run is a managed compute platform that lets you run containerized applications without having to manage servers." }
```

## Environment Variables

Required environment variable:

- `GEMINI_API_KEY` - Your Google Gemini API key

Optional environment variable:

- `GEMINI_MODEL` (default: `gemini-2.5-flash`) - The Gemini model to use

## Input Validation

The service includes security measures to prevent command injection attacks by validating input against patterns like:
- Command execution syntax (`$()`, `${}`, `&&`, `||`)
- Dangerous system commands (`rm -rf`, `sudo`, `chmod`, etc.)

## Run locally

```bash
cd gcp
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
export GEMINI_API_KEY="your_api_key"
uvicorn app.main:app --reload --host 0.0.0.0 --port 8080
```

## Deploy to Google Cloud Run

```bash
gcloud run deploy gcp-ambiguity-killer \
  --source . \
  --platform managed \
  --region us-central1 \
  --set-env-vars GEMINI_API_KEY=your-api-key
```

## Docker Deployment

You can also run the service using Docker:

```bash
# Build the image
docker build -t gcp-ambiguity-killer .

# Run locally
docker run -p 8080:8080 \
  -e GEMINI_API_KEY=your-api-key \
  gcp-ambiguity-killer
```

## Docker Compose

For local development, you can use docker-compose:

```bash
docker-compose up
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
