# LDES Workbench NiFi

The workbench is a customized [Apache NiFi](https://nifi.apache.org) docker image containing processors built by VSDS to allow processing of data that is intended to be ingested by an LDES server.

- [Available Processors](#available-processors)
    - [Ldes Client](#ldes-client)
    - [NGSI V2 to LD](#ngsiv2-to-ngsi-ld-processor)
    - [NGSI LD to LDES](#ngsi-ld-to-ldes-processor)
- [Docker images](#docker-images)
    - [Workbench docker](#workbench-docker)
    - [Custom NiFi image](#custom-nifi-image)

## Available Processors

The processors are packaged as individual nar files and can be manually added to a 
locally running NiFi instance by adding them to the /lib directory.
The [workbench docker](#workbench-docker) already contains these packaged processors.

Available nar files:
- [Ldes Client](#ldes-client)
- [NGSI V2 to LD](#ngsiv2-to-ngsi-ld-processor)
- [NGSI LD to LDES](#ngsi-ld-to-ldes-processor)

### LDES Client

The LDES Client Processor is a Nifi wrapper around the [LDES Client SDK](https://github.com/Informatievlaanderen/VSDS-LDESClient4J) which enables you to follow and synchronize with a Linked Data Event Stream.

The resulting LDES members can then be further used in the workflow, for instance as input for an [InvokeHttp processor](https://nifi.apache.org/docs/nifi-docs/components/org.apache.nifi/nifi-standard-nar/1.17.0/org.apache.nifi.processors.standard.InvokeHTTP/index.html) that sends them to an [LDES server](https://github.com/Informatievlaanderen/VSDS-LDESServer4J) instance for ingestion.

Further details on the usage of the LDES Client Processor can be found [here](./ldes-client-processor/README.md).
General instructions on how to use NiFi processors can be found in the [NiFi documentation](https://nifi.apache.org/docs.html).

### NGSIv2 to NGSI-LD processor

A processor that translates [NGSI v2](https://fiware.github.io/specifications/ngsiv2/stable/) data to [NGSI-LD](https://www.etsi.org/deliver/etsi_gr/CIM/001_099/008/01.01.01_60/gr_CIM008v010101p.pdf) data using [the protocol deliver by fiware](https://fiware-datamodels.readthedocs.io/en/stable/ngsi-ld_howto/index.html#steps-to-migrate-to-json-ld)

Documentation is available [here](./ngsiv2-to-ngsi-ld-processor/README.md).
Further reading (in dutch): [NGSI LD (lang: nl)](https://vloca-kennishub.vlaanderen.be/NGSI_(LD))
General instructions on how to use NiFi processors can be found in the [NiFi documentation](https://nifi.apache.org/docs.html).

### NGSI-LD to LDES processor

To support the ingestion of input in [NGSI LD format](https://vloca-kennishub.vlaanderen.be/NGSI_(LD)), the NgsiLdToLdes 
processor will take the inputted NGSI LD data and transform it into LDES members to be ingested into an LDES server.

Further information on how to use this processor can be found in the NiFi documentation.

A processor that adds data elements required to turn the NGSI-LD stream into an LDES stream.

Documentation is available [here](./ngsi-ld-to-ldes-processor/README.md).
Further reading (in dutch): [NGSI LD (lang: nl)](https://vloca-kennishub.vlaanderen.be/NGSI_(LD))
General instructions on how to use NiFi processors can be found in the [NiFi documentation](https://nifi.apache.org/docs.html).

## Docker Images

The [VSDS workbench docker](https://github.com/Informatievlaanderen/VSDS-LDESWorkbench-NiFi/pkgs/container/ldes-workbench-nifi) is built from a [customised NiFi docker](https://github.com/Informatievlaanderen/VSDS-LDESWorkbench-NiFi/pkgs/container/nifi).

The docker doesn't contain any NiFi workflows, these can be uploaded by logging into the NiFi instance (by default: [http://localhost:8443](http://localhost:8443) or by using the [NiFi toolkit](https://hub.docker.com/r/apache/nifi-toolkit).

Instructions for local build are provided:
- [Workbench docker](#workbench-docker)
- [Custom NiFi image](#custom-nifi-image)


### Workbench docker

The workbench docker is built on top of a [custom NiFi image](#custom-nifi-image) with the [processors](#available-processors) provided by VSDS pre-packaged.

**try locally**

```shell
docker run --name ldes-workbench \
  -p 8443:8443 \
  -d \
  -e SINGLE_USER_CREDENTIALS_USERNAME=[username] \
  -e SINGLE_USER_CREDENTIALS_PASSWORD=[password consisting of up to 32 characters] \
  ghcr.io/informatievlaanderen/ldes-workbench-nifi:latest
```

**build locally**

```shell
git clone git@github.com:Informatievlaanderen/VSDS-LDESWorkbench-NiFi.git
cd VSDS-LDESWorkbench-NiFi/ldes-workbench-nifi
docker build \
  --build-arg NIFI_DOCKER_IMAGE_VERSION="1.17.0-jdk17" \
  -t ldes-workbench-nifi:local \
  -f Dockerfile
docker run --name ldes-workbench \
  -p 8443:8443 \
  -d \
  -e SINGLE_USER_CREDENTIALS_USERNAME=[username] \
  -e SINGLE_USER_CREDENTIALS_PASSWORD=[password consisting of up to 32 characters] \
  ldes-workbench-nifi:local
```

**github**

- github workflow: [1.b.pr_build-docker-workbench-nifi.yml](.github/workflows/1.b.pr_build-docker-workbench-nifi.yml)
- github package: [ghcr.io/informatievlaanderen/ldes-workbench-nifi](https://github.com/Informatievlaanderen/VSDS-LDESWorkbench-NiFi/pkgs/container/ldes-workbench-nifi)

The configuration used in the github workflow can be found below.

```yaml
  REGISTRY: ghcr.io
  NIFI_DOCKER_IMAGE_VERSION: 1.17.0-jdk17
  LDES_NIFI_DOCKER_IMAGE_NAME: ghcr.io/informatievlaanderen/ldes-workbench-nifi
```

Be sure to set the `SINGLE_USER_CREDENTIALS_USERNAME` and `SINGLE_USER_CREDENTIALS_PASSWORD` to allow login to the NiFi instance. When ommitted, the build will succeed, but values will be generated for you (logged to the console), making automation impossible.

### Custom NiFi image

The [workbench docker](#workbench-docker) uses a docker image with a customized [NiFi](https://nifi.apache.org) instance, built with JDK 17.

**building locally**

To build the custom NiFi image locally, follow these steps:

1. Fetch the official Apache NiFi image and extract it

```shell
curl -L https://github.com/apache/nifi/archive/refs/tags/rel/nifi-1.17.0.tar.gz | tar -zxv --strip-components=2 nifi-rel-nifi-1.17.0/nifi-docker/dockerhub/
```

2. Allow the dockerfile to find local context

```shell
sed -i 's/xmlstarlet procps$/xmlstarlet procps curl unzip/' dockerhub/Dockerfile
```

3. Build the docker image

```shell
docker build --build-arg IMAGE_TAG="${BASE_IMAGE_TAG}" --build-arg NIFI_VERSION="${NIFI_VERSION}" --build-arg DISTRO_PATH="${DISTRO_PATH}" -t "${NIFI_DOCKER_IMAGE_NAME}:${NIFI_DOCKER_IMAGE_VERSION}" -f dockerhub/Dockerfile dockerhub/
```

Environment variables can be taken from the github workflow and adjusted as desired.

**github**

- github workflow: [0.build_push-docker-nifi.yml](.github/workflows/0.build_push-docker-nifi.yml)
- github package: [ghcr.io/informatievlaanderen/nifi](https://github.com/Informatievlaanderen/VSDS-LDESWorkbench-NiFi/pkgs/container/nifi)

The configuration used in the github workflow can be found below.

```yaml
  REGISTRY: ghcr.io
  USERNAME: informatievlaanderen
  BASE_IMAGE_TAG: 17-slim-buster
  NIFI_VERSION: 1.17.0
  DISTRO_PATH: 1.17.0
  TARGET_IMAGE_TAG: 1.17.0-jdk17
  NIFI_IMAGE: nifi
  NIFI_DOCKER_IMAGE_NAME: ghcr.io/informatievlaanderen/nifi
  NIFI_DOCKER_IMAGE_VERSION: 1.17.0-jdk17
```
