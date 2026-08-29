#!/bin/sh
set -eu

psql -v ON_ERROR_STOP=1 \
    --username "$POSTGRES_USER" \
    --dbname "$POSTGRES_DB" \
    --set=keycloak_database="$KEYCLOAK_DB_NAME" \
    --set=keycloak_user="$KEYCLOAK_DB_USERNAME" \
    --set=keycloak_password="$KEYCLOAK_DB_PASSWORD" <<-'SQL'
CREATE ROLE :"keycloak_user" LOGIN PASSWORD :'keycloak_password';
CREATE DATABASE :"keycloak_database" OWNER :"keycloak_user";
SQL
