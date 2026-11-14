FROM eclipse-temurin:21-jdk-alpine
COPY target/roadready-backend-0.0.1-SNAPSHOT.jar roadready_backend.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","roadready_backend.jar"]