# Pivotal Application Service > Application Inventory Report

[![Build Status](https://travis-ci.org/pacphi/cf-app-inventory-report.svg?branch=app-deploy)](https://travis-ci.org/pacphi/cf-app-inventory-report) [![Known Vulnerabilities](https://snyk.io/test/github/pacphi/cf-app-inventory-report/badge.svg)](https://snyk.io/test/github/pacphi/cf-app-inventory-report)

This is a Spring Boot application that employs the Reactive support in both the [Pivotal Application Service Java Client](https://github.com/cloudfoundry/cf-java-client) and your choice of either [Spring Boot Starter Data Mongodb](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.reactive) or [rxjava2-jdbc](https://github.com/davidmoten/rxjava2-jdbc) with an [H2](http://www.h2database.com/html/main.html) backend.  These libraries are employed to generate custom application inventory detail and summary reports from a target foundation.  An email will be sent to recipient(s) with those reports attached on a scheduled basis. 

## Prerequisites

Required

* [Pivotal Application Service](https://pivotal.io/platform/pivotal-application-service) account 

Optional

* Email account
* SMTP Host
* [SendGrid](https://sendgrid.com/pricing/) account 

## Tools

* [git](https://git-scm.com/downloads) 2.17.1 or better
* [JDK](http://openjdk.java.net/install/) 8u162 or better
* [cf](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html) CLI 6.37.0 or better

## Clone

```
git clone https://github.com/pacphi/cf-app-inventory-report.git
```

## How to configure

Edit the contents of the `application.yml` file located in `src/main/resources`.  You will need to provide administrator credentials to Apps Manager for the foundation if you want to get a complete inventory of applications. 

> You really should not bundle configuration with the application. To take some of the sting away, you might consider externalizing and encrypting this configuration.

### Minimum required keys

At a minimum you should supply values for the following keys

* `cf.apiHost` - a Pivotal Application Service API endpoint
* `cf.username` - a Pivotal Application Service account username (typically an administrator account)
* `cf.password` - a Pivotal Application Service account password
* `notification.engine` - email provider, options are: `none`, `java-mail` or `sendgrid`

> If you set the email provider to `none`, then no email will be delivered

### for java-mail

* `spring.mail.host` - an SMTP host
* `spring.mail.username` - an email account username
* `spring.mail.password` - an email account password
* `mail.from` - originator email address 
* `mail.recipients` - email addresses that will be sent an email with CSV attachments

### for sendgrid

* `spring.sendgrid.api-key` - an api key for your SendGrid account
* `mail.from` - originator email address 
* `mail.recipients` - email addresses that will be sent an email with CSV attachments

### to choose between backends

Set `spring.profiles.active` to one of either `mongo` or `jdbc`.

E.g., you could start the app with an H2 backend using

```
./gradlew bootRun -Dspring.profiles.active=jdbc
```

### to override the default download URL for Embedded Mongo

On application start-up, a versioned Mongo executable is downloaded from a default location (addressable from the public internet).  If you would like to download the executable from an alternate location and/or select an alternate version, add the following:

* `spring.mongodb.embedded.verson` - version of the Mongo executable (e.g., `3.4.15`)
* `spring.mongodb.embedded.download.path` - the path to the parent directory hosting OS-specific sub-directories and version(s) of Mongo executables (e.g., `https://fastdl.mongodb.org/`)
* `spring.mongodb.embedded.download.alternate` - this is a boolean property and must be set to true to activate alternate download URL

As an example, the following

```
spring:
  mongodb:
    embedded:
      version: 3.4.15
      download:
        path: https://fastdl.mongodb.org/
        alternate: true
```

would download the Mongo executable from `https://fastdl.mongodb.org/osx/mongodb-osx-x86_64-3.4.15.tgz` when the app is running on a Mac OSX host.

> OS-specific sub-directory choices are: `linux`, `win32`, and `osx`. See [https://www.mongodb.com/download-center#community](https://www.mongodb.com/download-center#community) for more details.

### to set the delivery Schedule

Update the value of the `cron` property in `application.yml`.  Consult this [article](https://www.baeldung.com/spring-scheduled-tasks) and the [Javadoc](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html#cron--) to understand how to tune it for your purposes.

## How to Build

```
./gradlew build
```

## How to Run

```
./gradlew bootRun -Dspring.profiles.active={backend_provider}
```
where `{backend_provider}` is either `mongo` or `jdbc`

> You'll need to manually stop to the application with `Ctrl+C`

## How to deploy to Pivotal Application Service

You may choose to follow the step-by-step instructions below or execute a couple of shell scripts to [deploy](deploy.sh) and/or [shutdown/destroy](destroy.sh) the application.

Authenticate to a foundation using the API endpoint. 
> E.g., login to [Pivotal Web Services](https://run.pivotal.io)

```
cf login -a https:// api.run.pivotal.io
```

Push the app, but don't start it, also disable health check and routing.

```
cf push get-app-inventory-task -p ./build/libs/cf-app-inventory-report-0.1-SNAPSHOT.jar -m 1G --no-start
```

Set environment variable for backend

> You have a choice of backends, either `jdbc` or `mongo`

```
cf set-env get-app-inventory-task SPRING_PROFILES_ACTIVE jdbc
```

Start the app

```
cf start get-app-inventory-task
```

## What does the task do?

Utilizes cf CLI to query foundation for application details across all organizations and spaces for which the account is authorized.  Generates an email with a couple of attachments, then sends a copy to each recipient.

### Subject

Sample 

```
PCF Application Inventory Report
```

### Body

Sample 

```
Please find attached application inventory detail and summary reports from api.run.pivotal.io generated 2018-05-30T10:55:08.247.
```

### Attachments

#### Detail

Sample `app-inventory-detail.csv`

```
organization,space,application name,buildpack,image,stack,running instances,total instances,urls,last pushed,last event,last event actor,requested state
Northwest,cphillipson,cloud-native-spring-ui,,java,cflinuxfs2,0,1,cloud-native-spring-ui-riskiest-ghee.cfapps.io,2017-12-13T15:56:26,,,stopped
Northwest,sdeeg,nodefire-smd,nodejs,,cflinuxfs2,1,1,nodefire-smd.cfapps.io,2018-05-03T17:14:10,,,started
Northwest,cphillipson,cloud-native-spring,java,,cflinuxfs2,0,1,cloud-native-spring-laboured-estragon.cfapps.io,2017-12-13T14:54:18,,,stopped
zoo-labs,test,cphillipson-ruby-demo,ruby,,cflinuxfs2,1,1,cphillipson-ruby-demo.cfapps.io,2018-05-29T13:25:36,audit.app.droplet.create,cphillipson@pivotal.io,started
zoo-labs,test,cook,java,,cflinuxfs2,0,1,cook-impressive-bonobo.cfapps.io,2018-05-10T16:48:18,audit.app.stop,cphillipson@pivotal.io,stopped
Northwest,nthomson,bookstore-connector,java,,cflinuxfs2,1,1,bookstore-connector.cfapps.io,2018-04-09T16:40:23,,,started
Northwest,nthomson,bookstore-service-broker,java,,cflinuxfs2,1,1,bookstore-service-broker.cfapps.io,2018-04-09T12:49:41,,,started
...
```

#### Summary

Sample `app-inventory-summary.csv`

```
organization,total
Northwest,32
zoo-labs,4

buildpack,total
java,25
unknown,5
staticfile,2
dotnet,2
ruby,1
nodejs,1

last pushed,application total
<= 1 day,2
> 1 day <= 1 week,11
> 1 week <= 1 month,6
> 1 month <= 3 months,3
> 3 months <= 6 months,6
> 6 months <= 1 year,8
> 1 year,0

state,instance total
started,14
stopped,30
all,44

Total applications: 36
```

## Credits

Tip of the hat to those who've gone before...

* Baeldung [1](http://www.baeldung.com/spring-email), [2](http://www.baeldung.com/spring-events)
* [John Thompson](https://springframework.guru/spring-data-mongodb-with-reactive-mongodb/)
* [Josh Long](https://github.com/joshlong/cf-task-demo)
* [Mohit Sinha](https://github.com/mohitsinha/spring-boot-webflux-reactive-mongo)
* [Pas Apicella](http://theblasfrompas.blogspot.com/2017/03/run-spring-cloud-task-from-pivotal.html)
* [Robert Watkins](https://gist.github.com/twasink/3073710)
* [David Moten](https://github.com/davidmoten/rxjava2-jdbc)
* [Robert B Roeser](https://medium.com/netifi/spring-webflux-and-rxjava2-jdbc-83a94e71ba04)