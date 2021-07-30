# SMART Hot Spot Application 1.0


## 개요

최적의 방법으로 사용자들이 SMFi 인터넷 망을 이용할 수 있도록 가장 가까운 위치에 위치한 안테나를 안내하여 연결해준다.

## 개발환경

- Andriod Studio Version 4.2.1

## UI 및 구현사항

- 서버와의 API 통신은 retrofit 을 사용할 수 있도록 설정 해두었다.
- Google Map API 을 사용하였다.

### Function 1 : 사용자 현재 위치 확인

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/b6cad722-1837-43ce-927c-6f7d61f8ce82/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/b6cad722-1837-43ce-927c-6f7d61f8ce82/Untitled.png)

사용자 실시간 위치 확인을 위한 permission

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/095052dc-0392-458a-a9e9-3350ad9fd381/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/095052dc-0392-458a-a9e9-3350ad9fd381/Untitled.png)

현재 에뮬레이터 시연으로 현재 위치 감지 안됨

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/a5f2b36b-d268-487e-b387-f4d58bfb7ee0/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/a5f2b36b-d268-487e-b387-f4d58bfb7ee0/Untitled.png)

사용자의 실시간 위치 log로 확인 가능

- 사용자의 현재 위치를 초기 화면으로 보여준다.
- 사용자는 사용자의 움직이는 위치를 실시간으로 파란색 표식을 통해 확인할 수 있다.

* 현재 에뮬레이터 시연 화면으로 현재 위치가 구글 본사로 보여지고 있으나, 실제 테스트 핸드폰으로는 사용자의 현재 위치 감지.

### Function 2 : 접속 가능한 SMART Hot spot 검색

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/cfdfce0f-34e0-4ca6-9237-6e83b600701b/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/cfdfce0f-34e0-4ca6-9237-6e83b600701b/Untitled.png)

일정 범위 내의 핫스팟이 존재하지 않는 경우

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/ac0d5241-7e39-4812-aae6-833526c1ba86/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/ac0d5241-7e39-4812-aae6-833526c1ba86/Untitled.png)

범위 내에 핫스팟이 존재하는 경우

- 접속 가능한 핫스팟 확인 버튼을 누르면 사용자 위치를 중심으로 반경 범위 내의 핫스팟을 확인할 수 있다.
- 접속 가능한 핫스팟이 존재하지 않는 경우 메세지가 출력되며 반경 범위를 조정하며 재탐색하도록 한다.
- 접속 가능한 핫스팟이 존재하는 경우 해당 안테나 위치가 보여지며 반경범위까지 확인할 수 있다.

### Function 3 : Hot spot 접속

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/707d73e4-edfc-461c-9831-1b00ef652567/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/707d73e4-edfc-461c-9831-1b00ef652567/Untitled.png)

핫스팟 클릭시 해당 핫스팟까지의 거리를 확인

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/2b87ed98-9f5b-48d2-b088-d2946c19623b/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/2b87ed98-9f5b-48d2-b088-d2946c19623b/Untitled.png)

말풍선을 누르면 핫스팟에 접속이 가능

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/544419a3-ca34-4828-a0dd-68d8ab526a01/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/544419a3-ca34-4828-a0dd-68d8ab526a01/Untitled.png)

핫스팟에 접속 시 마커가 생성

- 일정 반경 내에 있어 활성화된 안테나를 클릭하여 해당 핫스팟에 접속이 가능하다.
- 핫스팟에 접속하면 해당 안테나에 마커가 생성되고 하단에 연결된 핫스팟이 보여진다.

### Function 4 : HOT SPOT 해제

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/b8d27826-ccb2-4cc7-a896-7d4b4d48cf63/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/b8d27826-ccb2-4cc7-a896-7d4b4d48cf63/Untitled.png)

안테나 접속 해제

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/2272d19f-0991-46f6-ae1e-6f1039ccf9d9/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/2272d19f-0991-46f6-ae1e-6f1039ccf9d9/Untitled.png)

- 접속한 안테나의 말풍선을 누르면 접속 해제가 가능하다.

### Function 5 : 반경 범위 설정

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/5ab86fbc-3274-4f6f-9e31-268a91d40849/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/5ab86fbc-3274-4f6f-9e31-268a91d40849/Untitled.png)

- 지도의 하단 부를 보면 반경 범위 설정이 가능하다.
- progress bar 를 통해 현재 위치에서부터 얼마나 떨어진 안테나까지 검색할지 설정할 수 있다.

### ETC.

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/3329accc-6c6c-4efc-8b56-7da99dca0ed6/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/3329accc-6c6c-4efc-8b56-7da99dca0ed6/Untitled.png)

로그아웃 시 초기 화면으로 이동

- 로그아웃 버튼 클릭 시, 초기 화면으로 이동한다.
- logout 버튼 혹은 어플을 나가더라도 사용자가 접속한 HOT SPOT이 존재하다면 해당 정보를 SharedPreferences를 통해 관리하여 정보를 저장한다.

## 개발 계획

- DB 구축과 서버 연동을 통해 안테나의 정확한 위치 설정
- 실제 사용자가 해당 안테나와 연결하여 인터넷망을 이용할 수 있도록 작업
