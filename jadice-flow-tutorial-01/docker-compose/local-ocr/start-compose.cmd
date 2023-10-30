@echo off
echo Starting docker compose for jadice flow with OCR
echo Login to levigo container registry
docker login registry.jadice.com

IF NOT EXIST eureka-data mkdir eureka-data

docker-compose --env-file .env up
