프로필 추가
application-{profileName}.properties
    설정 추가

./gradlew bootRun --args='--spring.profiles.active=h2'
java -jar build/lib/SNAPSHOT....jar --spring.profiles.active=h2