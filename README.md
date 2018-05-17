# Cloud Foundry Application Inventory Report

This is a [Spring Cloud Task](http://cloud.spring.io/spring-cloud-task/) that employs the Reactive support in both the [Cloud Foundry Java Client](https://github.com/cloudfoundry/cf-java-client) and [Spring Boot Starter Data Mongodb](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.reactive) libraries to generate custom application inventory detail and summary reports from a target foundation.  An email will be sent to recipient(s) with those reports attached. 


## Clone

```
git clone https://github.com/pacphi/cf-get-app-details.git
```

## How to configure

Edit the contents of the `application.yml` file located in `src/main/resources`.  You will need to provide administrator credentials to Apps Manager for the foundation if you want to get a complete inventory of applications. 

> You really should not bundle configuration with the application. To take some of the sting away, you might consider externalizing and encrypting this configuration.

At a minimum you should supply values for the following keys

* `cf.apiHost` - a PCF foundation endpoint
* `cf.username` - typically an Apps Manager administrator account
* `cf.password`
* `spring.mail.host` - an SMTP host
* `spring.mail.username` - an email account
* `spring.mail.password`
* `spring.mail.recipients` - email accounts that will be sent an email with a CSV attachment

## How to Build

```
./gradlew build
```

## How to Run

```
./gradlew bootRun
```

## How to deploy to Cloud Foundry

Authenticate to a foundation using the API endpoint. E.g., to login to Pivotal Web Services (PWS).

```
cf login -a https:// api.run.pivotal.io
```

Push the app disabling health check and routing.

```
cf push get-app-details-task --no-route --health-check-type none -p ./build/libs/cf-get-app-details-0.0.1-SNAPSHOT.jar -m 1G
```


## How to run as a task on Cloud Foundry

To run the task

```
cf run-task get-app-details-task ".java-buildpack/open_jdk_jre/bin/java org.springframework.boot.loader.JarLauncher"
```

To validate that the task ran successfully

```
cf logs get-app-details-task --recent
```


## How to schedule the task on Cloud Foundry

Let's employ the [job scheduler](https://docs.pivotal.io/pcf-scheduler/1-1/using.html).

Create the service instance

```
cf create-service scheduler-for-pcf standard get-app-details-job
```

Bind the service instance to the task

```
cf bind-service get-app-details-task get-app-details-job
```

You'll need the Pivotal Cloud Foundry [job scheduler plugin for the cf CLI](https://network.pivotal.io/products/p-scheduler-for-pcf). Once the cf CLI plugin is installed, you can create jobs.

```
cf create-job get-app-details-task get-app-details-scheduled-job ".java-buildpack/open_jdk_jre/bin/java org.springframework.boot.loader.JarLauncher"
```

To execute the job

```
cf run-job get-app-details-scheduled-job
```

To adjust the schedule for the job using a CRON-like expression (`MIN` `HOUR` `DAY-OF-MONTH` `MONTH` `DAY-OF-WEEK`)

```
cf schedule-job get-app-details-scheduled-job "0 8 ? * * "
```

Consult the [User Guide](https://docs.pivotal.io/pcf-scheduler/1-1/using-jobs.html) for other commands.

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
Please find attached application inventory detail and summary reports from api.run.pivotal.io generated 2018-05-17T12:25:25.169.
```

### Attachments

#### Detail

Sample `app-inventory-detail.csv`

```
organization,space,application name,buildpack,stack,running instances,total instances,urls,last pushed,current state
Northwest,cphillipson,grivet-standalone,java,cflinuxfs2,0,1,grivet-standalone.cfapps.io,2017-06-28T13:50:18,stopped
Northwest,cphillipson,cloud-native-spring,java,cflinuxfs2,0,1,cloud-native-spring-laboured-estragon.cfapps.io,2017-12-13T12:54:18,stopped
Northwest,cphillipson,cloud-native-spring-ui,java,cflinuxfs2,0,1,cloud-native-spring-ui-riskiest-ghee.cfapps.io,2017-12-13T13:56:26,stopped
Northwest,cphillipson,env,dotnet,cflinuxfs2,0,1,env-overexuberant-nonopposition.cfapps.io,2017-12-11T19:50:29,stopped
```

#### Summary

Sample `app-inventory-summary.csv`

```
buildpack,total
java,20
dotnet,6
staticfile,2
nodejs,3
ruby,1

total applications: 34
total application instances: 52
```

## Credits

Tip of the hat to those who've gone before...

* Baeldung [1](http://www.baeldung.com/spring-email), [2](http://www.baeldung.com/spring-events)
* [John Thompson](https://springframework.guru/spring-data-mongodb-with-reactive-mongodb/)
* [Josh Long](https://github.com/joshlong/cf-task-demo)
* [Mohit Sinha](https://github.com/mohitsinha/spring-boot-webflux-reactive-mongo)
* [Pas Apicella](http://theblasfrompas.blogspot.com/2017/03/run-spring-cloud-task-from-pivotal.html)
* [Robert Watkins](https://gist.github.com/twasink/3073710)
