# Stash Backend

[![Build Status](https://travis-ci.com/mobilabsolutions/payment-sdk-backend-open.svg?token=eZip4D1t6wvFGqNxU2ki&branch=master)](https://travis-ci.com/mobilabsolutions/payment-sdk-backend-open)

Many applications need to process payments for digital or physical goods. Implementing payment functionality can be very cumbersome though: many payment service providers support or don't support various types of payment methods and payment method registration and usage flows. The Stash SDK simplifies the integration of payments into our applications and abstracts away a lot of the internal complexity that different payment service providers' solutions have. With the Stash SDK it does not matter which payment service provider one chooses to register payment methods with - the API is standardized and works across the board.

## Supported PSPs

At the moment, the Stash Backend supports the following PSPs:

- BSPayone - Credit Cards / SEPA
- Braintree - PayPal
- Adyen - Credit Cards / SEPA

## Structure

This repository contains multiple modules:

- `payment-ws` - the main service module that contains the Stash Backend domain model, repositories, and API endpoints
- `payment-commons` - the common module that contains the error handling, project validations and common models and data between the `payment-ws` and PSP modules
- separate modules for every PSP - `payment-adyen`, `payment-braintree` and `payment-bs-one`
- `payment-notifications` - the notification service that contains notification domain model, repositories, and webhook endpoints for each PSP

## Requirements

To build this project, you will need to have at least the following:

- Preferred IDE
- JDK 8 or later
- Maven

## Building the project

The Stash Backend is using `ktlint` formatter. You should first format all the files by running the following command from the project root folder:
```
mvn antrun:run@ktlint-format
```

After that, you can build the package simply by running this command:

```
mvn clean install
```

The resulting jar file will be produced in the directory named `target`.

## Starting the service locally

If you want to start only the ws service, you should run `docker-compose up` from the `payment-ws` folder. It will start the following services :
- **PostgreSQL** - listens on port 5432, username:password - `payment:payment`
- **Zookeeper** - listens on port 2181
- **Kafka** - listens on port 9092, the topic will be created automatically on the application startup
- **payment-ws** - listens on port 8080

If you want to start both the ws service and the notification service, you should run `docker-compose up` from the root folder. It will start the following services :
- **2 PostgreSQL databases** 
  - payment db, listens on port 5432, username:password - `payment:payment`
  - notifications db, listens on port 5433, username:password - `notifications:notifications`
- **Zookeeper** - listens on port 2181
- **Kafka** - listens on port 9092, the topic will be created automatically on the application startup
- **2 services** 
  - payment-ws, listens on port 8080
  - payment-notifications, listens on port 8082

To shut down the services gracefully run `ctrl+c`. To reset the data of the environment run `docker-compose down`.

If you want to create a database instance on your own, you will need to set the configuration properties below. You can either put them in your local `application-properties.local`, or define the environment variables:

```
- spring.datasource.url: DB url
- spring.datasource.username: DB username
- spring.datasource.password: DB password
- spring.jpa.show-sql=true
- authorization.server.signingKey: oauth signing key
- payment.ws.notification.apiKey= notification service api key
  
- initial.data.loader.oauthClientId: oauth client id
- initial.data.loader.oauthClientPassword: oauth client password
- initial.data.loader.adminUsername: admin username
- initial.data.loader.adminPassword: admin password
```
 
After that, you can run the app directly from Maven using the Spring Boot plugin by simply running the following command:

```
mvn spring-boot:run -Dspring.profiles.active=local
```

You can now access the Stash Backend here: http://localhost:8080/ 

## Request authentication

In the Stash Backend, there are the `secret` and `publishable` keys that should be generated for the merchants. These keys will later be used for the authentication requests.

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

The Stash SDK uses a concept of idempotency for both aliases and transactions. The idempotent operation is the one that produces the same result no matter how many times it is called. The idempotency is performed by sending an `Idempotent-Key` in the header for `Create Alias`, `Preauthorization`, `Authorization` and `Refund` requests. This will avoid adding the same alias more than once or performing the same transaction several times if unintentionally called.

When a request comes with a new idempotent key, the key and the request body are stored in the Stash backend. If a second request comes with the same idempotent key and the same body, the original response is returned. However, if the second request has the same idempotent key as the original one, but a different body, an appropriate error will be returned.

## Feedback

The Stash Backend is in active development. We welcome your feedback! Please write to us at payment-sdk@mobilabsolutions.com to report any issues or give feedback.

## Documentation

- [Overall documentation](https://github.com/mobilabsolutions/payment-sdk-wiki-open/wiki)
- [Backend Wiki](https://github.com/mobilabsolutions/payment-sdk-backend-open/wiki)
- [API Documentation](https://payment-dev.mblb.net/api/v1/swagger-ui.html)
