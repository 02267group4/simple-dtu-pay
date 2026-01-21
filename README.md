To run docker:
cd simple_dtu_pay_service 
mvn package -DskipTests
cd ..
docker compose down -v #in case there are old containers
docker compose up -d --build

set environment variable BANK_API_KEY to "eagle0192"
windows: $env:BANK_API_KEY="eagle0192"
Linux: export BANK_API_KEY="eagle0192"

for testing:
cd simple_dtu_pay_client
mvn clean test
