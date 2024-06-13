FROM openjdk:23-slim
ENV WRK_DIR /opt/app
WORKDIR ${WRK_DIR}
RUN mkdir db

ARG JAR_FILE=app/*.jar
COPY ${JAR_FILE} app.jar

VOLUME ./db/:${WRK_DIR}/db

ENTRYPOINT ["java", "-jar", "./app.jar"]
