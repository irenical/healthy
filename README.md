[![][maven img]][maven]
[![][travis img]][travis]
[![][codecov img]][codecov]
[![][codacy img]][codacy]

# Healthy
A ready-to-use LifeCycle that registers and reports health to Consul. It assumes you're using Jindy and that your application is also a LifeCycle with a well implemented isRunning() method.

![alt text][cardio]  

## Usage
In your application configuration, make sure you have the following properties
```properties
my.service.address= node354.mydomain.com #the address your service will be exposed on (defaults to the node's IP)
my.service.port=1337 #the port your service will be exposed on (mandatory). The key is arbitrary
healthy.interval.millis=10000 #the health report interval (defaults to 10000, minimum value=1000)

consul.host=localhost #Consul's host (defaults to localhost)
consul.port=8500 #Consul's port (defaults to 8500)
```

In your application bootstrap, run the following code
```java
Healthy l = new Healthy(myApp, "myapp", null, "my.service.port");
l.start();
```

[cardio]:https://www.irenical.org/healthy/hb.png "Sometimes a straight line"

[maven]:http://search.maven.org/#search|gav|1|g:"org.irenical.healthy"%20AND%20a:"healthy"
[maven img]:https://maven-badges.herokuapp.com/maven-central/org.irenical.healthy/healthy/badge.svg

[travis]:https://travis-ci.org/irenical/healthy
[travis img]:https://travis-ci.org/irenical/healthy.svg?branch=master

[codecov]:https://codecov.io/gh/irenical/healthy
[codecov img]:https://codecov.io/gh/irenical/healthy/branch/master/graph/badge.svg

[codacy]:https://www.codacy.com/app/tiagosimao/healthy?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=irenical/healthy&amp;utm_campaign=Badge_Grade
[codacy img]:https://api.codacy.com/project/badge/Grade/5314e5c382804a95bea12bd9a2a8c2da
