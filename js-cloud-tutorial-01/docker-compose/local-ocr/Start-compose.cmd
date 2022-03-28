@echo off
set JS_DC_PATH=.
set JS_DOCKER_REGISTRY_PROXY=""
set JS_DOCKER_REGISTRY_JADICE=registry.jadice.com/
set JS_DIRECTOR_ACCESS_TOKEN=THE-JADICE-SERVER-ACCESS-TOKEN
set MINIO_ENDPOINT=YOUR-IP
set MINIO_PORT=9000
set MINIO_ACCESS_KEY=UXBQvo9GfxWqzeInxLalvRWpEXAMPLEKEY
set MINIO_SECRET_KEY=ki0BOUQn7XTfJphbXvG59kEbLsy89V5DkOoH7A6bEvwI6qzfEXAMPLEKEY
set MINIO_PROTOCOL=http

echo Starting docker compose for jadice server with OCR

docker login %JS_DOCKER_REGISTRY_JADICE%

IF NOT EXIST mariadb-data mkdir mariadb-data
IF NOT EXIST minio-data mkdir minio-data

docker-compose up
