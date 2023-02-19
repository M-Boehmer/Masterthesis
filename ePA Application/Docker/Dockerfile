##FROM azul/zulu-openjdk-alpine:17.0.5-jre-headless

FROM eclipse-temurin:17-jdk-alpine as kafkazeebeconnector
COPY /KafkaZeebeConnector/target/KafkaZeebeConnector-0.0.1-SNAPSHOT.jar KafkaZeebeConnector.jar
ENTRYPOINT ["java","-jar","KafkaZeebeConnector.jar"]

FROM eclipse-temurin:17-jdk-alpine as worker1
VOLUME /tmp
COPY /worker1/target/worker1-0.0.1-SNAPSHOT.jar worker1.jar
ENTRYPOINT ["java","-jar","worker1.jar"]

FROM eclipse-temurin:17-jdk-alpine as worker2
VOLUME /tmp
COPY /worker2/target/worker2-0.0.1-SNAPSHOT.jar worker2.jar
ENTRYPOINT ["java","-jar","worker2.jar"]

FROM eclipse-temurin:17-jdk-alpine as worker3
VOLUME /tmp
COPY /worker3/target/worker3-0.0.1-SNAPSHOT.jar worker3.jar
ENTRYPOINT ["java","-jar","worker3.jar"]

FROM eclipse-temurin:17-jdk-alpine as worker4
VOLUME /tmp
COPY /worker4/target/worker4-0.0.1-SNAPSHOT.jar worker4.jar
ENTRYPOINT ["java","-jar","worker4.jar"]

FROM eclipse-temurin:17-jdk-alpine as worker5
VOLUME /tmp
COPY /worker5/target/worker5-0.0.1-SNAPSHOT.jar worker5.jar
ENTRYPOINT ["java","-jar","worker5.jar"]

FROM eclipse-temurin:17-jdk-alpine as worker6
VOLUME /tmp
COPY /worker6/target/worker6-0.0.1-SNAPSHOT.jar worker6.jar
ENTRYPOINT ["java","-jar","worker6.jar"]

FROM eclipse-temurin:17-jdk-alpine as worker7
VOLUME /tmp
COPY /worker7/target/worker7-0.0.1-SNAPSHOT.jar worker7.jar
ENTRYPOINT ["java","-jar","worker7.jar"]

FROM eclipse-temurin:17-jdk-alpine as workeri1
VOLUME /tmp
COPY /workerI1/target/workerI1-0.0.1-SNAPSHOT.jar workerI1.jar
ENTRYPOINT ["java","-jar","workerI1.jar"]

FROM eclipse-temurin:17-jdk-alpine as workeri2
VOLUME /tmp
COPY /workerI2/target/workerI2-0.0.1-SNAPSHOT.jar workerI2.jar
ENTRYPOINT ["java","-jar","workerI2.jar"]