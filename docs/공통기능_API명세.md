# 공통기능 API 명세 (v0.1)

PRD(`공통기능_PRD.md`) COM 번호 기준으로 정리. ERD는 `ERD.md`의 `Organization`, `Engineer` 엔티티 참고.

**설계 방향**: 부품관리시스템/재고공유 API명세와 동일한 원칙 — 단순 필드 수정은 `PATCH`로 묶고,
검증 절차가 있거나(비밀번호 변경) 부수효과가 있는(로그인) 액션만 전용 엔드포인트로 분리한다.
로그인 화면(지점 검색)과 로그인 자체는 세션이 없는 상태에서 호출되므로 인증이 필요 없는 유일한
엔드포인트다. 그 외 모든 엔드포인트는 로그인 세션의 지점으로 스코핑된다.

## 로그인

| Method | Path | 설명 | FR |
|---|---|---|---|
| GET | `/api/organizations?search=` | 로그인 화면의 지점 검색 드롭다운용 목록 조회. 인증 불필요(로그인 전 호출). `id`, `name`만 반환하고 비밀번호·전화번호·주소 등은 노출하지 않음 | COM-09 |
| POST | `/api/auth/login` | 지점 선택 + 비밀번호로 로그인, 세션 생성. 인증 불필요 | COM-02 |
| POST | `/api/auth/logout` | 로그아웃, 세션 종료 | COM-03 |

**Request Body** (`GET /organizations` 응답)

```json
{ "items": [ { "id": 7, "name": "강남점" }, { "id": 12, "name": "잠실점" } ] }
```

**Request Body** (`POST /auth/login`)

```json
{ "organizationId": 7, "password": "••••••" }
```

**Response Body** (`POST /auth/login`)

```json
{ "organizationId": 7, "organizationName": "강남점" }
```

- 세션은 로그아웃 전까지 별도 만료 없이 유지된다 (COM-03). 구현은 장기 유효 쿠키/토큰으로 처리

## 설정 — 지점 정보

| Method | Path | 설명 | FR |
|---|---|---|---|
| GET | `/api/organizations/me` | 로그인한 지점의 상세정보 조회 | - |
| PATCH | `/api/organizations/me` | 지점 상세정보(전화번호, 주소) 수정. `name`은 로그인 식별자를 겸해 이 엔드포인트로 수정 불가 (§10-3 확인 필요) | COM-05 |
| PATCH | `/api/organizations/me/password` | 비밀번호 변경. 현재 비밀번호 확인 절차가 있어 일반 필드 수정과 분리 | COM-04 |

**Response Body** (`GET /organizations/me`)

```json
{
  "id": 7,
  "name": "강남점",
  "phone": "02-1234-5678",
  "address": "서울시 강남구 ...",
  "latitude": 37.123,
  "longitude": 127.456
}
```

**Request Body** (`PATCH /organizations/me`, 보낸 필드만 수정)

```json
{ "phone": "02-1234-5678", "address": "서울시 강남구 ..." }
```

- `address`를 보내면 서버가 자동으로 지오코딩해 `latitude`/`longitude`를 갱신한다 (지점재고공유_PRD.md INV-07)

**Request Body** (`PATCH /organizations/me/password`)

```json
{ "currentPassword": "••••••", "newPassword": "••••••" }
```

- `currentPassword`가 일치하지 않으면 실패 응답

## 엔지니어 관리

| Method | Path | 설명 | FR |
|---|---|---|---|
| GET | `/api/engineers?activeOnly=true` | 로그인한 지점의 엔지니어 목록 조회. `activeOnly`(기본 `true`)는 RO 접수 화면의 담당 엔지니어 드롭다운용 — 비활성 엔지니어 제외 | COM-06, COM-07, COM-08 |
| POST | `/api/engineers` | 엔지니어 등록. `organizationId`는 요청 본문에 담지 않고 세션에서 자동 지정 | COM-06 |
| PATCH | `/api/engineers/{id}` | 이름·전화번호 수정 또는 `isActive`를 `false`로 바꿔 비활성화. 완전 삭제 엔드포인트는 없음(§ ERD.md 참고) | COM-06, COM-08 |

**Request Body** (`POST /engineers`)

```json
{ "name": "김엔지니어", "phone": "010-1234-5678" }
```

**Response Body** (공통 `Engineer` 객체, `GET`/`POST`/`PATCH` 공통)

```json
{ "id": 3, "organizationId": 7, "name": "김엔지니어", "phone": "010-1234-5678", "isActive": true }
```

**Request Body** (`PATCH /engineers/{id}`, 보낸 필드만 수정, 비활성화 예시)

```json
{ "isActive": false }
```

- 비활성화된 엔지니어는 `GET /api/engineers?activeOnly=true` 결과에서 빠지지만, 그 엔지니어가 이미
  배정된 과거 `RepairOrder.engineerId`는 그대로 유지된다 (공통기능_PRD.md COM-08, ERD.md 참고)

## Open Questions (구현 전 확인 필요)

- 최초 로그인(기본 비밀번호) 시 비밀번호 변경을 강제할지, 응답에 "변경 권장" 플래그만 내려줄지는 PRD §10-1 클라이언트 확정 후 구현
- 비밀번호 분실 시 재발급 절차(운영자 문의 vs 자체 재설정 API)는 PRD §10-2 클라이언트 확정 후 구현
- `PATCH /api/organizations/me`에서 `name` 변경 가능 여부는 PRD §10-3 클라이언트 확정 후 구현 — 가능하다고 정해지면 별도 엔드포인트 또는 이 엔드포인트에 필드 추가
- 로그인 세션 구현 방식(서버 세션 저장 vs 무상태 토큰)은 순수 구현 디테일 — 클라이언트 확인 불필요, 개발 시 결정
