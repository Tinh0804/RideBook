FROM maven:3.9.11-eclipse-temurin-21-alpine AS provider-build
WORKDIR /build
COPY keycloak/providers/user-storage/pom.xml ./pom.xml
COPY keycloak/providers/user-storage/src ./src
RUN mvn -q -DskipTests package

FROM quay.io/keycloak/keycloak:26.7.0
ENV KC_DB=postgres \
    KC_HEALTH_ENABLED=true \
    KC_METRICS_ENABLED=true
COPY --from=provider-build /build/target/ridebook-keycloak-user-storage.jar /opt/keycloak/providers/
RUN /opt/keycloak/bin/kc.sh build
