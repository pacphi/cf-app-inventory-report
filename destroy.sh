#!/usr/bin/env bash

set -e

cf stop get-app-inventory-task
cf delete get-app-inventory-task
