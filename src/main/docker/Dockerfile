FROM tomcat:7.0-jre8
COPY tuerauf.war $CATALINA_HOME/webapps/
RUN bash -c "touch $CATALINA_HOME/webapps/tuerauf.war"
#see tomcat: EXPOSE 8080
#see tomcat: WORKDIR $CATALINA_HOME
CMD ["catalina.sh", "run"]

# build:
# mvn package docker:build
# mvn -Dmaven.test.skip=true package docker:build

# run
# docker run -P --rm -p 8080:8080 -v /dev/urandom:/dev/random --name tuerauf dsteinkopf/tuerauf