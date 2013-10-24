#!/usr/bin/env bash
# convenience script to run client
# accepts the same arguments as Client.Client#main

java -cp lib/*:. \
     -Djava.rmi.server.codebase=http://${HOSTNAME}:${PWD} \
     -Djava.rmi.server.hostname=$HOSTNAME \
     Server.Server \
     "$@" # rest of args
