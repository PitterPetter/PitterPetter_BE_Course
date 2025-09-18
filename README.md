# 💌 Loveventure - Course Service 

## 📌 서비스 개요

**Course Service**는 Loveventure 플랫폼의 핵심 기능인 **데이트 코스 추천 및 관리**를 담당하는 백엔드 서비스입니다. 

---

## 🚀 주요 기능
* **코스 상세 조회**

  * 전체 소요 시간
  * 코스별 개별 장소(POI) 상세 정보 제공
* **커플 페어링 연동**

  * Auth Service의 커플 온보딩을 기반으로 취향/예산을 합산하여 추천 최적화

---

## 🛠 기술 스택

* **Language:** Java 17
* **Framework:** Spring Boot 3.5.5
* **Database:** postgreSQL
* **AI 연동:** 내부 추천 엔진 모듈 + 외부 날씨/위치 API
* **Integration:** OpenFeign (Auth, Review Service와 연동)
* **Build Tool:** Gradle

---

## ⚙️ 환경 변수

```bash
DB_URL=jdbc:postgresql://localhost:port_number/loventure_course
DB_USERNAME=root
DB_PASSWORD=yourpassword
AI_RECOMMENDER_ENDPOINT=http://ai-service:8000/recommend
MAP_SDK_KEY=xxx
```

---

## ▶ 실행 방법

```bash
# 빌드
./gradlew clean build

# 실행
java -jar build/libs/loventure-course-service.jar
```

---

## 📡 API 엔드포인트 (예시)

### 🎯 추천

* `GET /courses/recommend` → 조건 입력 후 코스 추천 조회
* `GET /courses/{courseId}` → 코스 상세 조회
* `GET /courses/couple/{coupleId}` → 특정 커플의 추천 코스 목록 조회

### 🗺 POI

* `GET /courses/{courseId}/pois` → 코스 내 POI 목록 조회
* `GET /pois/{poiId}` → 특정 장소 상세 조회

---

## 🧩 브랜치 전략

* `main`: 배포용 안정 버전
* `develop`: 통합 개발 브랜치
* `feature/PIT-이슈번호`: 기능 단위 개발 브랜치

---

## 📜 커밋 규칙

* `feat`: 새로운 기능 추가
* `fix`: 버그 수정
* `docs`: 문서 수정
* `refactor`: 코드 리팩토링
* `test`: 테스트 코드
