#!/usr/bin/env bash

set -e

if [ $# -ne 1 ]; then
    echo "Usage: ./deploy.sh {pivotal cloud foundry api endpoint}"
    exit 1
fi

cf login -a $1

cf push get-app-details-task --no-route --health-check-type none -p ./build/libs/cf-get-app-details-0.0.1-SNAPSHOT.jar -m 1G --no-start
cf set-env get-app-details-task SPRING_PROFILES_ACTIVE jdbc
cf start get-app-details-task
cf run-task get-app-details-task ".java-buildpack/open_jdk_jre/bin/java org.springframework.boot.loader.JarLauncher"
cf create-service scheduler-for-pcf standard get-app-details-job
cf bind-service get-app-details-task get-app-details-job
cf create-job get-app-details-task get-app-details-scheduled-job ".java-buildpack/open_jdk_jre/bin/java org.springframework.boot.loader.JarLauncher"
cf run-job get-app-details-scheduled-job
cf schedule-job get-app-details-scheduled-job "0 8 ? * * "
