FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .
COPY src/ src/

RUN ./gradlew dependencies --no-daemon --stacktrace
RUN ./gradlew bootJar -x test --no-daemon --stacktrace && \
    mv /app/build/libs/*.jar /app/app.jar  # Fix wildcard issue

RUN useradd -m appuser && chown -R appuser /app
USER appuser

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]