FROM gradle:8.14-jdk17 AS build

WORKDIR /app

COPY . .

RUN gradle build -x test --no-daemon


FROM eclipse-temurin:21

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]