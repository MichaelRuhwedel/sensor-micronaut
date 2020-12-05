# test file for manual testing that can be run with https://httpie.io/docs
http POST http://localhost:8080/api/v1/sensors/4324/measurements < 0-measurement-below-threshold.json
http GET http://localhost:8080/api/v1/sensors/4324
#OK

http POST http://localhost:8080/api/v1/sensors/4324/measurements < 1-measurement-above-threshold.json
http GET http://localhost:8080/api/v1/sensors/4324
#warn

http POST http://localhost:8080/api/v1/sensors/4324/measurements < 2-measurement-above-threshold.json
http GET http://localhost:8080/api/v1/sensors/4324
#warn


http POST http://localhost:8080/api/v1/sensors/4324/measurements < 3-measurement-above-threshold.json
http GET http://localhost:8080/api/v1/sensors/4324
#alert

http POST http://localhost:8080/api/v1/sensors/4324/measurements < 4-measurement-below-threshold.json
http GET http://localhost:8080/api/v1/sensors/4324
#alert

http POST http://localhost:8080/api/v1/sensors/4324/measurements < 5-measurement-below-threshold.json
http GET http://localhost:8080/api/v1/sensors/4324
#alert

http POST http://localhost:8080/api/v1/sensors/4324/measurements < 6-measurement-below-threshold.json
http GET http://localhost:8080/api/v1/sensors/4324
#ok

http POST http://localhost:8080/api/v1/sensors/4324/measurements < 7-measurement-above-threshold.json
http GET http://localhost:8080/api/v1/sensors/4324
#warn
