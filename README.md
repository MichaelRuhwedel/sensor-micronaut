## CO2 Sensor Service
A Service that will collect co2 data in an InfluxDb. You'll find it's API in `SensorAPI`.
The configuration is stored in `application.yml` and `application-{env}.yml`
.The app will listen on http://localhost:8080

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

### Deploying / Running
#### Native Executable in a Docker Image 
```bash
  # build a docker image that you may run/push
  ./gradle dockerBuildNative  
  docker run sensor
  # start time ~100ms  
```
#### Traditional Fat Jar
```bash
  # test and build a fat jar
  ./gradle build 
  # run it
  java -jar build/libs/sensor-0.1-all.jar
  # start time ~1000ms
```

## Ideas for future development
* [Go Serverless](https://docs.micronaut.io/latest/guide/index.html#serverlessFunctions)
* [Caching](https://docs.micronaut.io/latest/guide/index.html#caching)
* [Offload blocking IO from event loop](https://docs.micronaut.io/latest/guide/index.html#reactiveServer)

