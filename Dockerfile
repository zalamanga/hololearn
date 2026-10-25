FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
ENV MAVEN_OPTS="-Xmx384m -Djava.net.preferIPv4Stack=true"
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,id=hololearn-maven,target=/root/.m2 mvn -B -ntp clean verify

FROM tomcat:9.0.123-jre17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/hololearn.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
