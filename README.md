# OpenAPI Proxy Spring Boot Starter

## Features

* Java 17
* Spring Framework 6
* Spring Boot 3
* Annotation-based proxy generation
* Proxies can be generated from OpenAPI specifications
* Proxy interceptors can be defined in order to modify HTTP Request and Response (Add/Remove Headers, Update Body)

OpenAPI Proxy Spring Boot Starter is released under the non-restrictive Apache 2.0 license,
and follows a very standard Github development process, using Github
tracker for issues and merging pull requests into main. If you want
to contribute even something trivial please do not hesitate, but
follow the guidelines below.

OpenAPI Proxy Spring Boot Starter offers the following benefits:

* Pure Spring Boot annotation-based HTTP-proxy
* Support for OpenAPI endpoints automatic proxying


## Installation

### Maven

Add the following dependency to the `<dependencies>` section of your project:

```xml
<dependency>
</dependency>
```

### Gradle

```groovy
dependencies {
}
```

## Usage




## Contribution guideline

Local environment setup:

_Prerequisite: JDK and Maven installed_

```
git clone git@github.com:chirob/openapi-proxy-spring-boot-starter.git
cd openapi-proxy-spring-boot-starter
git submodule init
git submodule update
```

### Building the project:

`mvn clean package`

### Building the project and running the official test suite:

Test annotated with `@Tag("acceptance")` require the test suite to be pulled using:

`git submodule update --init --recursive`

Then run the tests:

`mvn clean verify`

### Building the project without running the official test suite:
`mvn clean package -Dgroups='!acceptance'`

