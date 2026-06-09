FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B package -DskipTests dependency:copy \
    -Dartifact=org.postgresql:postgresql:42.7.5 \
    -DoutputDirectory=target/docker-libs

FROM quay.io/wildfly/wildfly:35.0.1.Final-jdk21

ENV DB_HOST=postgres \
    DB_PORT=5432 \
    DB_NAME=helpdeskpro \
    DB_USER=helpdesk \
    DB_PASSWORD=helpdesk \
    JWT_SECRET=dev-helpdesk-pro-jwt-secret-change-me-32-bytes-minimum \
    JWT_EXPIRATION_MINUTES=120 \
    ADMIN_NAME="HelpDesk Admin" \
    ADMIN_EMAIL=admin@helpdeskpro.local \
    ADMIN_PASSWORD=admin123 \
    CORS_ALLOWED_ORIGINS=*

COPY --from=build /workspace/target/docker-libs/postgresql-42.7.5.jar /tmp/postgresql.jar
COPY docker/wildfly/configure.cli /opt/jboss/wildfly/bin/configure.cli
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/bin/configure.cli

COPY --from=build /workspace/target/helpdesk-pro.war /opt/jboss/wildfly/standalone/deployments/ROOT.war

EXPOSE 8080

CMD ["/bin/sh", "-c", "rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history/current /opt/jboss/wildfly/standalone/configuration/standalone_xml_history/snapshot && exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 --read-only-server-config=standalone.xml"]
