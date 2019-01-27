# Pivotal Application Service > Application Inventory Report

[![Build Status](https://travis-ci.org/pacphi/cf-app-inventory-report.svg?branch=master)](https://travis-ci.org/pacphi/cf-app-inventory-report) [![Known Vulnerabilities](https://snyk.io/test/github/pacphi/cf-app-inventory-report/badge.svg)](https://snyk.io/test/github/pacphi/cf-app-inventory-report)

This is a Spring Boot application that employs the Reactive support in both the [Pivotal Application Service Java Client](https://github.com/cloudfoundry/cf-java-client) and your choice of either [Spring Boot Starter Data Mongodb](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.reactive) or [rxjava2-jdbc](https://github.com/davidmoten/rxjava2-jdbc) with an [HSQL](http://hsqldb.org) backend.  These libraries are employed to generate custom application inventory detail and summary reports from a target foundation.  It may optionally be configured to send an email to recipient(s) with those reports attached.

> While the cf-app-inventory-report does not take the place of an official foundation [Accounting Report](https://docs.pivotal.io/pivotalcf/2-4/opsguide/accounting-report-apps-man.html), it does provide a much more detailed snapshot of all the applications that were currently running at the time of collection.  The Accounting Report is focussed on calculating aggregates (on a monthly basis) such as: (a) the total hours of application instance usage and (b) the largest # of application instances running (a.k.a. maximum concurrent application instances).

## Prerequisites

Required

* [Pivotal Application Service](https://pivotal.io/platform/pivotal-application-service) account

Optional

* Email account
* SMTP Host
* [SendGrid](https://sendgrid.com/pricing/) account 

## Tools

* [git](https://git-scm.com/downloads) 2.20.1 or better
* [JDK](http://openjdk.java.net/install/) 11 or better
* [cf](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html) CLI 6.41.0 or better

## Clone

```
git clone https://github.com/pacphi/cf-app-inventory-report.git
```

## How to configure

Make a copy of then edit the contents of the `application.yml` file located in `src/main/resources`.  A best practice is to append a suffix representating the target deployment environment (e.g., `application-pws.yml`, `application-pcfone.yml`). You will need to provide administrator credentials to Apps Manager for the foundation if you want to get a complete inventory of applications.

> You really should not bundle configuration with the application. To take some of the sting away, you might consider externalizing and/or [encrypting](https://blog.novatec-gmbh.de/encrypted-properties-spring/) this configuration.

### Managing secrets

Place secrets in `config/secrets.json`, e.g.,

```
{
	"CF_API-HOST": "xxxxx",
	"CF_USERNAME": "xxxxx",
	"CF_PASSWORD": "xxxxx",
	"MAIL_FROM": "xxxxx",
	"MAIL_RECIPIENTS": "xxxxx",
	"SENDGRID_API-KEY": "xxxxx"
}
```

We'll use this file later as input configuration for the creation of either a [credhub](https://docs.pivotal.io/credhub-service-broker/using.html) or [user-provided](https://docs.cloudfoundry.org/devguide/services/user-provided.html#credentials) service instance.

> Replace occurrences of `xxxxx` above with appropriate values

### Minimum required keys

At a minimum you should supply values for the following keys

* `cf.apiHost` - a Pivotal Application Service API endpoint
* `token.provider` - Authorization token provider, options are: `userpass` or `sso`

Based on choice the authorization token provider

#### Username and password

* `cf.username` - a Pivotal Application Service account username (typically an administrator account)
* `cf.password` - a Pivotal Application Service account password

#### Single-sign on

* `cf.refreshToken` - the refresh token to be found within `~/.cf/config.json` after your authenticate

#### Email notification

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

If you edited the contents of `application.yml` then you could set `spring.profiles.active` to one of either `mongo` or `jdbc`.

E.g., you could start the app with an HSQL backend using

```
./gradlew bootRun -Dspring.profiles.active=jdbc
```

If you copied and appended a suffix to the original `application.yml` then you would set `spring.profiles.active` to be that suffix 

E.g., if you had a configuration file named `application-pws.yml`

```
./gradlew bootRun -Dspring.profiles.active=pws
```

> See the [samples](samples) directory for some examples of configuration when deploying to [Pivotal Web Services](https://login.run.pivotal.io/login) or [PCFOne](https://login.run.pcfone.io/login).

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


### Troubleshooting

To have access to a database management [console](http://hsqldb.org/doc/guide/running-chapt.html#rgc_access_tools) which would allow you to execute queries against the in-memory database, you will need to set an additional JVM argument.  

```
-Djava.awt.headless=false
```

> Note: this is not an available option when deploying to a PAS foundation.

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

### with Username and password authorization 

The following instructions explain how to get started when `token.provider` is set to `userpass`

Authenticate to a foundation using the API endpoint.
> E.g., login to [Pivotal Web Services](https://login.run.pivotal.io)

```
cf login -a https://api.run.pivotal.io
```

### with SSO authorization

The following instructions explain how to get started when `token.provider` is set to `sso`

Authenticate to a foundation using the API endpoint

> E.g., login to [PCF One](https://login.run.pcfone.io)

```
cf login -a https://api.run.pcfone.io -sso
```

Visit the link in the password prompt to retrieve a temporary passcode, then complete the login process

> E.g., `https://login.run.pcfone.io/passcode`)

Inspect the contents of `~/.cf/config.json` and copy the value of `RefreshToken`.

Paste the value as the value for `CF_REFRESH-TOKEN` in your `config/secrets.json`

```
{
  "TOKEN_PROVIDER": "sso",
  "CF_API-HOST": "xxxxx",
  "CF_REFRESH-TOKEN": "xxxxx",
}
```

### using scripts

Deploy the app (w/ a user-provided service instance vending secrets)

```
./deploy.sh
```

Deploy the app (w/ a Credhub service instance vending secrets)

```
./deploy.sh --with-credhub
```

Shutdown and destroy the app and service instances

```
./destroy.sh
```

## What does this task do?

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

## On-demand Endpoints

For additional convenenience REST endpoints have been exposed for on-demand reporting.  Report results are refreshed on the `cron` schedule mentioned above.

```
GET /report
```
> Produces `text/plain` output combining detail and summary application info

```
GET /users
```
> Produces `application/json` output containing user/role details for all orgs/spaces. 

```
GET /users/{organization}/{space}
```
> Produces `application/json` output containing user/role details for one org/space.

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
