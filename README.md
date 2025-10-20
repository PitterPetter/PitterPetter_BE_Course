# 💌 PitterPetter Course Service

## 📌 서비스 개요

**PitterPetter Course Service**는 커플을 위한 **데이트 코스 추천 및 관리** 백엔드 서비스입니다. 
도메인 주도 설계(DDD)를 적용하여 안정적이고 확장 가능한 마이크로서비스 아키텍처를 구현했습니다.

---

## 🚀 주요 기능

### 🎯 코스 관리
- **코스 생성/조회/삭제**: 커플별 개인화된 데이트 코스 관리
- **코스 평점 시스템**: 0-10점 리뷰 시스템
- **트랜잭션 보장**: 단일 트랜잭션으로 데이터 일관성 보장

### 🗺 POI (Point of Interest) 관리
- **중앙화된 장소 정보**: 중복 방지를 위한 UPSERT 패턴
- **데이터 정규화**: 영업시간, 태그, 링크 등 체계적 관리
- **JSONB 활용**: 복잡한 데이터 구조 유연하게 처리

### 🔐 보안 및 인증
- **JWT 기반 인증**: OAuth2 Resource Server
- **데이터 격리**: couple_id 기반 완전 격리
- **권한 관리**: 커플별 데이터 접근 제어

---

## 🛠 기술 스택

### Backend
- **Java 17** + **Spring Boot 3.4.10**
- **Spring Security** + **OAuth2 Resource Server**
- **Spring Data JPA** + **PostgreSQL**
- **SpringDoc OpenAPI 3** (Swagger)

### Build & Deploy
- **Gradle** 빌드 도구
- **Docker** 컨테이너화
- **Spring Cloud Config** 중앙 설정 관리
- **API Gateway** 연동
- **Kubernetes** 오케스트레이션

### 추가 라이브러리
- **JWT (jjwt)**: JWT 토큰 처리
- **Lombok**: 코드 간소화
- **Jackson**: JSON 처리
- **Spring Cloud Config**: 중앙 설정 관리
- **Spring Cloud Gateway**: API Gateway 연동

---

## 🏗️ 아키텍처 설계

### 마이크로서비스 아키텍처
```
API Gateway → Course Service (8081)
     ↓              ↓
Config Server    Database
     ↓              ↓
Auth Service    PostgreSQL
```

### 도메인 모델
```
Course (1) ←→ (N) PoiSet (N) ←→ (1) POI
  ↓
Couple (외부 시스템)
```

### 핵심 엔티티
- **Course**: 커플별 코스 메타데이터 (UUID 기반)
- **POI**: 중앙화된 장소 정보 (위치 기반 UNIQUE)
- **PoiSet**: 코스-POI 연결 테이블 (순서 포함)
- **Category**: 장소 카테고리 enum

### 성능 최적화
- **인덱스 전략**: couple_id, category, mood_tag, location 인덱스
- **JOIN FETCH**: N+1 쿼리 해결
- **LAZY 로딩**: 메모리 효율성
- **성능 모니터링**: 처리 시간 및 중복률 추적

---

## ⚙️ 환경 설정

### Config Server 연동
이 서비스는 **Spring Cloud Config Server**를 통해 중앙화된 설정 관리를 사용합니다.

```yaml
# application.yaml
spring:
  config:
    import: "optional:configserver:"
  cloud:
    config:
      uri: http://config-server.config-server.svc.cluster.local:80
      name: course-service
      label: main
      fail-fast: true
  application:
    name: course-service
  profiles:
    active: prod
```

### API Gateway 연동
**API Gateway**를 통해 외부 요청을 라우팅하며, 내부 서비스 포트는 8081을 사용합니다.

```bash
# 내부 서비스 포트
SERVER_PORT=8081

# API Gateway를 통한 외부 접근
# Gateway URL: http://api-gateway:8080
# Service Path: /course-service
```

---

## ▶ 실행 방법

### 로컬 개발 환경
```bash
# 의존성 설치
./gradlew build

# 애플리케이션 실행 (Config Server 연동)
./gradlew bootRun
```

### Docker 실행 (마이크로서비스 환경)
```bash
# Docker 이미지 빌드
docker build -t pitterpetter-course-service .

# 컨테이너 실행 (Config Server 연동)
docker run -p 8081:8081 \
  --network microservices-network \
  pitterpetter-course-service
```

### Kubernetes 배포
```bash
# Config Server와 함께 배포
kubectl apply -f k8s/course-service-deployment.yaml
kubectl apply -f k8s/course-service-service.yaml
```

---

## 📡 API 엔드포인트

### 내부 서비스 엔드포인트 (포트 8081)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/courses` | 코스 생성 | ✅ |
| GET | `/api/courses` | 커플별 코스 목록 조회 | ✅ |
| DELETE | `/api/courses/{courseId}` | 코스 삭제 | ✅ |
| PATCH | `/api/courses/{courseId}/review` | 코스 평점 업데이트 | ✅ |

### API Gateway를 통한 외부 접근
| Method | Gateway Endpoint | 설명 | 인증 |
|--------|------------------|------|------|
| POST | `/api/courses` | 코스 생성 | ✅ |
| GET | `/api/courses` | 커플별 코스 목록 조회 | ✅ |
| DELETE | `/api/courses/{courseId}` | 코스 삭제 | ✅ |
| PATCH | `/api/course/{courseId}/review` | 코스 평점 업데이트 | ✅ |

### API 문서
- **내부 Swagger UI**: `http://localhost:8081/swagger-ui.html`
- **Gateway를 통한 접근**: `http://api-gateway:8080/course-service/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8081/v3/api-docs`

---

## 🗄️ 데이터베이스 설계

### 주요 테이블
- **course**: 코스 정보 (UUID 기반 ID)
- **poi**: 장소 정보 (위치 기반 UNIQUE)
- **poi_set**: 코스-POI 연결 (순서 포함)
- **poi_food_tags**: POI 음식 태그 (ElementCollection)

### 인덱스 전략
- **course**: `couple_id`, `couple_id + created_at`
- **poi_set**: `course_id + order`, `poi_id`
- **poi**: `category`, `mood_tag`, `lat + lng`

---

## 🔧 개발 가이드

### 프로젝트 구조
```
src/main/java/com/example/course/
├── api/                    # API 레이어
│   ├── controller/        # REST 컨트롤러
│   └── dto/              # 요청/응답 DTO
├── config/               # 설정 클래스
├── domain/               # 도메인 모델
│   ├── service/          # 도메인 서비스
│   └── Category.java     # 카테고리 enum
├── exception/            # 예외 처리
├── jwt/                  # JWT 처리
├── repository/           # 데이터 접근
└── service/              # 비즈니스 로직
```

### 코드 스타일
- **도메인 주도 설계**: 비즈니스 로직을 도메인에 캡슐화
- **클린 아키텍처**: 계층 분리 및 의존성 역전
- **단일 책임 원칙**: 각 클래스의 명확한 역할 분담

---

## 🧪 테스트

### 단위 테스트
```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "CourseServiceTest"
```

### 통합 테스트
```bash
# 통합 테스트 실행
./gradlew integrationTest
```

---

## 📊 모니터링

### 성능 지표
- **응답 시간**: p95 150-200ms 목표
- **처리량**: 1000 req/s 목표
- **중복률**: POI 중복률 1% 미만

### 로그 모니터링
- **코스 생성 시간**: 처리 시간 측정
- **POI 중복 감지**: 중복률 추적
- **에러 로그**: 상세한 에러 정보

---

## 🚀 배포

### 마이크로서비스 환경 배포
```bash
# Docker 이미지 빌드
docker build -t pitterpetter-course-service:latest .

# 마이크로서비스 네트워크에서 실행
docker run -d \
  --name course-service \
  --network microservices-network \
  -p 8081:8081 \
  pitterpetter-course-service:latest
```

### Kubernetes 배포 (Config Server 연동)
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: course-service
  namespace: microservices
spec:
  replicas: 3
  selector:
    matchLabels:
      app: course-service
  template:
    metadata:
      labels:
        app: course-service
    spec:
      containers:
      - name: course-service
        image: pitterpetter-course-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SERVER_PORT
          value: "8081"
---
apiVersion: v1
kind: Service
metadata:
  name: course-service
  namespace: microservices
spec:
  selector:
    app: course-service
  ports:
  - port: 8081
    targetPort: 8081
  type: ClusterIP
```



## 🧩 브랜치 전략

- **main**: 배포용 안정 버전
- **develop**: 통합 개발 브랜치
- **feature/PIT-이슈번호**: 기능 단위 개발 브랜치
- **hotfix/PIT-이슈번호**: 긴급 수정 브랜치

---

## 📜 커밋 규칙

- **feat**: 새로운 기능 추가
- **fix**: 버그 수정
- **docs**: 문서 수정
- **refactor**: 코드 리팩토링
- **test**: 테스트 코드
- **perf**: 성능 개선
- **chore**: 빌드 과정 또는 보조 도구 변경

