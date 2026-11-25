# multi-stage Dockerfile for ms-solicitud
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Expect the module JAR to be built locally at ms-solicitud/target/*.jar
COPY ms-solicitud/target/*.jar app.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8084
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-default} -DDB_HOST=${DB_HOST:-postgres} -jar /app/app.jar"]
