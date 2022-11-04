# LDES Workbench NiFi

The workbench is a customized [Apache NiFi](https://nifi.apache.org) docker image containing processors built by VSDS to allow processing of data that is intended to be ingested by an LDES server.

- [Available Processors](#available-processors)
    - [Ldes Client](#ldes-client)
    - [NGSI V2 to LD](#ngsi-v2-to-ld)
    - [NGSI LD to LDES](#ngsi-ld-to-ldes)
- [Deliverables](#deliverables)
    - [NiFi nar packages](#nifi-nar-packages)
    - [NiFi docker](#nifi-docker)
    

## Available Processors

### LDES Client

The LDES Client Processor is a Nifi wrapper around the [LDES Client SDK](https://github.com/Informatievlaanderen/VSDS-LDESClient4J) which enables you to follow and synchronize with a Linked Data Event Stream.

Further details on the usage of the the Client can be found on the [dedicated readme](./ldes-client-wrappers-nifi/README.md).

### NGSI V2 To LD

To support the ingestion of input data in [NGSI V2 format](https://fiware-tutorials.readthedocs.io/en/stable/getting-started/), 
the NgsiV2ToLd processor will take the inputted NGSI V2 data and transform it to [NGSI LD](https://vloca-kennishub.vlaanderen.be/NGSI_(LD)).

Further information on how to use this processor can be found in the in-NiFi documentation.

### NGSI LD to LDES

To support the ingestion of input in [NGSI LD format](https://vloca-kennishub.vlaanderen.be/NGSI_(LD)), the NgsiLdToLdes 
processor will take the inputted NGSI LD data and transform it into LDES members to be ingested into an LDES server.

Further information on how to use this processor can be found in the in-NiFi documentation.

## Deliverables

### NiFi nar packages

The above-mentioned processors are being packaged as individual nar files and can be manually added to a 
locally running NiFi instance by adding it to the /lib directory.

### NiFi Docker 

Additionally, a docker file is provided to avoid setting up a Nifi instance which contains a NiFi instance 
(based on the apache/nifi docker) and the LDES Client processor NAR files. The docker doesn't contain any workflows, these can be uploaded manually.

```shell
docker run --name ldes-workbench \
  -p 8443:8443 \
  -d \
  -e SINGLE_USER_CREDENTIALS_USERNAME=workbench \
  -e SINGLE_USER_CREDENTIALS_PASSWORD=b677c5c07bf83f5742c815a04fd48b88372ed19f0e06c4d2f0bc5997ab4f0470 \
  ghcr.io/informatievlaanderen/ldes-workbench-nifi:latest
```