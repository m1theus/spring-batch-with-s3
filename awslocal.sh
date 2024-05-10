# create s3 bucket
aws --endpoint http://s3.localhost.localstack.cloud:4566/ s3 mb s3://my-bucket

# list s3 buckets
aws s3 ls --endpoint-url=http://s3.localhost.localstack.cloud:4566/

aws s3 cp ./PEOPLE.txt s3://my-bucket/ \
     --endpoint-url=http://s3.localhost.localstack.cloud:4566/