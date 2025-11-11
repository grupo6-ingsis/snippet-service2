FROM gradle:8.5.0-jdk21 AS build
COPY  . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle assemble
FROM eclipse-temurin:21-jre
EXPOSE 8080
RUN mkdir -p /opt/newrelic
ADD https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-agent.jar /opt/newrelic/newrelic-agent.jar
RUN chmod 644 /opt/newrelic/newrelic-agent.jar

RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar

ENTRYPOINT ["java", \
    "-javaagent:/opt/newrelic/newrelic-agent.jar", \
    "-jar", \
    "-Dspring.profiles.active=production", \
    "/app/spring-boot-application.jar"]