FROM openjdk:latest
ARG JAR_FILE=./opencbdc_ssi_client/target/opencbdc_ssi_client-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
RUN ls
ENTRYPOINT ["java","-jar","/app.jar"]