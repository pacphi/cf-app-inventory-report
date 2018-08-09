#!/usr/bin/env bash

set -e

cf delete-job get-app-inventory-scheduled-job
cf delete-service scheduler-for-pcf
cf delete get-app-inventory-task
