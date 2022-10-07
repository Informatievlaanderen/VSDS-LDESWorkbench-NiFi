# syntax=docker/dockerfile:1

ARG NIFI_DOCKER_IMAGE_VERSION

FROM ghcr.io/informatievlaanderen/nifi:${NIFI_DOCKER_IMAGE_VERSION}
ADD --chown=nifi:nifi *.nar /opt/nifi/nifi-current/lib/
RUN chmod -R 664 /opt/nifi/nifi-current/lib/*.nar
USER nifi
