FROM java:8-jre

MAINTAINER Dirk Steinkopf "https://github.com/dsteinkopf"

COPY tuerauf.jar tuerauf.jar
RUN bash -c 'touch /tuerauf.jar'

# see https://spring.io/guides/gs/spring-boot-docker/
# that is where a Spring Boot application creates working directories for Tomcat by default:
# VOLUME /tmp

EXPOSE 8080

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/tuerauf.jar"]

# build:
# mvn                        package docker:build
# mvn -Dmaven.test.skip=true package docker:build
# mvn -Dmaven.test.skip=true package docker:build -DpushImage

# run develop
# docker run -P --rm -p 8080:8080 --name tuerauf dsteinkopf/tuerauf

# run prod
# docker run --env=SPRING_PROFILES_ACTIVE=prod --env=SPRING_CONFIG_LOCATION=/application-prod.properties -v $PWD/src/main/resources/application-prod.properties:/application-prod.properties --link mysql:mysql-1 -P --rm -p 8080:8080 --name tuerauf dsteinkopf/tuerauf

# run mysql e.g.
# docker run --env=MYSQL_PASS=tuerauf --env=ON_CREATE_DB=tuerauf -v /opt/tuerauf/var_lib_mysql:/var/lib/mysql --rm --name mysql tutum/mysql