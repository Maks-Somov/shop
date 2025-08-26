FROM openjdk:17-jdk-slim AS build
WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew
ARG MODULE
RUN ./gradlew :${MODULE}:bootJar -x test

FROM openjdk:17-jdk-slim
WORKDIR /app
ARG MODULE
COPY --from=build /workspace/${MODULE}/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
