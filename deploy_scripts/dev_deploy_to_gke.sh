#!/bin/bash

kubectl delete -n diary deployment/diary-service

# Delete old 1.0 image from gcr
echo "y" | gcloud container images delete gcr.io/fitcentive-dev-03/diary:1.0 --force-delete-tags

# Build and push image to gcr
sbt docker:publish

kubectl apply -f deployment/gke-dev-env/

cd warm-wger-api-cache-cronjob && \
  docker build -t gcr.io/fitcentive-dev-02/gcloud-diary-cron-pubsub-image:latest -t gcr.io/fitcentive-dev-02/gcloud-diary-cron-pubsub-image:1.0 . && \
  docker push gcr.io/fitcentive-dev-03/gcloud-diary-cron-pubsub-image:1.0