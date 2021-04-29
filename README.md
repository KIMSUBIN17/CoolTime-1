# CoolTime

## 소개

교내 기숙사에서 사용하고 있는 세탁기는 공용 세탁기이기 때문에 세탁기를 사용하기 위해 기다리는 일이 빈번했습니다.

이 기다림을 최소화 하고자 세탁기 알람을 만들었습니다.

## 영상

[![FiledPlay](https://img.youtube.com/vi/rt-ju-J1BGk/sddefault.jpg)](https://www.youtube.com/watch?v=rt-ju-J1BGk)

## 설명

1. Application
   1. 메인 화면
    <img src = "./img/main.jpg" width="30%" height="30%">


    - 처음 메인 화면

    2. 내 타이머
   
     - <img src = "./img/checktime.jpg" width="30%" height="30%">

     - 현재 저장되어 있는 세탁기의 값이 없을경우 0시0분으로 고정.
     - 현재 저장되어 있는 세탁기 값이 있을경우 실시간으로 시간 출력.
  
    3. QR 코드
    -  누르면 즉시 리더기가 작동 됨.
    -  QR코드를 읽으면 내 타이머로 자동으로 이동.
    -  만약 이미 세탁기 값이 있을 경우 지우고 새로 할당받음

    4. 실시간 사용 현황
   
    - <img src = "./img/checkwasher.jpg" width="30%" height="30%">
      
    - 색이 있는 옷 이미지는 이미 사용중인 세탁기이며, 눌렀을 경우 누른 세탁기의 남은 시간 표기.
    - 색이 없는 옷은 사용중이 아닌 세탁기이며, 눌렀을 경우 밑에 사용중이 아니라고 표기.

    5. 설정
    
    - <img src = "./img/setting.jpg" width="30%" height="30%">

    - 각종 설정을 할 수 있음.
    - 미리 알림을 켜 놓은 경우 설정한 시간 전에 알람이 울림.
    - 남은 자리 알림은 현재 남은 세탁기가 1개 이상 존재할때 노티피케이션으로 알려줌.
    - 알림음 선택
      - <img src = "./img/seletalarm.jpg" width="30%" height="30%">
      - 각종 알람을 터치하여 들어봄.
      - 이전에 터치한 알람과 같은 알람이면 선택이 됨.
      - 다른 알람일 경우에 다시 들어볼 수 있음.

## 사용한 서비스

[AndoidStudio](https://developer.android.com/studio?gclid=Cj0KCQjwsqmEBhDiARIsANV8H3YxYG8duUJj33uMteSyiUgjezs6i-E1J8vhvbZMpsYvl50JqGdzuOAaAiR3EALw_wcB&gclsrc=aw.ds) : 안드로이드 플랫폼 어플리케이션 제작 프로그램

[Firebase](https://firebase.google.com/?hl=ko) : 서버와 통신 할 클라우드

## 제작 기간
2019년 10월 15일 ~ 2019년 11월 29일
