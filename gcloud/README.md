# Google Cloud Run Deploy

## Prerequisites
- gcloud authenticated as `vrgames700@gmail.com`
- active project set (`gcloud config set project <PROJECT_ID>`)
- APIs enabled:
  - run.googleapis.com
  - cloudbuild.googleapis.com
  - artifactregistry.googleapis.com

## Deploy Order
1. `./gcloud/deploy-access-profile-service.ps1`
2. `./gcloud/deploy-gig-marketplace-service.ps1`
3. `./gcloud/deploy-chat-notification-service.ps1`
4. Update `_local-gcloud-config/pulls-service.env.yaml` with real Cloud Run URLs
5. `./gcloud/deploy-pulls-service.ps1`

Or run all:
`./gcloud/deploy-all.ps1`

All commands read env vars from `_local-gcloud-config/*.env.yaml`.

## Chat EDA config
- Bucket: `gigu-external-config`
- Object: `prod/eda-pubsub-config.json`
- Cloud Run env vars:
  - `EDA_CONFIG_BUCKET`
  - `EDA_CONFIG_OBJECT`
  - `PUBSUB_PUSH_TOKEN`
  - `GCP_PROJECT_ID`
- Required IAM on the Cloud Run service account:
  - `roles/storage.objectViewer`
- Pub/Sub and WebSocket use the Cloud Run service identity and GCS config file, not committed service account JSON keys.
