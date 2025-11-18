FROM robotshopdevacr.azurecr.io/baseimages/maven:3.9.11-eclipse-temurin-8-alpine-internal AS build

WORKDIR /app

COPY pom.xml .

RUN mvn -B dependency:go-offline

COPY src ./src

RUN mvn -B clean package -DskipTests=true

FROM robotshopdevacr.azurecr.io/baseimages/eclipse-temurin:8-jre-alpine-internal

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]