# Sambastian
Implementing HTTP Server Framework using java socket.

#### [document](https://hypernova1.github.io/java-http-server-doc/)
#### [Sample Project](https://github.com/hypernova1/Java-Http-Server-Sample)

### How to use
##### 0. install jdk 1.8++
##### 1. create maven project
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
##### 2. add dependency
~~~xml
<dependency>
    <groupId>io.github.hypernova1</groupId>
    <artifactId>sambastian</artifactId>
    <version>1.0</version>
</dependency>
~~~
##### 3. initial setting
filename: resources/static/resource/application.properties
~~~properties
server.port=[port number]
file-buffer-size=[file buffer size]
# activate SSL
key-store=[keyStore name]
key-store.password=[keyStore password]
~~~
##### 4. write main class
~~~java
@ComponentScan
public class Application {
    public static void main(String[] args) {
        HttpServer.start();
    }
}
~~~

##### 5. execute program
~~~
22:26:34.015 [main] INFO  org.sam.server.HttpServer - server started..
22:26:34.019 [main] INFO  org.sam.server.HttpServer - server port: 8081
~~~
