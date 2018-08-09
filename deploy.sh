#!/usr/bin/env bash

set -e

if [ $# -ne 1 ]; then
    echo "Usage: ./deploy.sh {pivotal cloud foundry api endpoint}"
    exit 1
fi

cf login -a $1

cf push get-app-inventory-task --no-route --health-check-type none -p ./build/libs/cf-app-inventory-report-0.1-SNAPSHOT.jar -m 1G --no-start
cf set-env get-app-inventory-task SPRING_PROFILES_ACTIVE jdbc
cf start get-app-inventory-task
cf run-task get-app-inventory-task ".java-buildpack/open_jdk_jre/bin/java org.springframework.boot.loader.JarLauncher"
cf create-service scheduler-for-pcf standard get-app-inventory-job
cf bind-service get-app-inventory-task get-app-inventory-job
cf create-job get-app-inventory-task get-app-inventory-scheduled-job ".java-buildpack/open_jdk_jre/bin/java org.springframework.boot.loader.JarLauncher"
cf run-job get-app-inventory-scheduled-job
cf schedule-job get-app-inventory-scheduled-job "0 8 ? * * "
