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

* `influxdbStart` and `influxdbStop` have been tested Linux (maybe mac-os) if you're using another OS, 
please start influxDB with the command you may find in the [influxDbStart Task](build.gradle). 

* For debugging, you'll find  that [Application](src/main/java/com/mruhwedel/application/Application.java) 
  has a main method that you can run. It's the same method that the task `run` calls.

### Testing
```
 ./gradlew test
```
The functional- and unit tests aren't yet separated. The functional tests require a running db.
The test task takes care of firing it up (i. Same caveat: It'll work on linux (maybe MacOS)

### Ideas for future development
* [Go Serverless](https://docs.micronaut.io/latest/guide/index.html#serverlessFunctions)
* [Use a native image on Graalvm](https://docs.micronaut.io/latest/guide/index.html#graalServices)
