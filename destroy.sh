#!/usr/bin/env bash

set -e

cf delete-job get-app-details-scheduled-job
cf delete-service scheduler-for-pcf
cf delete-task get-app-details-task