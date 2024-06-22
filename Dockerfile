FROM amazoncorretto:17-alpine3.14-jdk
VOLUME /tmp

ARG JAR_FILE=target/*.jar
ARG USER_NAME=dev-user
ARG GROUP_NAME=backend-project
ARG GROUPID=1001
ARG USERID=1001

COPY ${JAR_FILE} app-start.jar

RUN addgroup --gid 1001 $GROUP_NAME &&  \
    adduser --disabled-password --gecos "Virtual account - bogdanmierloiu.com" --uid 1001 --home /home/$GROUP_NAME  \
    --ingroup $GROUP_NAME $USER_NAME

USER $USER_NAME

ENTRYPOINT ["java","-jar","/app-start.jar"]

