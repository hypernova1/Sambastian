# HTTP Server
자바 Socket을 이용하여 HTTP 서버 구현

### 사용 방법
0. jdk 1.8 이상 설치
1. 로컬 저장소에 jar 배포
~~~
$ mvn install
~~~
2. maven project 생성
3. pom.xml 의존성 추가
~~~xml
<dependency>
    <groupId>org.sam.server</groupId>
    <artifactId>sam-server</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
~~~
4. resources/static/resource/application.properties 에 설정 추가
~~~properties
server.port=[포트 번호]

# SSL 활성화시
keyStore=[keyStore 파일명]
keyStorePassword=[keyStore 비밀번호]
~~~
5. 루트 패키지에 메인 클래스 생성 후 함수 호출
~~~java
public class Application {
    public static void main(String[] args) {
        HttpServer.start();
    }
}
~~~

6. 실행
~~~
22:26:34.015 [main] INFO  org.sam.server.HttpServer - server started..
22:26:34.019 [main] INFO  org.sam.server.HttpServer - server port: 8081
~~~

#### [Sample Project](https://github.com/hypernova1/Java-Http-Server-Sample)
