FROM eclipse-temurin:21-jre-jammy
#FROM debian:bullseye-slim

RUN apt-get update && \
    apt-get install -y git maven && \
    rm -rf /var/lib/apt/lists/*

RUN apt-get update
RUN apt-get install -y git

RUN git config --global user.email "kamogotjhb024@student.wethinkcode.co.za"

WORKDIR /srv

ADD target/robot-world-*.*.*-SNAPSHOT-jar-with-dependencies.jar /srv/robot-world.jar

ADD .git /srv/.git

EXPOSE 5050
CMD ["java", "-jar", "robot-world.jar"]