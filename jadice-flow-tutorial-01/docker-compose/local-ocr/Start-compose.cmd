@echo off
echo Starting docker compose for jadice flow with OCR
echo Login to levigo container registry
docker login registry.jadice.com

IF NOT EXIST mariadb-data mkdir mariadb-data
IF NOT EXIST minio-data mkdir minio-data

docker-compose --env-file .env up
