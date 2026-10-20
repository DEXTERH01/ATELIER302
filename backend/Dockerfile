FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline
COPY src src
RUN ./mvnw -B package
FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 atelier
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
USER atelier
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
