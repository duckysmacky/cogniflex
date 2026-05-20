FROM eclipse-temurin:25-jdk-jammy AS builder

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src

RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:25-jdk-jammy AS runner

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]