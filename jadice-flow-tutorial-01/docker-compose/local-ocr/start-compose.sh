#!/usr/bin/env bash

set -eu pipefail ;

_login_docker_registry(){
  echo ">>>[start-compose] login to levigo container registry" ;
  docker login registry.jadice.com ;
  return 0 ;
} ;

_configure_container_mounts(){
  echo ">>>[start-compose] configure container mounts" ;
  local _sudo="" ;
  local _uid="$(id -u)" ;
  if [[ ! "${_uid}" == "0" ]] ; then
    _sudo="sudo"
  fi ;
  ${_sudo} mkdir -p ./eureka-data/ ;
  ${_sudo} chown -R ${_uid}:538446 ./controller-config/ ;
  ${_sudo} chown -R ${_uid}:538446 ./worker-config/ ;
  ${_sudo} chown -R ${_uid}:0 ./eureka-config/ ;
  ${_sudo} chown -R ${_uid}:0 ./eureka-data/ ;

  return 0 ;
} ;

_start_docker_compose_stack(){
  echo ">>>[start-compose] start docker-compose stack" ; # and follow logs" ;
  echo ">>>[start-compose] you can follow the logs with 'docker compose logs -f'" ;
  #echo ">>>[start-compose] you can disconnect from logs with Ctrl+C without stopping containers" ;
  docker compose up -d ;
  #docker compose logs -f
  return 0 ;
} ;


_main() {
  _login_docker_registry ;
  _configure_container_mounts ;
  _start_docker_compose_stack ;
  return 0 ;
}

_main ;
