

### 프로필 추가
`application-{profileName}.properties` 파일로 프로필별 설정 추가 가능

> ⚠️ **주의:** Spring 프로필(`spring.profiles.active`)과 `current_cctv_status_cache` 테이블 내 `environment_mode` (ENUM)는 별개의 개념입니다.
> - `environment_mode`: 개발 환경 구분용 필드
> - `spring.profiles.active`: Spring 설정 파일 그룹 지정용 프로필

### 실행 방법
```bash
./gradlew clean build
java -jar build/libs/SNAPSHOT....jar --spring.profiles.active={profileName}
```

예를 들어, test 프로필을 사용하고 싶다면 다음과 같이 실행합니다.  
`test` 프로필은 여러 데이터 소스에 접근하지 못하는 환경에서 추천됩니다.

```bash
--spring.profiles.active=test
```

