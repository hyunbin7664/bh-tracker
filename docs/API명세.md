# API 명세 (v0.2)

Base path: `/api/repair-orders`. PRD(`부품관리시스템_PRD.md`) FR 번호 기준으로 정리.
ERD는 `ERD.md` 참고 — `Part.status`가 단일 진실 공급원이고 `RepairOrder.incoming_status`는 그 집계 파생값이다.

**설계 방향**: 상태 전이별로 액션 엔드포인트를 잘게 나누지 않고, 데이터 수정은 `PATCH` 하나로 통합한다.
상태값(`status`, `isCheckedIn` 등)도 그냥 필드로 취급해서 자유롭게 수정 가능하며, 서버가 전이 유효성을
검증해서 막지 않는다 (1인 운영 내부 도구라 유효성 검증보다 단순함을 우선). 외부로 실제 메시지를 발송하는
`/notice`만 부수효과가 있는 별도 액션으로 분리한다.

**공통 응답 객체 (`RepairOrder`)**

```json
{
  "id": 1,
  "roNumber": "RO-20260904-001",
  "vehicleNumber": "12가3456",
  "customerName": "홍길동",
  "customerPhone": "010-1234-5678",
  "engineerId": 3,
  "engineerName": "김엔지니어",
  "incomingStatus": "RECEIVED",
  "receivedDate": "2026-09-01",
  "appointmentDate": "2026-09-10",
  "isCheckedIn": false,
  "isRebooked": false,
  "notifiedAt": null,
  "parts": [
    { "id": 10, "partNumber": "P-1001", "partName": "브레이크 패드", "status": "RECEIVED", "receivedDate": "2026-09-01" },
    { "id": 11, "partNumber": "P-2002", "partName": "오일필터", "status": "RECEIVED", "receivedDate": "2026-08-28" }
  ]
}
```

## 접수

| Method | Path | 설명 | FR |
|---|---|---|---|
| POST | `/api/repair-orders` | RO 접수 + 필요 부품 목록 등록 (부품별 `status=AWAITING_ORDER` 기본값) | FR-01 |

**Request Body**

```json
{
  "roNumber": "RO-20260904-001",
  "vehicleNumber": "12가3456",
  "customerName": "홍길동",
  "customerPhone": "010-1234-5678",
  "engineerId": 3,
  "parts": [
    { "partNumber": "P-1001", "partName": "브레이크 패드" },
    { "partNumber": "P-2002", "partName": "오일필터" }
  ]
}
```

**Response Body**: 공통 `RepairOrder` 객체, `incomingStatus`/부품 전부 `AWAITING_ORDER`

## 조회

| Method | Path | 설명 | FR |
|---|---|---|---|
| GET | `/api/repair-orders/{id}` | RO 상세 조회 (부품 포함) | - |
| GET | `/api/repair-orders` | `tab`, `roNumber`, `engineerId` 쿼리 파라미터로 목록 필터링 (각각 optional, 조합 가능) | FR-12, FR-13 |

**탭 판별 로직** (FR-11-1로 `AWAITING_RETURN`이 부품 개별 트리거로도 붙을 수 있게 되면서, 예약 유효성까지 같이 봐야 함)

- **반품필요**: `incomingStatus = AWAITING_RETURN` **AND** (`appointmentDate`가 없거나 이미 지남)
- **진행중**: `isCheckedIn = false` **AND** (`incomingStatus != AWAITING_RETURN` **OR** `appointmentDate`가 아직 안 지남)
- **작업완료**: `isCheckedIn = true`
- **빨간 행 강조** (FR-11-1): 탭과 무관하게 `incomingStatus = AWAITING_RETURN`이면 항상 빨간색 (진행중 탭에 있어도 강조됨)

**Response Body** (`{id}` 상세): 공통 `RepairOrder` 객체

**Response Body** (목록): `{ "items": [공통 RepairOrder 객체, ...] }`

## 수정

| Method | Path | 설명 | FR |
|---|---|---|---|
| PATCH | `/api/repair-orders/{id}` | RO 필드 + `parts` 배열로 하위 부품까지 한 번에 수정, 상태 전이 검증 없음. 모든 Part가 `RECEIVED`가 되면 엔지니어에게 카톡 자동 발송(FR-04), Part 상태 변경 시 RO `incomingStatus` 자동 재계산 | FR-02, FR-03, FR-05, FR-10, FR-14, FR-15 |

**Request Body** (보낸 필드만 수정, `parts[].id`만 필수. `appointmentDate`를 기존 값 위에 새로 덮으면 재예약으로 간주)

```json
{
  "appointmentDate": "2026-09-10",
  "parts": [
    { "id": 10, "status": "RECEIVED", "receivedDate": "2026-09-01" }
  ]
}
```

**Response Body**: 수정 반영된 최신 공통 `RepairOrder` 객체

## 안내

| Method | Path | 설명 | FR |
|---|---|---|---|
| POST | `/api/repair-orders/{id}/notice` | 안내(1차 통보) 발송 — 전화 연결 실패 시 엔지니어가 클릭. 카카오 알림톡 실제 발송 부수효과 때문에 `PATCH`로 통합하지 않음 | FR-06 |

**Response Body**

```json
{ "id": 1, "notifiedAt": "2026-09-04" }
```

## 배치 (API 아님, 내부 스케줄러)

| 배치 | 조건 | 동작 | FR |
|---|---|---|---|
| 안내 자동 발송 (잠정) | 입고일 기준 영업일 3일 경과 & `appointmentDate` 미등록 & `notifiedAt` 미기록 | 안내 자동 발송, 영업시간 내 실행 | FR-06-1 |
| 미예약 반품 전환 | 안내 발송 후 영업일 3일 경과 & `appointmentDate` 미등록 | 그 RO의 모든 Part를 `AWAITING_RETURN`으로 일괄 전환 + 반품 예정 통지 발송 | FR-07 |
| 노쇼 감지 | 작업 예정일 경과 & `isCheckedIn=false` | 담당 엔지니어에게 노쇼 알림 | FR-08 |
| 재예약 실패 반품 전환 | 재예약 연락두절 또는 2회차 노쇼 | 그 RO의 모든 Part를 `AWAITING_RETURN`으로 일괄 전환 + 반품 예정 통지 발송 | FR-09, FR-10 |
| 반품기한 임박 전환 (잠정) | 부품 입고일 기준 반품기한(30일) D-N일 전 (N 미정, §11-13) | **그 부품 하나만** `AWAITING_RETURN`으로 전환 (RO 일괄 아님 — 부품마다 도착일이 달라서 개별 판단) | FR-11-1 |

## Open Questions (구현 전 확인 필요)

- FR-07 대기기간(3영업일, 잠정)은 PRD §11-6 클라이언트 확정 후 구현
- FR-06-1(안내 자동 발송 안전망) 자체가 필요한지, 3영업일 기준이 맞는지는 PRD §11-7 클라이언트 확정 후 구현 — 불필요하다고 확정되면 이 배치는 제거
- 노쇼 감지 배치 실행 주기는 PRD에 명시되지 않은 순수 구현 디테일 — 하루 1회로 충분, 단 실행 시각은 자정이 아니라 **영업시간 내**로 스케줄링 (NFR-05 알림톡 야간 발송 제한 때문에 자정에 돌리면 엔지니어 알림이 못 나가거나 다음날로 밀림)
- 반품기한 임박 전환(FR-11-1)의 D-N일 기준은 PRD §11-13 클라이언트 확정 후 구현
