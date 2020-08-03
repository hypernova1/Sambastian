# HTTP Server
Socket을 이용하여 자바로 웹 서버 구현

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
root-package=[루트 패키지 경로]
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
server started..
server port: 8080
~~~

### 구현 클래스
#### HttpServer
요청을 받을 때 마다 스레드를 생성하여 RequestReceiver에 위임한다.
#### BeanLoader
핸들러 클래스를 찾아 클래스 정보를 저장한다.
#### RequestReceiver
HttpServer객체에게 받은 요청 정보를 분석하여 Request와 Response 객체를 만들고 핸들러를 찾아 실행한다.
#### Request
요청의 정보(경로, 헤더, 파라미터 등)를 가진다.
#### Response
정보를 받아 클라이언트에 응답한다. 