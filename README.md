# gcp-ambiguity-killer

This repository contains two complementary projects:

- an Android client app in `android/`
- a FastAPI service for GCP/Cloud Run in `gcp/`

## Folder Overview

### android

Android application project built with Gradle Kotlin DSL.

Short resume:

- Mobile client code lives under `android/app/`.
- Standard Android tooling is included (`gradlew`, `gradle/`, `settings.gradle.kts`).
- Intended as the app-side interface for testing or consuming backend features.

### gcp

Backend API project based on FastAPI, ready for local Docker testing and Google Cloud Run deployment.

Short resume:

- API implementation is under `gcp/app/`.
- Includes container files (`Dockerfile`, `docker-compose.yml`) for local execution.
- Includes Python dependencies in `requirements.txt`.
- Includes its own setup/deployment guide in `gcp/README.md`.