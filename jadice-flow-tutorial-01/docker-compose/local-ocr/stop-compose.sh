#!/usr/bin/env bash

set -eu pipefail ;

function _reset_ownership_to_current_user(){
  echo ">>>[remove-compose] Re-set ownership of mounted config files to the current user (necessary for editing)" ;
  local _sudo="" ;
  local _uid="$(id -u)" ;
  if [[ ! "${_uid}" == "0" ]] ; then
    _sudo="sudo"
  fi ;
  ${_sudo} chown -R "${_uid}:${_uid}"  ./controller-config/ ;
  ${_sudo} chown -R "${_uid}:${_uid}"  ./eureka-config/ ;
  ${_sudo} chown -R "${_uid}:${_uid}"  ./worker-config/ ;
  return 0 ;
}

_remove_docker_compose_stack(){
  echo ">>>[remove-compose] remove docker-compose stack" ;
  docker compose down ;
  return 0 ;
}

_main() {
  #_reset_ownership_to_current_user ;
  _remove_docker_compose_stack ;
  return 0 ;
}

_main ;
