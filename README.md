## CO2 Sensor Service


## Development Setup

### Run the local DB && the app

```
 ./gradlew influxdbStart
 ./gradlew run
```

* `influxdbStart` will work on Linux (maybe mac-os) if you're using another OS, 
please start influxDB with the command you may find in the [influxDbStart Task](build.gradle). 

* For debugging, you'll find  that [Application](src/main/java/com/mruhwedel/application/Application.java) 
  has a main method that you can run. It's the same method that the task `run` calls.

## Testing
```
 ./gradlew test
```
The functional and unit tests aren't yet separated. The functional tests require a running db.
The test task takes care of firing up. Same caveat: It'll work on linux (maybe MacOS)