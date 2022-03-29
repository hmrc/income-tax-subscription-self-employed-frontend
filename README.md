
# income-tax-subscription-self-employed-frontend

Scala/Play frontend web UI that provides screens for an existing SA Individual to provide the details required to sign-up a self-employed business.

1. [Quick start](#Quick-start)
- [Prerequisites](#Prerequisites)
- [How to start](#How-to-start)
- [How to use](#How-to-use)
- [How to test](#How-to-test)
2. [Persistence](#Persistence)

# Quick start

## Prerequisites

* [sbt](http://www.scala-sbt.org/)
* MongoDB (*[See Persistence](#Persistence)*)
* HMRC Service manager (*[Install Service-Manager](https://github.com/hmrc/service-manager/wiki/Install#install-service-manager)*)

## How to start

**Run the service with `ITSA_SUBSC_ALL`:**
```
./scripts/start
```

**Run the service with mininal downstreams:**
```
./scripts/start --minimal
```
   
## How to use
  
   This service has pages for two separate flows; agent and individual.

   **Using the service locally**

* Login via: [http://localhost:9949/auth-login-stub/gg-sign-in](http://localhost:9949/auth-login-stub/gg-sign-in)
* Entry page (individual): [http://localhost:9563/report-quarterly/income-and-expenses/sign-up/self-employments/details](http://localhost:9563/report-quarterly/income-and-expenses/sign-up/self-employments/details)
* Entry page (agent): [http://localhost:9563/report-quarterly/income-and-expenses/sign-up/self-employments/client/details](http://localhost:9563/report-quarterly/income-and-expenses/sign-up/self-employments/client/details)
* Feature switches: [http://localhost:9563/report-quarterly/income-and-expenses/sign-up/self-employments/test-only/feature-switch](http://localhost:9563/report-quarterly/income-and-expenses/sign-up/self-employments/test-only/feature-switch)

  
**Using the service on staging**

* Login via: [https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in](https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in)
* Entry page (individual): [https://www.staging.tax.service.gov.uk/report-quarterly/income-and-expenses/sign-up/self-employments/details](https://www.staging.tax.service.gov.uk/report-quarterly/income-and-expenses/sign-up/self-employments/details)
* Entry page (agent): [https://www.staging.tax.service.gov.uk/report-quarterly/income-and-expenses/sign-up/self-employments/client/details](https://www.staging.tax.service.gov.uk/report-quarterly/income-and-expenses/sign-up/self-employments/client/details)
* Feature switches: [https://www.staging.tax.service.gov.uk/report-quarterly/income-and-expenses/sign-up/self-employments/test-only/feature-switch](https://www.staging.tax.service.gov.uk/report-quarterly/income-and-expenses/sign-up/self-employments/test-only/feature-switch)


## How to test

* Run unit tests: `sbt clean test`
* Run integration tests: `sbt clean it:test`
* Run performance tests: provided in the repo [income-tax-subscription-performance-tests](https://github.com/hmrc/income-tax-subscription-performance-tests)
* Run acceptance tests: provided in the repo [income-tax-subscription-acceptance-tests](https://github.com/hmrc/income-tax-subscription-acceptance-tests)

# Persistence

Data is stored as key/value in Mongo DB. See json reads/writes implementations (especially tests) for details.

To connect to the mongo db provided by docker (recommended) please use;

```
docker exec -it mongo-db mongosh
```

Various commands are available.  Start with `show dbs` to see which databases are populated.

### License.

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")


