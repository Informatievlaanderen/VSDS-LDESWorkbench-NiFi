# syntax=docker/dockerfile:1

#
# INSTALL MAVEN DEPENDENCIES
#
FROM maven:3.8.5-openjdk-11 AS builder

# Build custom processors
FROM builder as app-stage
RUN git clone https://github.com/Informatievlaanderen/VSDS-LDESClient4J.git
RUN cd VSDS-LDESClient4J; mvn clean install -Dmaven.compiler.target=11 -Dmaven.compiler.source=11 -DskipTests

COPY . /
RUN mvn net.revelc.code.formatter:formatter-maven-plugin:format install -Dmaven.compiler.target=11 -Dmaven.compiler.source=11 -DskipTests

# Build Apache Nifi with custom processors
FROM apache/nifi:1.19.1
COPY --chown=nifi:nifi --from=app-stage /ldes-client-processor/target/*.nar /opt/nifi/nifi-current/lib/
COPY --chown=nifi:nifi --from=app-stage /ngsiv2-to-ngsi-ld-processor/target/*.nar /opt/nifi/nifi-current/lib/
COPY --chown=nifi:nifi --from=app-stage /ngsi-ld-to-ldes-processor/target/*.nar /opt/nifi/nifi-current/lib/
RUN rm -rf *.db *.db-* ldes-client-processor/*.db ldes-client-processor/*.db-*
RUN chmod -R 664 /opt/nifi/nifi-current/lib/*.nar
USER nifi
