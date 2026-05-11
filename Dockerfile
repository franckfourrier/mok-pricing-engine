# =========================
# Stage 1 : Build Maven
# =========================
FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY . .

RUN chmod +x mvnw

RUN ./mvnw -pl pricing-app -am clean package -DskipTests


# =========================
# Stage 2 : Runtime
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/pricing-app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
