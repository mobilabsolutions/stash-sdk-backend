# Stash! Backend

[![Build Status](https://travis-ci.com/mobilabsolutions/payment-sdk-backend-open.svg?token=eZip4D1t6wvFGqNxU2ki&branch=master)](https://travis-ci.com/mobilabsolutions/payment-sdk-backend-open)

Many applications need to process payments for digital or physical goods. Implementing payment functionality can be very cumbersome though: many payment service providers support or don't support various types of payment methods and payment method registration and usage flows. The Stash! SDK simplifies the integration of payments into our applications and abstracts away a lot of the internal complexity that different payment service providers' solutions have. With the Stash! SDK it does not matter which payment service provider one chooses to register payment methods with - the API is standardized and works across the board.

There are many ways to contribute to this project. Get started [here](https://github.com/mobilabsolutions/payment-sdk-backend-open/tree/master/.github/CONTRIBUTING.md) and make sure to take a look at our [code of conduct](https://github.com/mobilabsolutions/payment-sdk-backend-open/tree/master/.github/CODE_OF_CONDUCT.md).

## Supported PSPs

At the moment, the Stash! backend supports the following PSPs:

- BSPayone - Credit Cards / SEPA
- Braintree - PayPal
- Adyen - Credit Cards / SEPA

## Project Structure

This repository contains multiple modules:

- `payment-ws` - the main service module that contains the Stash! backend domain model, repositories and API endpoints
- `payment-commons` - contains error handling, project validations and common models and data shared between `payment-ws` and the PSP modules
- separate modules for every PSP - `payment-adyen`, `payment-braintree`, `payment-bs-one`
- `payment-notifications` - the notification service that contains the notification domain model, repositories, and webhook endpoints for each PSP

## Requirements

To build this project, you will need to have at least the following:

- JDK 8 or later
- Maven

## Building the project

The Stash! backend uses the `ktlint` formatter. When making changes, you can run this command to auto-format the code:
```
mvn antrun:run@ktlint-format
```

After that, you can build the package by running this command:

```
mvn clean install
```

## Starting the service locally

If you want to start only the ws service, you should run `docker-compose up` from the `payment-ws` folder. It will start the following services:
- **PostgreSQL** - listens on port 5432, username:password - `payment:payment`
- **payment-ws** - listens on port 8080

If you want to start both the ws service and the notification service, you should run `docker-compose up` from the root folder. It will start the following services:
- **2 PostgreSQL databases** 
  - payment db, listens on port 5432, username:password - `payment:payment`
  - notifications db, listens on port 5433, username:password - `notifications:notifications`
- **2 services** 
  - payment-ws, listens on port 8080
  - payment-notifications, listens on port 8082

To shut down the services gracefully run `ctrl+c`. To reset the data of the environment run `docker-compose down -v`.

If you want to create a database instance on your own, you will need to set the configuration properties below. You can either put them in your local `application-properties.local`, or define the environment variables:

```
- spring.datasource.url: DB url
- spring.datasource.username: DB username
- spring.datasource.password: DB password
- postgres.db.port: DB port
- postgres.db.host: DB host
- postgres.db.name: DB name
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

You can now access the Stash! Backend here: http://localhost:8080/ 

### Setting up infrastructure

For setting up infrastructure on Google Cloud Platform, the provided Terraform scripts can be used.

* In cloud console, navigate to IAM & Admin > Service Accounts, and click Create Service Account with `Editor` 
role. Create a new private key in JSON format and download it. Then create a directory called `creds` inside the 
`terraform` root directory and copy this JSON file into it.
* In cloud console, create a GCS bucket with bucket name of `stash-terraform-states` and prefix of `mobility` for 
storing the terraform states.
* Execute following Terraform commands inside `terraform` root directory and your infrastructure will be ready in few 
minutes
```
$ terraform init
$ terraform plan
$ terraform apply
```

## Request authentication

In the Stash! Backend, there are the `secret` and `publishable` keys that should be generated for merchants. These keys will later be used for the authentication requests.

The publishable key is used to authenticate alias registration requests:
- Create alias
- Exchange alias

The secret key is used to authenticate transactions requests and alias deletion:
- Preauthorization
- Capture
- Authorization
- Reversal
- Refund
- Delete alias

## Idempotency

The Stash! SDK uses idempotence keys for both aliases and transactions. This prevents adding the same alias more than once or performing the same transaction several times. Idempotence works via an `Idempotent-Key` header for `Create Alias`, `Preauthorization`, `Authorization` and `Refund` requests. 

The value of the header and the request body are stored in the Stash! backend. If a second request comes with the same idempotent key and the same body, the original response is returned. However, if the second request has the same idempotent key as the original one, but a different body, an error is returned.

## Feedback

The Stash! backend is in active development. We welcome your feedback! Please write to us at payment-sdk@mobilabsolutions.com to report any issues or give feedback.

## Documentation

- [Overall documentation](https://github.com/mobilabsolutions/payment-sdk-wiki-open/wiki)
- [Backend Wiki](https://github.com/mobilabsolutions/payment-sdk-backend-open/wiki)
- [API Documentation](https://payment-dev.mblb.net/api/v1/swagger-ui.html)
