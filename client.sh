#!/usr/bin/env bash
# convenience script to run Server
# accepts the same arguments as Server.Server#main

java -cp lib/*:. \
     -Djava.rmi.server.codebase=http://${HOSTNAME}:${PWD}/lib/* \
     -Djava.rmi.server.hostname=$HOSTNAME \
     Client.Client \
     "$@" # rest of args
