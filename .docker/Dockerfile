FROM tomcat:7.0-jre8
COPY tuerauf.war $CATALINA_HOME/webapps/
#see tomcat: EXPOSE 8080
#see tomcat: WORKDIR $CATALINA_HOME
CMD ["catalina.sh", "run"]

# build:
# mv clean install
# cd target
# docker build -t tuerauf .

# run
# docker run -P --rm -p 8080:8080 -v /dev/urandom:/dev/random --name tuerauf tuerauf
