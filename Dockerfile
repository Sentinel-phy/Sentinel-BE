FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew 2>/dev/null || true
RUN ./gradlew fatJar --no-daemon 2>/dev/null || gradle fatJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar
EXPOSE 8090
CMD ["java", "-jar", "app.jar"]
