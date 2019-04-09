# Payment SDK Backend
[![Travis CI build status](https://travis-ci.com/mobilabsolutions/payment-sdk-backend-open.svg?branch=master)](https://travis-ci.com/mobilabsolutions/payment-sdk-backend-open)

This repository contains code and documentation related to the backend component of the Payment SDK system. 

## Requirements

- Preferred IDE
- JDK 8 or later
- Maven

## Building the project

Payment SDK is using `ktlint` formatter, so first you should format all the files by running the following command from the project root folder:
```
mvn antrun:run@ktlint-format
```

After that, you can build the package simply by running this command:

```
mvn clean install
```

The resulting jar file will be produced in the directory named `target`.

## Starting the service locally

You should run `docker-compose up` from `payment-ws` folder to start the following services :
- **PostgreSQL** - listens on port 5432, username:password - `payment:payment`
- **payment-ws** - listens on port 8080

To shutdown the services gracefully run `ctrl+c`. To reset the data of the environment run `docker-compose down`.

If you want to create a database instance on your own, you will need to set the configuration properties below. You should either put them in your local `application-properties.local`, or have the environment variable defined:

```
- spring.datasource.url: DB url
- spring.datasource.username: DB username
- spring.datasource.password: DB password
- spring.jpa.show-sql=true
- authorization.server.signingKey: oauth signing key
  
- initial.data.loader.oauthClientId: oauth client id
- initial.data.loader.oauthClientPassword: oauth client password
- initial.data.loader.adminUsername: admin username
- initial.data.loader.adminPassword: admin password
```
 
After that, you can run the app directly from Maven using the Spring Boot plugin by simply running the following command:

```
mvn spring-boot:run -Dspring.profiles.active=local
```

You can now access Payment SDK here: http://localhost:8080/ 

## Request authentication

In Payment SDK there are `secret` and `publishable` keys, which should be generated for merchants. Those keys will later be used for requests authentication.

Publishable key is used for authentication of alias related requests:
- Create alias
- Exchange alias
- Delete alias

Secret key is used for authentication of transaction related requests:
- Preauthorization
- Capture
- Authorization
- Reversal
- Refund

## Idempotency

Payment SDK uses a concept of idempotency both for aliases and transactions. Idempotent operation is the one that produces the same result, no matter how many times is called. Idempotency is performed by sending an `Idempotent-Key` a header for `Create Alias`, `Preauthorization`, `Authorization` and `Refund` request. This will provide that the same alias cannot be added more than once, or that the transaction will not be performed more times, if unintentionally called.

## Feedback

The Payment SDK Backend is in active development, we welcome your feedback! Please use [GitHub Issues](https://github.com/mobilabsolutions/payment-sdk-backend-open/issues) to report any issues or give a feedback.

## Documentation

- [Overall documentation](https://github.com/mobilabsolutions/payment-sdk-wiki-open/wiki)
- [Backend Wiki](https://github.com/mobilabsolutions/payment-sdk-backend-open/wiki)
- [API Documentation](https://payment-dev.mblb.net/api/v1/swagger-ui.html)
