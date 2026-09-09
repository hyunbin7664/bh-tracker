# 부품관리시스템 API 명세 (v0.4)

Base path: `/api/repair-orders`. PRD(`부품관리시스템_PRD.md`) FR 번호 기준으로 정리.
ERD는 `ERD.md` 참고 — `Part.status`(2단계, ORDERED/RECEIVED, 접수 즉시 ORDERED로 시작 §11-14)는 그대로
집계 파생 구조지만, 반품 관련(`AWAITING_RETURN`/`RETURNED`)은 §11-11(RO 단위 확정) 이후 **RO 단위
직접 설정값**으로 바뀌었다.

**v0.4 변경**: 여러 지점이 하나의 시스템을 공용으로 쓰게 되면서(§11-15), 모든 엔드포인트는 로그인
세션(`공통기능_PRD.md` 참고)의 지점으로 자동 스코핑된다. 즉 조회는 항상 로그인한 지점의 RO만
대상이며, 생성 시 `orgId`를 요청 본문에 담지 않고 세션에서 자동으로 채운다.

**설계 방향**: 상태 전이별로 액션 엔드포인트를 잘게 나누지 않고, 데이터 수정은 `PATCH` 하나로 통합한다.
상태값(`status`, `isCheckedIn`, `incomingStatus`의 반품 관련 값 등)도 그냥 필드로 취급해서 자유롭게
수정 가능하며, 서버가 전이 유효성을 검증해서 막지 않는다 (1인 운영 내부 도구라 유효성 검증보다 단순함을
우선). 외부로 실제 메시지를 발송하는 `/notice`만 부수효과가 있는 별도 액션으로 분리한다.

**공통 응답 객체 (`RepairOrder`)**

```json
{
  "id": 1,
  "orgId": 5,
  "roNumber": "RO-20260904-001",
  "vehicleNumber": "12가3456",
  "customerName": "홍길동",
  "customerPhone": "010-1234-5678",
  "engineerId": 3,
  "engineerName": "김엔지니어",
  "incomingStatus": "RECEIVED",
  "receivedDate": "2026-09-01",
  "appointmentDate": "2026-09-10",
  "rebookingDate": null,
  "isCheckedIn": false,
  "notifiedDate": null,
  "renotifiedDate": null,
  "parts": [
    { "id": 10, "partNumber": "P-1001", "partName": "브레이크 패드", "status": "RECEIVED", "receivedDate": "2026-09-01" },
    { "id": 11, "partNumber": "P-2002", "partName": "오일필터", "status": "RECEIVED", "receivedDate": "2026-08-28" }
  ]
}
```

## 접수

| Method | Path | 설명 | FR |
|---|---|---|---|
| POST | `/api/repair-orders` | RO 접수 + 필요 부품 목록 등록 (부품별 `status=ORDERED` 기본값, 접수 즉시 주문중으로 시작 §11-14). `orgId`는 요청 본문에 담지 않고 로그인 세션에서 자동 지정 (v0.4). `engineerId`는 `is_active=true`인 엔지니어만 선택 가능 | FR-01 |

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

**Response Body**: 공통 `RepairOrder` 객체, `incomingStatus`/부품 전부 `ORDERED`

## 조회

| Method | Path | 설명 | FR |
|---|---|---|---|
| GET | `/api/repair-orders/{id}` | RO 상세 조회 (부품 포함, 부품별 `receivedDate` 표시 §11-10) | FR-16 |
| GET | `/api/repair-orders` | `tab`, `roNumber`, `engineerId` 쿼리 파라미터로 목록 필터링 (각각 optional, 조합 가능). 항상 로그인한 지점(`orgId`)으로 스코핑되어 다른 지점 RO는 조회되지 않음 (v0.4) | FR-12, FR-13 |

**탭 판별 로직** (v0.3: 반품이 항상 RO 단위이므로 v0.2보다 단순해짐 — 더 이상 "반품예정인데 진행중에 남는" 예외 없음)

- **반품필요**: `incomingStatus = AWAITING_RETURN`
- **진행중**: `isCheckedIn = false` **AND** `incomingStatus != AWAITING_RETURN`
- **작업완료**: `isCheckedIn = true`

**사무실 모니터링 표시** (FR-06-1, 배치 아닌 조회 시점 계산): `incomingStatus = RECEIVED` **AND** `appointmentDate`가 없음 **AND** `notifiedDate`이 없음 인 건은 목록에서 별도로 표시해 사무실이 인지하고 엔지니어에게 직접 연락을 지시할 수 있게 한다 (고객에게 나가는 자동 문자는 아님)

**Response Body** (`{id}` 상세): 공통 `RepairOrder` 객체

**Response Body** (목록): `{ "items": [공통 RepairOrder 객체, ...] }`

## 수정

| Method | Path | 설명 | FR |
|---|---|---|---|
| PATCH | `/api/repair-orders/{id}` | RO 필드 + `parts` 배열로 하위 부품까지 한 번에 수정, 상태 전이 검증 없음 | FR-03, FR-05, FR-08, FR-09, FR-10, FR-14, FR-15 |

**Request Body** (보낸 필드만 수정, `parts[].id`만 필수)

- `appointmentDate`: 1차 작업 예정일 등록
- `rebookingDate`: 2차 작업 예정일(재예약일) 등록 — 노쇼 후 재예약 시 `appointmentDate`를 덮어쓰지 않고 이 필드에 별도로 저장 (둘 다 화면에 표시, §11-8)
- `isCheckedIn`: 차량입고(방문) 등록
- `incomingStatus`: **`AWAITING_RETURN` / `RETURNED`만 직접 설정 가능** (반품 처리, RO 단위, FR-09/FR-14). `ORDERED`/`RECEIVED`는 `parts[].status` 집계로만 결정되므로 여기 보내도 무시됨
- `parts[].status`: `ORDERED` / `RECEIVED` 중 하나

```json
{
  "rebookingDate": "2026-09-15",
  "parts": [
    { "id": 10, "status": "RECEIVED", "receivedDate": "2026-09-01" }
  ]
}
```

**Response Body**: 수정 반영된 최신 공통 `RepairOrder` 객체. `parts[].status`가 바뀌어 RO에 걸린 모든 부품이
`RECEIVED`가 되면 `incomingStatus`도 자동으로 `RECEIVED`로 재계산되고 담당 엔지니어에게 카톡이 자동
발송된다 (FR-04)

## 안내

| Method | Path | 설명 | FR |
|---|---|---|---|
| POST | `/api/repair-orders/{id}/notice` | 안내 최초 발송 — 전화 연결 실패 시 엔지니어가 클릭. `notifiedDate` 기록. 카카오 알림톡 실제 발송 부수효과 때문에 `PATCH`로 통합하지 않음 | FR-06 |

**Response Body**

```json
{ "id": 1, "notifiedDate": "2026-09-04" }
```

## 배치 (API 아님, 내부 스케줄러)

| 배치 | 조건 | 동작 | FR |
|---|---|---|---|
| 안내 자동 재전송 | `notifiedDate` 존재 & 영업일 3일 경과 & `appointmentDate` 미등록 & `renotifiedDate` 미기록 | 안내를 동일 내용으로 1회 자동 재전송, `renotifiedDate` 기록. 영업시간 내 실행 | FR-06-2 |
| 미예약 반품 전환 | `renotifiedDate` 존재 & 영업일 3일(잠정, §11-12) 경과 & `appointmentDate` 미등록 | 반품 예정 통지 발송 + RO `incomingStatus = AWAITING_RETURN` 전환 (RO 단위) | FR-07 |
| 노쇼 감지 | 유효 작업 예정일(`rebookingDate` 있으면 그것, 없으면 `appointmentDate`) + 2일 경과(영업일/캘린더일 기준 §11-13 확인 필요) & `isCheckedIn=false` | 담당 엔지니어에게 노쇼 알림 | FR-08 |
| 재예약 실패 반품 전환 | 재예약 연락두절 또는 2차 작업 예정일 노쇼 재발 | 반품 예정 통지 발송 + RO `incomingStatus = AWAITING_RETURN` 전환 (RO 단위) | FR-09, FR-10 |

## Open Questions (구현 전 확인 필요)

- 안내 재전송 후 반품예정 전환까지의 대기기간(영업일 3일, 잠정)은 PRD §11-12 클라이언트 최종 확정 후 구현
- 노쇼 유예기간(+2일)이 영업일 기준인지 캘린더일 기준인지는 PRD §11-13 클라이언트 확정 후 구현
- 노쇼 감지 배치 실행 주기는 PRD에 명시되지 않은 순수 구현 디테일 — 하루 1회로 충분, 단 실행 시각은 자정이 아니라 **영업시간 내**로 스케줄링 (NFR-05 알림톡 야간 발송 제한 때문에 자정에 돌리면 엔지니어 알림이 못 나가거나 다음날로 밀림)
- (v0.2 대비 삭제됨) 반품기한 임박 자동 전환/강조 기능(구 FR-11-1)과 그 D-N 기준일 관련 Open Question은 PRD §11-11에서 기능 자체가 불필요한 것으로 확정되어 더 이상 유효하지 않음
