# KakaoTalk Connector for Oracle Chatbot

오라클 챗봇에서는 Facebook, Webhook, Web, iOS, Android등의 여러가지 타입의 채널을 통해 SNS와 연결할 수 있습니다. 카카오톡은  webhook 의 채널 방식을 통해 연결이 가능합니다. 
기본적으로 카카오톡의 API는 request/response 방식의 동기화 방식입니다. 이에 반해 오라클 챗봇의 webhook은 비동기 방식입니다. 따라서 카카오톡의 동기와 오라클 챗봇의 비동기를 서로 연결해 줄 필요가 있습니다.

![Alt text](https://monosnap.com/image/c0oS6FmYVhCD90ZydpoTlXmkMDX0Bt)

다음에 설명드릴 항목은 카카오톡과 오라클 챗봇의 동기/비동기 방식을 서로 연결할 커넥터에 대한 설명입니다.

카카오톡과 오라클 챗봇을 연결하기 위해서는 다음과 같은 환경이 필요합니다.
1. Oracle Autonomous Digital Assistant
    - 오라클 챗봇입니다.
1. Oracle Application Container Cloud Service (ACCS)
    - 카카오톡과 오라클 챗봇을 연결시켜줄 커넥터입니다.
1. 카카오 플러스 친구 (https://business.kakao.com/)
    - 오라클 챗봇과 대화할 카카오톡 아이디입니다.

그리고 개발을 위해 프로그램이 필요합니다.
1. Java SE 8 (https://www.oracle.com/technetwork/java/javase/downloads/index.html)
1. Spring Tools (http://spring.io/tools)



# 오라클 챗봇 - 채널 생성

1. 채널을 생성하기 위하여 오른쪽 아이콘에서 `Settings` 아이콘을 클릭하고 `Channels` 를 클릭하여 채널을 생성할 화면을 엽니다. 

    <kbd>![Alt text](https://monosnap.com/image/85eZgcPEY7mIFVguiOk0OLXv3nLXvD.png)</kbd>
1. `+ Channel`을 클릭하고 다음을 입력합니다.
    - Name : kakao (원하는 이름을 입력합니다)
    - Description : kakao channel (원하는 설명을 입력합니다)
    - Channel Type : Webhook
    - Platform Version : 1.0 (Simple Model)
    - Outgoing Webhook URI : https://localhost

    <kbd>![Alt text](https://monosnap.com/image/fCkFA4dkzumIJwgCDAaym1HTyXPjsA.png)</kbd>

1. `Create`를 클릭하면 다음과 같이 채널이 만들어집니다.

    <kbd>![Alt text](https://monosnap.com/image/GaH5JcEzWphl6Lpek0GMC4NiBNEzwL.png)</kbd>

    다음의 두 항목은 카카오 커넥터를 만들 때 참조될 항목입니다.
    - Secret Key
    - Webhook URL

    다음의 한 항목은 카카오 커넥터를 ACCS에 배포하고 난 후에 수정할 항목입니다.
    - Outgoing Webhook


    
# 카카오 커텍터 만들기
1. 소스를 가져옵니다. 소스는 https://github.com/jonggyoukim/kakaoconnector-for-chatbot 에 있습니다.

    ~~~
    git clone https://github.com/jonggyoukim/kakaoconnector-for-chatbot.git
    ~~~

1. STS(SpringToolSuite4)에서 해당 소스를 불러옵니다. 방법은 이클립스와 동일합니다.

    <kbd>![Alt text](https://monosnap.com/image/Vq4x0tEePF1NTLsMTGeI1M8jQc4g5n.png)</kbd>

1. src/main/resources/application.properties 을 열어 수정합니다.

    ~~~
    oracle.bots.kakao.uri={위에 생성한 채널의 Webhook URL}
    oracle.bots.kakao.secret={위에 생성한 채널의 Secret Key}
    ~~~

1. maven 으로 컴파일 합니다. 컴파일 후 target/kakaotalk-0.9.jar 가 생성될 것입니다.
    - Goals은 "package"로 합니다.

1. target/kakaotalk-0.9.jar를 deploy 디렉토리에 복사합니다. deploy디렉토리에 두개의 파일이 존재합니다.
    ~~~
    kakaotalk-0.9.jar
    manifest.json 
    ~~~

1. zip 파일을 만듭니다. ACCS 배포단위입니다.
    ~~~
    cd deploy
    zip kakaotalk.zip * 
    ~~~
    윈도우에서는 zip 툴로 만듭니다. zip 안에는 디렉토리가 없이 두개의 파일만 존재해야 합니다.



# 카카오 커넥터를 ACCS 에 배포하기

위에서 만들어진 zip 파일을 사용하여 ACCS에 배포합니다.

1. Application Continaer 의 서비스 콘솔을 엽니다.

    <kbd>![Alt text](https://monosnap.com/image/iu06DHsFnVW5LERWe4DoalWUsxIJH2.png)</kbd>

    만약 Application Container 가 보이지 않으면 "`대시보드 사용자정의`" 를 눌러 Application Container 가 보이도록 설정합니다.

    <kbd>![Alt text](https://monosnap.com/image/jimvaQKmw5zLpHKu3nm2wxJFGwAzIP.png)</kbd>

1. 콘솔이 열리면 우측의 "`Create Application`" 을 클릭합니다. 팝업이 뜨면 "`Java SE`" 를 선택합니다.
    <kbd>![Alt text](https://monosnap.com/image/at8GZhxr6ZcKtwa3iuWvkoZGyhsBAY.png)</kbd>

1. 다시 Create Application 콘솔이 열리면 다음과 같이 입력합니다.
    - Name : kakaotalk (원하는 이름을 입력합니다.)
    - Application : 위에서 만든 kakaotalk.zip을 선택합니다.
    - Instances : 1
    - Memory : 1
    - Region : uscom-central-1

    <kbd>![Alt text](https://monosnap.com/image/N3SEtmJLGpV9ZeWuAS7HBGPVG1SaXt.png)</kbd>

    입력후 Create 버튼을 누릅니다. 

1. 약 2분정도 후에 애플리케이션이 구동중인 것을 볼 수 있습니다.

    <kbd>![Alt text](https://monosnap.com/image/0zQVkNgzvVf4vCrrWwiUi9VzZRzwbd.png)</kbd>

    애플리케이션에 접근할 수 있는 URL의 빨간 네모안의 URL입니다. 이 URL은 아래의 `카카오 플러스 친구`에서 입력할 값입니다.

    만약 계속 진행중 보인다면 우측의 리로드 아이콘을 클릭하십시오.



# 카카오 플러스 친구

1. 우선 카카오톡과 오라클 챗봇을 연결하기 위해서는 플러스 친구 아이디가 필요합니다. https://business.kakao.com/ 에 접속해서 플러스 친구를 만듭니다.

    <kbd>![Alt text](https://monosnap.com/image/gxflnSsZPbzwScV6JCQklM2XrhJ6Up.png)</kbd>

1. 만들어진 플러스 친구를 클릭하고 왼쪽 메뉴에서 `스마트채팅`을 클릭하고 나온 화면에서 `API형`의 수정마크를 클릭합니다.

    <kbd>![Alt text](https://monosnap.com/image/y3Hkj0dgSZ3zXpwtEXJ3qNH3DdT91E.png)</kbd>

1. 다음의 항목을 입력합니다.
    - 앱이름 : chatbot (원하는 이름을 입력합니다)
    - 앱URL : ACCS에 배포한 후 출력되는 url 을 입력합니다.
    - 앱설명 : oracle chatbot  (원하는 설명을 입력합니다)

    <kbd>![Alt text](https://monosnap.com/image/TFyyFBeJCxx0E9HMYN0tJPPN5lVf1R.png)</kbd>

1. 앱URL을 입력한 후 `API 테스트`를 클릭하여 테스트 완료합니다. 통과되지 않으면 저장이 되지 않습니다.

    <kbd>![Alt text](https://monosnap.com/image/I5tX1WKqjT58LSkf2AeiTYIIWnZDkW.png)</kbd>

1. 하위의 API형 저장하기를 눌러 저장 한 후, 시작하기를 눌러 시작을 합니다.

1. 이제 카카오톡에서 해당 플러스친구를 선택해 채팅하면 됩니다.
