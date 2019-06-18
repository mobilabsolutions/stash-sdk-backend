# Payment SDK Backend
[![Build Status](https://travis-ci.com/mobilabsolutions/payment-sdk-backend-open.svg?token=eZip4D1t6wvFGqNxU2ki&branch=master)](https://travis-ci.com/mobilabsolutions/payment-sdk-backend-open)

This repository contains the code and the documentation related to the backend component of the Payment SDK system. 

## Requirements

- Preferred IDE
- JDK 8 or later
- Maven

## Building the project

The Payment SDK is using `ktlint` formatter. You should first format all the files by running the following command from the project root folder:
```
mvn antrun:run@ktlint-format
```

After that, you can build the package simply by running this command:

```
mvn clean install
```

The resulting jar file will be produced in the directory named `target`.

## Starting the service locally

You should run `docker-compose up` from the `payment-ws` folder to start the following services :
- **PostgreSQL** - listens on port 5432, username:password - `payment:payment`
- **payment-ws** - listens on port 8080

To shutdown the services gracefully run `ctrl+c`. To reset the data of the environment run `docker-compose down`.

If you want to create a database instance on your own, you will need to set the configuration properties below. You can either put them in your local `application-properties.local`, or define the environment variables:

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

You can now access the Payment SDK here: http://localhost:8080/ 

## Request authentication

In the Payment SDK there are the `secret` and `publishable` keys that should be generated for the merchants. These keys will later be used for the authentication requests.

The publishable key is used to authenticate the alias registration requests:
- Create alias
- Exchange alias

The secret key is used to authenticate the transactions requests and the alias deletion:
- Preauthorization
- Capture
- Authorization
- Reversal
- Refund
- Delete alias

## Idempotency

The Payment SDK uses a concept of idempotency for both aliases and transactions. The idempotent operation is the one that produces the same result no matter how many times it is called. The idempotency is performed by sending an `Idempotent-Key` in the header for `Create Alias`, `Preauthorization`, `Authorization` and `Refund` requests. This will avoid adding the same alias more than once or to perform the same transaction several times if unintentionally called.

When a request comes with a new idempotent key, the key and the request body are stored in the Payment SDK backend. If the other request comes with the same idempotent key and the same body, the original response is returned. However, if the other request has the same idempotent key as the original one, but the different body, an appropriate error will be returned.

## Feedback

The Payment SDK Backend is in active development. We welcome your feedback! Please write us at payment-sdk@mobilabsolutions.com to report any issues or give a feedback.

## Documentation

- [Overall documentation](https://github.com/mobilabsolutions/payment-sdk-wiki-open/wiki)
- [Backend Wiki](https://github.com/mobilabsolutions/payment-sdk-backend-open/wiki)
- [API Documentation](https://payment-dev.mblb.net/api/v1/swagger-ui.html)
