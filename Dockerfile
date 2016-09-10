FROM docker.io/openjdk:8-alpine

MAINTAINER  xqyu@yihecloud.com

WORKDIR /program

COPY docker/startup.sh /program/startup.sh
RUN chmod +x /program/startup.sh

COPY target/yuxueqiang1-0.0.1-SNAPSHOT.jar /program/app.jar

CMD /program/startup.sh