# 선박 예매 시스템 - 백엔드

## 기술 스택
- Java 17
- Spring Boot 3.4.2
- Spring Data JPA
- H2 (인메모리 DB)
- Lombok

## 실행 방법
1. 저장소 클론
2. 프로젝트 루트에서 실행
   ./gradlew bootRun
3. 서버 실행 확인: http://localhost:8080

## API 목록

### 회원
| Method | URL | 설명 |
|--------|-----|------|
| POST | /api/users/signup | 회원가입 |
| POST | /api/users/login | 로그인 |
| GET | /api/users/{id} | 회원 정보 조회 |

### 운항 조회
| Method | URL | 설명 |
|--------|-----|------|
| GET | /api/schedules/available-dates | 예약 가능 날짜 조회 |
| GET | /api/schedules/search | 예약 가능 선박 조회 |

### 예매
| Method | URL | 설명 |
|--------|-----|------|
| POST | /api/reservations | 예매 신청 |
| GET | /api/reservations/user/{userId} | 예매 내역 조회 |
| PATCH | /api/reservations/{id}/cancel | 예매 취소 |

## 참고
- 서버 시작 시 샘플 데이터 자동 생성 (선사, 선박, 운항 일정)
- H2 콘솔: http://localhost:8080/h2-console
