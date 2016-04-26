[![][maven img]][maven]
[![][travis img]][travis]

A ready-to-use LifeCycle that registers and reports health to Consul. It assumes you're Jindy and that your application is also a LifeCycle with a well implemented isRunning() method.

## Usage
In your application configuration, make sure you have the following properties
```properties
application=myapp #the service name in Consul.
environment=dev #if environment is not defined, no Consul connection will be attempted
stack=web #required for Consul's key/value to work
```

In your application bootstrap, run the following code
```java
Healthy l = new Healthy(myApp,);
l.start();
```

[maven]:http://search.maven.org/#search|gav|1|g:"org.irenical.healthy"%20AND%20a:"healthy"
[maven img]:https://maven-badges.herokuapp.com/maven-central/org.irenical.healthy/healthy/badge.svg

[travis]:https://travis-ci.org/irenical/healthy
[travis img]:https://travis-ci.org/irenical/healthy.svg?branch=master
