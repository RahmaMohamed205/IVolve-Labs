FROM maven:3.9-eclipse-temurin-17
RUN useradd -m appuser
WORKDIR /app
COPY . .
RUN chown -R appuser /app
USER appuser
RUN mvn package
EXPOSE 8080
CMD ["java", "-jar", "target/calculator.jar"]
