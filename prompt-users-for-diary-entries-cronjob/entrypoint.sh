#!/bin/bash
set -e

gcloud auth activate-service-account --key-file=/opt/service-account/key.json
gcloud pubsub topics publish --project=fitcentive-dev-02 prompt-users-for-diary-entries --message="{\"topic\":\"prompt-users-for-diary-entries\",\"payload\":{\"message\":\"Send push notification to all users to prompt them to enter diary entries now\"},\"id\":\"dbb8cbdf-62fd-40a4-b710-67c90141f3e3\"}"

exec "$@"