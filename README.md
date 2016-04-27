[![][maven img]][maven]
[![][travis img]][travis]

A ready-to-use LifeCycle that registers and reports health to Consul. It assumes you're Jindy and that your application is also a LifeCycle with a well implemented isRunning() method.

## Usage
In your application configuration, make sure you have the following properties
```properties
application=myapp #the service name in Consul (mandatory)
my.service.address= node354.mydomain.com #the address your service will be exposed on (defaults to the node's IP)
my.service.port=1337 #the port your service will be exposed on (mandatory). The key is arbitrary
healthy.interval.millis=10000 #the health report interval (defaults to 10000, minimum value=1000)

consul.host=localhost #Consul's host (defaults to localhost)
consul.port=8500 #Consul's port (defaults to 8500)
```

In your application bootstrap, run the following code
```java
Healthy l = new Healthy(myApp,null,"my.service.port");
l.start();
```

[maven]:http://search.maven.org/#search|gav|1|g:"org.irenical.healthy"%20AND%20a:"healthy"
[maven img]:https://maven-badges.herokuapp.com/maven-central/org.irenical.healthy/healthy/badge.svg

[travis]:https://travis-ci.org/irenical/healthy
[travis img]:https://travis-ci.org/irenical/healthy.svg?branch=master
