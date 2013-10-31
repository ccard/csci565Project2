#!/usr/bin/env bash

rm log1*.log
java -cp lib/*:. testClientMethods $1
