FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY shared/pom.xml shared/pom.xml
COPY modules/catalog/pom.xml modules/catalog/pom.xml
COPY modules/identity/pom.xml modules/identity/pom.xml
COPY modules/promotion/pom.xml modules/promotion/pom.xml
COPY modules/finance/pom.xml modules/finance/pom.xml
COPY modules/booking/pom.xml modules/booking/pom.xml
COPY modules/communication/pom.xml modules/communication/pom.xml
COPY keycloak/providers/user-storage/pom.xml keycloak/providers/user-storage/pom.xml
COPY app/pom.xml app/pom.xml
RUN ./mvnw -B -ntp -pl app -am -DskipTests dependency:go-offline

COPY shared/src shared/src
COPY modules modules
COPY app/src app/src
RUN ./mvnw -B -ntp -pl app -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S ridebook && adduser -S ridebook -G ridebook
COPY --from=build --chown=ridebook:ridebook /workspace/app/target/*-executable.jar app.jar
USER ridebook
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
