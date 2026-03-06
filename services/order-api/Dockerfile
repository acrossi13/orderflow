FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN ./mvnw -q -DskipTests package

EXPOSE 8080

CMD ["java","-jar","target/order-api-0.0.1-SNAPSHOT.jar"]