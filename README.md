## sensor-micronaut

## Development Setup

### Run the local DB
```
 ./gradlew influxdbStart
```
This works on linux, if you're using another OS, please start influxDB with the command you may find in the 
[influxDbStart Task](build.gradle).

## Testing
```
 ./gradlew test
```

The functional and unit tests aren't yet separated. For the  