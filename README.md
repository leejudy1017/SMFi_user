## 개요

최적의 방법으로 사용자들이 인터넷 망을 이용할 수 있도록 할 때, 안테나 사이의 거리, 각도, 안테나가 세워질 곳의 경도 위도 등을 관리자가 손쉽게 계산하고 관리할 수 있도록 한다.

## 개발환경

- Andriod Studio Version 4.2.1

## UI 및 구현사항

- 서버와의 API 통신은 retrofit 을 사용할 수 있도록 설정 해두었다.
- Google Map API 을 사용하였다.

### Function 1 : 사용자 현재 위치 확인

![1](https://user-images.githubusercontent.com/70352603/133383184-cb999d51-8a00-48be-b8fc-a7c699a97e51.png)

- 사용자의 현재 위치를 초기 화면으로 보여준다.
- 사용자는 사용자의 움직이는 위치를 실시간으로 파란색 표식을 통해 확인할 수 있고, 관리자는 log에 찍히는 위도, 경도로 확인할 수 있다.
- 카메라는 사용자의 현재 위치를 초기 화면으로 제공하고, 이 후 사용자의 검색이나  드래그를 통한 지도 이동을 포커싱한다. (tracking 변수 값으로 확인)

* 현재 에뮬레이터 시연 화면으로 현재 위치가 구글 본사로 보여지고 있으나, 실제 테스트 핸드폰으로는 사용자의 현재 위치 감지.

### Function 2 : 검색 기능

![2](https://user-images.githubusercontent.com/70352603/133383190-0a6e4465-a757-42c4-9831-f78aa71983e4.png)

- 상단의 검색 기능은 geocoder 를 사용하였다.
- 도로명, 주소명으로 위치를 입력할 시 해당 위치에 TP를 설정할 수 있다. (건물명은 검색되지 않는다.)
- 생성된 TP의 상세 주소,경도,위도를 확인할 수 있다.
- 외국 주소도 지원한다.

### Function 3 : TP 설정기능

![3](https://user-images.githubusercontent.com/70352603/133383198-dbac9bf5-5fb7-42af-9415-e56e922c965b.png)

- Map 의 임의의 위치를 길게 누르게 되면 TP 설정이 가능해진다.
- TP를 클릭하면 해당 위치의 주소, 경도, 위도를 확인할 수 있다.
- TP의 말풍선을 클릭하면 해당 TP를 삭제할 수 있다.
- (+) 버튼을 클릭하면 사용자가 직접 위도, 경도 값을 입력하여 해당 위치에 TP를 추가할 수 있다.

### Function 4 : 현재 위치에서부터 TP까지의 거리 확인

![4](https://user-images.githubusercontent.com/70352603/133383208-87c243e8-569f-455d-ae55-77da5ab7f17f.png)

- TP를 설정하면 사용자의 현재 위치와 설정한 TP 사이의 거리를 확인할 수 있다.

### Function 5 : Site 설정

![5](https://user-images.githubusercontent.com/70352603/133383221-2e9f9848-848f-4e31-bdd0-399dca595f5a.png)

- 사용자가 TP의 일정 반경 내에 들어올 경우 Site 설정 가능하다는 메세지가 뜬다.
- Site 설정 버튼을 누르면 사용자의 현재 위치에 Site가 설정된다.
- 설정된 Site 정보는 하단에 기재된다.

### Function 6 : 오차 범위 설정

![6](https://user-images.githubusercontent.com/70352603/133383234-487ce023-7350-4d63-974c-b7cca1f2841c.png)

- 사용자가 TP의 어느정도의 반경 내에 들어와야 Site 설정이 가능할지에 따른 오차 범위를 설정한다.
- 오차 범위가 설정되면 TP의 오차범위 범주를 확인할 수 있도록 TP 주변에 원이 그려진다.

### Function 7 : TP 안테나 각도 설정

![7](https://user-images.githubusercontent.com/70352603/133383243-b7c4d0f6-0f08-4d2f-8c34-c8dbdba98edd.png)

- TP 각각에 대한 안테나 각도를 설정한다.
- Site가 모두 설정되면 안테나 각도와 Site 사이의 거리를 기반으로 빔아크를 계산하여 하단에 기재한다.

* 현재 에뮬레이터 시연으로 Site 두개가 모두 같은 위치에 찍히게 되어 거리와 빔아크의 계산값이 모두 0으로 나온다.

### ETC.

![8](https://user-images.githubusercontent.com/70352603/133383265-23bd1b23-1dd0-4669-907c-a3d98beed553.png)

- Clear 버튼 클릭 시, Site 과 marker 가 TP가 카메라가 현재 위치를 가리킨다.
- logout 버튼 혹은 어플을 나가더라도 사용자가 설정해 둔 TP, Site 가 존재하다면 해당 정보를 SharedPreferences를 통해 관리하여 정보를 저장한다.

## 개발 계획

- 서버 연결을 통해 Site 사이의 관계 상세분석
- 사용자 DB를 만들어서, login & join 기능 활성화
