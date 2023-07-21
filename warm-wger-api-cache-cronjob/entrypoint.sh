#!/bin/bash
set -e

gcloud auth activate-service-account --key-file=/opt/service-account/key.json
gcloud pubsub topics publish --project=fitcentive-dev-03 warm-wger-api-cache --message="{\"topic\":\"warm-wger-api-cache\",\"payload\":{\"message\":\"Warm wger API cache now\"},\"id\":\"b505fa53-1ca7-41f8-a0e9-a746882b9601\"}"

exec "$@"