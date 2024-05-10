# spring-batch-with-s3

Proof of concept of spring batch app downloading file from s3 and processing to save into MySQL Database.


# How to run

You should have a docker configured and then run the docker-compose.

Running docker-compose
```bash
docker-compose -f docker-compose.yml up
```

Running aws localstack to create a s3 bucket and upload PEOPLE.txt file
```bash
chmod +x awslocal.sh && ./awslocal.sh
```

and then run the spring batch application

```bash
./gradlew bootrun
```