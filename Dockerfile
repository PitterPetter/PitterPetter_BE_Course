FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew \
    && ./gradlew bootJar --no-daemon


FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]
