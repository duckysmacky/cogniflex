FROM eclipse-temurin:25-jdk-jammy AS builder

WORKDIR /app

COPY backend/gradlew ./
COPY backend/gradle ./gradle
COPY backend/build.gradle backend/settings.gradle ./
COPY ml-service/proto ../ml-service/proto

RUN chmod +x ./gradlew
RUN ./gradlew --version --no-daemon

COPY backend/src ./src

RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:25-jdk-jammy AS runner

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
