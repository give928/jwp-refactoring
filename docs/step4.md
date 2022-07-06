# 레거시 코드 리팩터링

## 4단계 - 멀티 모듈 적용

### 요구 사항
- Gradle의 멀티 모듈 개념을 적용해 자유롭게 서로 다른 프로젝트로 분리해 본다.
  - 컨텍스트 간의 독립된 모듈로 만들 수 있다.
  - 계층 간의 독립된 모듈로 만들 수 있다.
- 의존성 주입, HTTP 요청/응답, 이벤트 발행/구독 등 다양한 방식으로 모듈 간 데이터를 주고받을 수 있다.

### 리팩터링
- [x] 멀티 모듈로 분리
  - 컨텍스트 간의 독립된 모듈로 분리
    - 공통 모듈: common-module
    - 상품 모듈: product-module
    - 메뉴 모듈: menu-module
    - 테이블 모듈: table-module
    - 주문 모듈: order-module
  - 모든 모듈들은 공통 모듈에만 의존
    - 공통 모듈에 gradle java-test-fixtures 플러그인 추가해서 테스트 클래스 공유
  - 하나의 모듈 안에서는 도메인 객체 참조 사용
- [x] HTTP 요청/응답
  - 메뉴 -> 상품
    - 메뉴 등록 시 상품 모듈에 HTTP 요청해서 상품의 아이디, 이름, 가격을 응답 받아서 유효성 확인
- [x] 이벤트 발행/구독
  - spring-kafka 의존성 추가
  - 이 프로젝트에서는 이벤트 발생 시 다른 모듈에서 유효성을 확인하고 정상/예외 결과에 따라 후처리를 해야하는 케이스만 존재
    - ReplyingKafkaTemplate 사용해서 json 메시지로 요청하고 RequestReplyFuture 비동기 결과를 응답 받음
  - 주문 생성 이벤트
    - 주문 -> 테이블
    - 주문 -> 메뉴
  - 테이블 변경 이벤트
    - 테이블 -> 주문
  - 테이블 단체 지정 이벤트
    - 테이블 -> 주문
- [x] JPA open-in-view false 적용