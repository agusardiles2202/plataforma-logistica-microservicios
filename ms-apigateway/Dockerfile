# multi-stage Dockerfile for ms-apigateway
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Expect the module JAR to be built locally at ms-apigateway/target/*.jar (context is root)
COPY ms-apigateway/target/*.jar app.jar
ENV JAVA_OPTS="-Xms128m -Xmx256m"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-default} -Dapigw.url-solicitud=${APIGW_URL_SOLICITUD:-http://ms-solicitud:8084} -Dapigw.url-contenedor=${APIGW_URL_CONTENEDOR:-http://ms-contenedor:8083} -Dapigw.url-tarifa=${APIGW_URL_TARIFA:-http://ms-tarifa:8085} -jar /app/app.jar"]
