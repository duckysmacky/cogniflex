FROM eclipse-temurin:25-jdk-jammy AS builder

WORKDIR /app/backend

COPY backend/gradlew ./
COPY backend/gradle ./gradle
COPY backend/build.gradle backend/settings.gradle ./
COPY backend/src ./src
COPY proto ../proto

RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:25-jdk-jammy AS runner

WORKDIR /app

COPY --from=builder /app/backend/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]