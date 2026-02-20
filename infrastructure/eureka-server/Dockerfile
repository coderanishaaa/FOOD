FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache wget
VOLUME /tmp
COPY target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app.jar"]
