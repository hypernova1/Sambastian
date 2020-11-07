# HTTP Server
Implementing HTTP Server using java socket.

### How to use
##### 0. install jdk 1.8++
##### 1. deploy local repository
~~~
$ mvn install
~~~
##### 2. create maven project
project structure
~~~
src/
 +- main/
     +- java/
     |   +- <source code>
     +- resources/
         +- config/
         |   +- application.properties
         +- static/
            +- <static files>
~~~
##### 3. add dependency
~~~xml
<dependency>
    <groupId>org.sam.server</groupId>
    <artifactId>sam-server</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
~~~
##### 4. initial setting
filename: resources/static/resource/application.properties
~~~properties
server.port=[port number]
file-buffer-size=[file buffer size]
# activate SSL
key-store=[keyStore name]
key-store.password=[keyStore password]
~~~
##### 5. write main class
~~~java
@ComponentScan
public class Application {
    public static void main(String[] args) {
        HttpServer.start();
    }
}
~~~

##### 6. execute program
~~~
22:26:34.015 [main] INFO  org.sam.server.HttpServer - server started..
22:26:34.019 [main] INFO  org.sam.server.HttpServer - server port: 8081
~~~

#### [Sample Project](https://github.com/hypernova1/Java-Http-Server-Sample)


