FROM google/cloud-sdk:391.0.0-slim

COPY gcloud-config /root/.config/gcloud

ADD entrypoint.sh /entrypoint.sh

RUN chmod 755 /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]