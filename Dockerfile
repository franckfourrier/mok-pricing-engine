FROM eclipse-temurin:21-jre

WORKDIR /app

COPY pricing-app/target/*.jar app.jar

EXPOSE 8017

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java","-jar","/app/app.jar"]

