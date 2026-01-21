To run docker:
start by cd simple_dtu_pay_service and 
run mvn package -DskipTests
cd ..
then docker compose up -d --build

set environment variable BANK_API_KEY to "eagle0192"
windows: $env:BANK_API_KEY="eagle0192"
Linux: export BANK_API_KEY="eagle0192"

for testing:
cd simple_dtu_pay_client
mvn clean test
