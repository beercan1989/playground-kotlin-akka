# Playground Kotlin + Akka
![license](https://img.shields.io/github/license/beercan1989/playground-kotlin-akka.svg)
[![build](https://travis-ci.com/beercan1989/playground-kotlin-akka.svg?branch=master)](https://travis-ci.com/beercan1989/playground-kotlin-akka)
[![release](https://img.shields.io/github/release/beercan1989/playground-kotlin-akka.svg)](https://github.com/beercan1989/playground-kotlin-akka/releases)
![commits since](https://img.shields.io/github/commits-since/beercan1989/playground-kotlin-akka/latest.svg)
  
Test project for Akka HTTP, Akka and Kotlin wrapped in Gradle

## Features in version 1
* [x] CI testing in Travis with Java 8 and 11
* [x] Akka HTTP routing
* [x] Github releases via Travis

## Features in version 2
* [x] Akka Typed actors processing requests
* [x] Basic unit tests for routes, actors and the glue between them
* [x] JSON handling of both rejections and exceptions

## Features coming  
* [ ] Throttling requests
* [ ] Circuit breaker
* [ ] Configuration to support high throughput

## How to
Run an instance
```bash
./gradlew run
```
Run tests only
```bash
./gradlew test
```
Build distribution
```bash
./gradlew build
```

## Endpoints
```bash
curl -i 'http://localhost:8080/hello-world'

HTTP/1.1 200 OK
Server: akka-http/10.1.5
Date: Mon, 17 Dec 2018 23:57:01 GMT
Content-Type: application/json
Content-Length: 25

{"message":"Hello World"}
```
