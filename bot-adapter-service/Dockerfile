FROM eclipse-temurin:25-jre
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 9126
ENV SPRING_PROFILES_ACTIVE=server
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
