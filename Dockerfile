# Stage 1: Build all modules
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy the entire project structure to satisfy Maven reactor
COPY pom.xml .
COPY customer_service ./customer_service
COPY merchant_service ./merchant_service
COPY payment_service ./payment_service
COPY token_service ./token_service
COPY simple_dtu_pay_client ./simple_dtu_pay_client

# Build the specific service
ARG SERVICE_NAME
# -pl builds only the service, -am builds its dependencies
# We skip tests and Hibernate/JAX-WS network calls during image build
RUN mvn -B -pl ${SERVICE_NAME} -am package \
    -DskipTests \
    -Dquarkus.package.type=uber-jar

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /work
ARG SERVICE_NAME

# Copy the built uber-jar from the build stage
COPY --from=build /workspace/${SERVICE_NAME}/target/*.jar /work/app.jar

# The app uses the port defined in its application.properties
CMD ["java","-jar","/work/app.jar"]