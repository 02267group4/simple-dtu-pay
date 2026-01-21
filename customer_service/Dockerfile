# dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# install maven (Debian/Ubuntu based image)
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# copy sources and build fat/uber jar
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests -Dquarkus.package.type=uber-jar

FROM eclipse-temurin:21-jre
WORKDIR /work
COPY --from=build /workspace/target/*.jar /work/app.jar
EXPOSE 8181
CMD ["java","-jar","/work/app.jar"]
