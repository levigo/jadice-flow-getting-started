#!/usr/bin/env bash

set -eu pipefail ;

_login_docker_registry(){
  echo ">>>[start-compose] login to levigo container registry" ;
  docker login registry.jadice.com ;
  return 0 ;
} ;

_configure_container_mounts(){
  echo ">>>[start-compose] configure container mounts" ;
  mkdir -p ./mariadb-data/ ;
  mkdir -p ./minio-data/ ;
  return 0 ;
} ;

_start_docker_compose_stack(){
  echo ">>>[start-compose] start docker-compose stack and follow logs" ;
  echo ">>>[start-compose] (You can disconnect from logs with Ctrl+C without stopping containers)" ;
  docker-compose up -d ;
  docker-compose logs -f
  return 0 ;
} ;


_main() {
  _login_docker_registry ;
  _configure_container_mounts ;
  _start_docker_compose_stack ;
  return 0 ;
}

_main ;
