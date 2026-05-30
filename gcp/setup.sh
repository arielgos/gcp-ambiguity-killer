#!/bin/bash

# GCP Ambiguity Killer Setup Script
# This script automates the setup of GCP resources for the Ambiguity Killer service

set -e  # Exit on any error

echo "Starting GCP Ambiguity Killer Setup..."

# Configuration
PROJECT_ID="gcp-demos-497820"
PROJECT_NUMBER="311746519106"
SERVICE_ACCOUNT_NAME="github-cloudrun-deployer"
WORKLOAD_POOL_NAME="github-pool"
WORKLOAD_PROVIDER_NAME="github-provider"
REPOSITORY_NAME="cloud-run-source-deploy"
API_SERVICE_ACCOUNT="github-cloudrun-deployer@gcp-demos-497820.iam.gserviceaccount.com"

echo "Using project: $PROJECT_ID"

# Enable required GCP services
echo "Enabling GCP services..."
gcloud services enable \
  iam.googleapis.com \
  iamcredentials.googleapis.com \
  sts.googleapis.com \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  artifactregistry.googleapis.com \
  --project "$PROJECT_ID"

# Create service account
echo "Creating service account..."
gcloud iam service-accounts create "$SERVICE_ACCOUNT_NAME" \
  --project "$PROJECT_ID" \
  --display-name "GitHub Cloud Run Deployer"

# Add IAM policies to service account
echo "Setting up IAM policies..."
gcloud projects add-iam-policy-binding "$PROJECT_ID" \
  --member "serviceAccount:$API_SERVICE_ACCOUNT" \
  --role roles/run.admin

gcloud projects add-iam-policy-binding "$PROJECT_ID" \
  --member "serviceAccount:$API_SERVICE_ACCOUNT" \
  --role roles/cloudbuild.builds.editor

gcloud projects add-iam-policy-binding "$PROJECT_ID" \
  --member "serviceAccount:$API_SERVICE_ACCOUNT" \
  --role roles/artifactregistry.writer

gcloud projects add-iam-policy-binding "$PROJECT_ID" \
  --member "serviceAccount:$API_SERVICE_ACCOUNT" \
  --role roles/iam.serviceAccountUser

# Create workload identity pool
echo "Creating workload identity pool..."
gcloud iam workload-identity-pools create "$WORKLOAD_POOL_NAME" \
  --project "$PROJECT_ID" \
  --location global \
  --display-name "GitHub Pool"

# Create workload identity provider
echo "Creating workload identity provider..."
gcloud iam workload-identity-pools providers create-oidc "$WORKLOAD_PROVIDER_NAME" \
  --project "$PROJECT_ID" \
  --location global \
  --workload-identity-pool "$WORKLOAD_POOL_NAME" \
  --display-name "GitHub Provider" \
  --issuer-uri "https://token.actions.githubusercontent.com" \
  --attribute-mapping="google.subject=assertion.sub,attribute.actor=assertion.actor,attribute.repository=assertion.repository" \
  --attribute-condition="assertion.repository == 'arielgos/gcp-ambiguity-killer'"

# Bind the service account to workload identity
echo "Binding service account to workload identity..."
gcloud iam service-accounts add-iam-policy-binding \
  "$API_SERVICE_ACCOUNT" \
  --project "$PROJECT_ID" \
  --role roles/iam.workloadIdentityUser \
  --member "principalSet://iam.googleapis.com/projects/$PROJECT_NUMBER/locations/global/workloadIdentityPools/$WORKLOAD_POOL_NAME/attribute.repository/arielgos/gcp-ambiguity-killer"

# Create Artifact Registry repository
echo "Creating Artifact Registry repository..."
gcloud artifacts repositories create "$REPOSITORY_NAME" \
  --repository-format=docker \
  --location=us-central1 \
  --description="Docker repository for Cloud Run source deployments" \
  --project "$PROJECT_ID"

# Deploy to Cloud Run
echo "Deploying to Cloud Run..."
gcloud run deploy ambiguity-killer-api \
  --source ./gcp \
  --region us-central1 \
  --allow-unauthenticated \
  --project "$PROJECT_ID"

# Add storage admin role (this seems to be a separate operation)
echo "Setting up additional permissions..."
gcloud projects add-iam-policy-binding "$PROJECT_ID" \
  --member="serviceAccount:$API_SERVICE_ACCOUNT" \
  --role="roles/storage.admin"

echo "Setup completed successfully!"
echo ""
echo "Next steps:"
echo "1. Make sure you have the GEMINI_API_KEY set in your Cloud Run service"
echo "2. The service will be accessible at the URL shown above"