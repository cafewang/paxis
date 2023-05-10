FROM openjdk:11
COPY build/libs/paxis-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
