FROM gradle:8.14-jdk17 AS build

WORKDIR /app

# Copy only the necessary build files first (for better caching)
COPY build.gradle settings.gradle ./

# Copy the gradle wrapper directory (with wrapper files)
COPY gradle ./gradle

# Copy the source code
COPY src ./src

RUN gradle build -x test --no-daemon


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-jar", "app.jar"]