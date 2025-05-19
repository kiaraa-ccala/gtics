FROM openjdk:25-ea-17-jdk
VOLUME /tmp
EXPOSE 8080
ADD ./target/ProyectoSanMiguel-0.0.1-SNAPSHOT.jar proyectosanmiguel.jar
ENTRYPOINT ["java","-jar","proyectosanmiguel.jar"]
LABEL authors="NewTelecomPUCP"
