FROM tomcat:7.0-jre8

MAINTAINER Dirk Steinkopf "https://github.com/dsteinkopf"

COPY tuerauf.war $CATALINA_HOME/webapps/
RUN bash -c "touch $CATALINA_HOME/webapps/tuerauf.war"

COPY setenv.sh $CATALINA_HOME/bin/

VOLUME $CATALINA_HOME/conf

#see tomcat: EXPOSE 8080
#see tomcat: WORKDIR $CATALINA_HOME
CMD ["catalina.sh", "run"]

# build:
# mvn                        package docker:build
# mvn -Dmaven.test.skip=true package docker:build
# mvn -Dmaven.test.skip=true package docker:build -DpushImage

# run develop
# docker run -P --rm -p 8080:8080 --name tuerauf dsteinkopf/tuerauf

# run prod
# docker run --env=SPRING_PROFILES_ACTIVE=prod --env=SPRING_CONFIG_LOCATION=/usr/local/tomcat/conf/application-prod.properties -v $PWD/src/main/resources/application-prod.properties:/usr/local/tomcat/conf/application-prod.properties -P --rm -p 8080:8080 --name tuerauf dsteinkopf/tuerauf
