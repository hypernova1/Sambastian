# HTTP Server
자바로 웹 서버 구현

#### SSL 활성화 방법
resources/config/application.properties
* keyStore: keyStore 파일명
* keyStorePassword: keyStore 비밀번호

### 구현 객체
#### HttpServer
요청을 받을 때 마다 스레드를 생성하여 위임한다.
#### BeanLoader
핸들러 클래스를 찾아 클래스 정보를 저장한다.
#### RequestReceiver
HttpServer객체에게 받은 요청 정보를 분석하여 Request와 Response 객체를 만들고 핸들러를 찾아 실행한다.
#### Request
요청의 정보(경로, 헤더, 파라미터 등)를 가진다.
#### Response
정보를 받아 클라이언트에 응답한다. 