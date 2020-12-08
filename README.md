## CO2 Sensor Service
A Service that will collect co2 data in an InfluxDb. You'll find it's API in `SensorAPI`.
The configuration is stored in `application.yml` and `application-{env}.yml`

## Development Setup
### Requirements
 * Docker
 * Java 11
 * Turn on annotation processing in your IDE 
### Run the local DB & the app
```
./gradlew influxdbDevStart
./gradlew run
./gradlew influxdbDevStop
```

* `influxdbStart` and `influxdbStop` are using docker and have been tested on Linux. If you're using another OS, 
please start influxDB with a command similar to what the [influxDbStart Task](build.gradle) in `build.gradle` uses. 

* For debugging, you'll find  that [Application](src/main/java/com/mruhwedel/application/Application.java) 
  has a main method that you can run. It's the same method that the task `run` calls.

### Testing
```
 ./gradlew test
```
The functional- and unit tests aren't yet separated. The functional tests require a running InfluxDb.
The test task takes care of firing it up Same caveat: It'll work on linux - maybe MacOS.

### Deploying
```bash
  ./gradle dockerBuildNative
  # will give you an image with a native executable that you may run/push
  docker run sensors:latest  
```

## Ideas for future development
* [Go Serverless](https://docs.micronaut.io/latest/guide/index.html#serverlessFunctions)
* [Caching](https://docs.micronaut.io/latest/guide/index.html#caching)
* [Offload blocking IO from event loop](https://docs.micronaut.io/latest/guide/index.html#reactiveServer)

