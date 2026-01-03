# Java 설치 가이드

이 프로젝트는 **Java 17 이상**이 필요합니다.

## 추천 설치 방법

### 1. Microsoft Build of OpenJDK (가장 추천 - Windows에서 설치 간편)
- **다운로드 URL**: https://www.microsoft.com/openjdk
- **특징**: Windows에서 MSI 설치 파일로 간편하게 설치 가능
- **장점**: Microsoft에서 공식 지원, 설치가 매우 쉬움
- **다운로드**: "Download 17 LTS" 또는 "Download 21 LTS" 버튼 클릭

### 2. Eclipse Temurin (Adoptium) (추천)
- **다운로드 URL**: https://adoptium.net/
- **특징**: Eclipse Foundation에서 관리하는 OpenJDK 빌드
- **다운로드 방법**:
  1. "Latest LTS Release" 또는 "17" 선택
  2. Operating System: Windows
  3. Architecture: x64
  4. Package Type: JDK
  5. 다운로드 후 실행 파일로 설치

### 3. Oracle JDK
- **다운로드 URL**: https://www.oracle.com/java/technologies/downloads/#java17
- **특징**: Oracle 공식 JDK
- **주의**: Oracle JDK는 상업적 사용 시 라이선스 확인 필요

### 4. Amazon Corretto
- **다운로드 URL**: https://aws.amazon.com/corretto/
- **특징**: Amazon에서 관리하는 OpenJDK 빌드
- **다운로드**: "Download" 버튼 클릭 → "Corretto 17" 선택

## 설치 후 확인

설치가 완료되면 새 터미널/명령 프롬프트를 열고 다음 명령어로 확인하세요:

```bash
java -version
```

예상 출력:
```
openjdk version "17.0.x" ...
```

## JAVA_HOME 환경 변수 설정 (필요시)

일부 IDE나 빌드 도구에서 JAVA_HOME이 필요할 수 있습니다.

### Windows:
1. 시스템 속성 → 고급 시스템 설정 → 환경 변수
2. 시스템 변수에서 "새로 만들기"
3. 변수 이름: `JAVA_HOME`
4. 변수 값: Java가 설치된 경로 (예: `C:\Program Files\Microsoft\jdk-17.x.x`)

### 설치 경로 확인:
```bash
where java
```

위 명령어로 Java 실행 파일 경로를 확인한 후, 그 상위 디렉토리가 JAVA_HOME입니다.

## 프로젝트 실행

Java 설치가 완료되면:

```bash
cd always-svc
./mvnw.cmd spring-boot:run
```




