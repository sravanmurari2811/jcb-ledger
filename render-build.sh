#!/usr/bin/env bash
# exit on error
set -o errexit

# Build the backend module
./gradlew :backend:build -x test
