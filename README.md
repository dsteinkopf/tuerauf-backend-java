# tuerauf-backend-java

Java-Backend für https://github.com/dsteinkopf/tuerauf

## Demo URLs for test deployment:

* Config-Link: `tuerauftest:///?http%3A%2F%2Fdocker-int1.steinkopf.net%3A8097%2Ftuerauf%2Ffrontend%2F/myappsecret`
* [Dashboard](http://docker-int1.steinkopf.net:8097/tuerauf/dashboard/)
* [phpmyadmin](http://docker-int1.steinkopf.net:8098/) (user/pw: root/verysecretdbpw see application-test.properties)
* Registration calls:
  * [User 1](http://docker-int1.steinkopf.net:8097/tuerauf/frontend/registerUser?installationId=iid123456789&username=u1&appsecret=myappsecret&pin=1234)
  * [User 3](http://docker-int1.steinkopf.net:8097/tuerauf/frontend/registerUser?installationId=iid223456789&username=u2&appsecret=myappsecret&pin=1234)
  * [User 3](http://docker-int1.steinkopf.net:8097/tuerauf/frontend/registerUser?installationId=iid323456789&username=u3&appsecret=myappsecret&pin=1234)
* checkLocation calls
  * [far](http://docker-int1.steinkopf.net:8097/tuerauf/frontend/checkLocation?installationId=iid123456789&appsecret=myappsecret&geoy=23.45&geox=12.34)
  * [near](http://docker-int1.steinkopf.net:8097/tuerauf/frontend/checkLocation?installationId=iid123456789&appsecret=myappsecret&geoy=1.2341&geox=3.4561)

