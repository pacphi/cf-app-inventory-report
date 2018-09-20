#!/usr/bin/env bash

set -e

export APP_NAME=cf-app-inventory-report

cf stop $APP_NAME
cf unbind-service $APP_NAME $APP_NAME-secrets
cf delete-service $APP_NAME-secrets -f
cf delete $APP_NAME -r -f
