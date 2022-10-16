FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="m.chernyavska@gmail.com"

COPY build/libs/konnectors.jar /opt/konnectors/konnectors.jar

EXPOSE 8088

CMD ["java", "-server", "-Xmx1024m", "-Xms1024m", "-jar", "/opt/konnectors/konnectors.jar"]
