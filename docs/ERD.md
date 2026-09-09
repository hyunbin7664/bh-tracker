# ERD (v0.4)

PRD(`부품관리시스템_PRD.md`) v0.3 기준 데이터 모델. 3차 클라이언트 피드백으로 반품 처리 단위가
**RO 단위로 확정**되면서(§11-11), v0.2에서 "Part.status가 단일 진실 공급원" 이었던 설계가
일부 되돌아갔다 — 초기 단계(주문/입고)는 여전히 Part 집계, 반품 관련(§11-11 확정 이후)은 RO 단위
독립 액션으로 분리되었다.

v0.4에서는 `공통기능_PRD.md`(로그인·지점·엔지니어 등록)와 `지점재고공유_PRD.md`(장기재고 공유)를
반영해 `ORGANIZATION`, `STOCK` 엔티티를 추가하고, 여러 지점이 하나의 시스템을 공용으로 쓰는 구조에 맞춰
`REPAIR_ORDER`/`ENGINEER`에 소속 지점을 연결했다 (부품관리시스템_PRD.md §11-15 후속 작업).
DB 스키마는 세 PRD 전체가 공유하는 하나의 스키마라, ERD 문서도 이 파일 하나로 통합 관리한다
(API 명세는 도메인별로 `부품관리시스템_API명세.md`, `재고공유_API명세.md`, `공통기능_API명세.md`로 분리).

```mermaid
erDiagram
    ORGANIZATION ||--o{ REPAIR_ORDER : "소속"
    ORGANIZATION ||--o{ ENGINEER : "소속"
    ORGANIZATION ||--o{ STOCK : "보유"
    ORGANIZATION ||--o{ STOCK_CLAIM_LOG : "조달해줌(source_org_id)"
    ENGINEER ||--o{ REPAIR_ORDER : "담당"
    REPAIR_ORDER ||--o{ PART : "포함"
    PART ||--o{ STOCK_CLAIM_LOG : "조달 이력"

    ORGANIZATION {
        bigint id PK
        varchar name UK "지점명, 로그인 드롭다운 식별자 (공통기능_PRD.md NFR-02)"
        varchar password "지점 공용 계정 비밀번호, 평문 저장 금지·해시로 저장 (공통기능_PRD.md NFR-01)"
        varchar phone "부트스트랩 직후엔 nullable, 지점이 설정 화면에서 직접 입력 (COM-05)"
        varchar address "위와 동일, 입력 시 자동 지오코딩"
        decimal latitude "address 지오코딩 결과, 거리순 정렬용 (지점재고공유_PRD.md INV-07)"
        decimal longitude "address 지오코딩 결과, 거리순 정렬용"
    }

    ENGINEER {
        bigint id PK
        bigint org_id FK "소속 지점 (공통기능_PRD.md COM-06)"
        varchar name
        varchar phone
        boolean is_active "비활성화된 엔지니어는 새 RO 배정 선택지에서 제외, 과거 RO는 유지 (COM-08)"
    }

    REPAIR_ORDER {
        bigint id PK
        bigint org_id FK "이 RO를 접수한 지점, 로그인 세션 기준으로 자동 지정"
        varchar ro_number UK "기존 전산(GSW) RO번호 그대로 사용 (지점 내에서 유일, 전역 유일 아님 — §11-16 확인 필요)"
        varchar vehicle_number
        varchar customer_name
        varchar customer_phone
        bigint engineer_id FK
        varchar incoming_status "ORDERED/RECEIVED는 Part.status 집계값, AWAITING_RETURN/RETURNED는 RO 단위 액션으로 독립 설정 (§11-11)"
        date received_date "부품 입고일 (D+0), RO 전체 부품 도착 시점 = MAX(Part.received_date)"
        date appointment_date "1차 작업 예정일, 최초 예약"
        date rebooking_date "2차 작업 예정일(재예약일), 최대 1회, nullable"
        boolean is_checked_in "차량입고 여부, 유효 작업 예정일(rebooking_date 있으면 그것, 없으면 appointment_date) + 2일 경과 후 판단 (FR-08)"
        date notified_date "안내 최초 발송일, 수동 발송 (FR-06)"
        date renotified_date "안내 자동 재전송일, notified_date 기준 영업일 3일 후 자동 (FR-06-2)"
    }

    PART {
        bigint id PK
        bigint repair_order_id FK
        varchar part_number
        varchar part_name
        varchar status "ORDERED / RECEIVED"
        date received_date "이 부품이 실제 도착한 날, RO 상세 화면에 부품별로 표시 (§11-10)"
    }

    STOCK {
        bigint id PK
        bigint org_id FK "재고를 보유한 지점 (지점재고공유_PRD.md INV-01)"
        varchar part_number
        varchar part_name
        int quantity "0이 되어도 삭제하지 않고 유지, [전체 검색] 결과에서만 제외 (지점재고공유_PRD.md §11 확정)"
    }

    STOCK_CLAIM_LOG {
        bigint id PK
        bigint part_id FK "재고를 조달받은 Part (재고공유_API명세.md claim-stock)"
        bigint source_org_id FK "재고를 내준 지점. Stock 행이 나중에 삭제돼도 이력이 남도록 stock_id가 아닌 org_id를 직접 저장"
        date claimed_at "조달 확정 시각"
    }
```

## 상태 전이 요약

### Part.status (2단계, 초기 단계 전용)

`ORDERED → RECEIVED`

- `ORDERED`: RO 접수(FR-01) 시 부품 목록과 함께 생성되는 기본값. 접수 즉시 모비스에 주문 처리된 것으로 간주하며, "미주문" 같은 별도 대기 상태는 두지 않는다 (§11-14 확정)
- `RECEIVED`: 개별 부품이 실제 도착했을 때 그 Part만 전환 (부품마다 도착 시점이 다름, FR-03)
- API 레벨에서 전이 순서를 강제 검증하지 않는다 (1인 운영 내부 도구, 부품관리시스템_API명세.md 참고)
- v0.2까지는 `AWAITING_ORDER`(미주문) → `ORDERED` → `RECEIVED` → `AWAITING_RETURN` → `RETURNED` 5단계였으나, §11-11(반품 처리는 무조건 RO 단위) 확정으로 부품 단위 반품 상태가 삭제되고, §11-14(접수 즉시 주문중 시작) 확정으로 `AWAITING_ORDER`도 삭제되어 지금의 2단계만 남음

### RepairOrder.incoming_status (4단계, 단계별로 출처가 다름)

`ORDERED → RECEIVED → AWAITING_RETURN → RETURNED`

- **`ORDERED` / `RECEIVED`**: 그 RO에 걸린 Part들의 status를 집계한 파생값 (RO에 걸린 Part들의 status 중 가장 진행이 덜 된 값을 취함, 예: 부품 2개 중 하나만 `RECEIVED`고 하나는 아직 `ORDERED`면 → RO는 `ORDERED`). §11-1 참고
- **`AWAITING_RETURN`**: Part 집계가 아니라 **RO 단위로 직접 설정**되는 값. 트리거: ①안내 재전송 후 영업일 3일 무응답 ②재예약 연락두절 ③2차 작업 예정일 노쇼 재발 (FR-07, FR-09) — 셋 다 "고객 응대 자체가 종료됨"을 뜻하며, RO 하나 전체에 대해 한 번에 전환된다 (부품별 개별 전환 없음, §11-11)
- **`RETURNED`**: 관리자/접수담당이 FR-14로 그 RO의 '반품 처리 완료' 체크를 남길 때 RO 단위로 직접 설정 (실제 반품 신청은 GSW 등 외부 시스템에서 처리, §11-2 — 이 시스템은 완료 체크만 받음)
- v0.2에서는 `AWAITING_RETURN`/`RETURNED`도 Part.status 집계 파생값이었고, 반품기한(30일) 임박 시 부품 개별로 자동 전환되는 4번째 트리거(FR-11-1)도 있었으나, §11-11(반품 RO 단위 확정)로 삭제되고 지금 구조로 단순화됨. 반품기한 자동 강조 기능 자체의 폐기 사유는 §11-11 참고

### 기타 필드

- `is_checked_in`: 유효 작업 예정일(`rebooking_date`가 있으면 그것, 없으면 `appointment_date`) + 2일 경과 시점에 배치가 확인. `false`면 노쇼 → 엔지니어 알림 (FR-08)
- `rebooking_date`가 이미 있는 상태(재예약 1회 소진)에서 다시 노쇼/연락두절 발생 시 더 이상 재예약 유도 없이 RO를 `AWAITING_RETURN` 전환 (FR-09, FR-10)
- `renotified_date`: `notified_date`이 있는 상태에서 영업일 3일 경과 & 예약 없음(`appointment_date` 미등록) 조건이면 배치가 이 값을 채우며 안내를 재전송 (FR-06-2). 이후 다시 영업일 3일이 지나도 예약이 없으면 `AWAITING_RETURN` 전환 (FR-07, §11-12 최종 대기기간 확인 필요)

### Organization (지점) — 여러 도메인이 공유하는 마스터 테이블

- `Organization`는 로그인(공통기능_PRD.md), RO/엔지니어 소속(부품관리시스템_PRD.md §11-15), 재고 보유(지점재고공유_PRD.md)에서 모두 참조하는 공용 엔티티다. 어느 한 도메인 소속이 아니라 별도 문서(공통기능_PRD.md)에서 정의를 주도한다
- `name`, `password`는 부트스트랩 시점(운영자가 백엔드에서 직접 등록, 공통기능_PRD.md COM-01)에 채워지고, `phone`/`address`/`latitude`/`longitude`는 그 이후 지점이 로그인해서 설정 화면에서 직접 채운다 (COM-05). 따라서 부트스트랩 직후엔 `phone`/`address`가 비어있을 수 있음
- `latitude`/`longitude`는 `address` 저장 시 지오코딩 API로 자동 계산되는 파생 데이터다. 주소 자체(사람이 읽는 용도)와 좌표(거리 계산용 숫자)를 별도 컬럼으로 둔다 — 문자열끼리는 거리 계산이 안 되기 때문

### Stock (지점재고공유)

- `Stock.org_id`는 이 재고를 등록한(보유한) 지점이다. `[내 지점 재고]` 탭은 `org_id = 로그인한 지점`으로 필터링, `[전체 검색]` 탭은 전체 지점 대상으로 검색한다 (지점재고공유_PRD.md §6)
- `quantity = 0`이 되어도 로우를 삭제하지 않는다. 검색 결과 노출 여부만 애플리케이션 레벨에서 `quantity > 0` 조건으로 필터링한다

### StockClaimLog (재고 조달 이력)

- `claim-stock` API(재고공유_API명세.md) 호출 시 `Stock.quantity`를 차감하는 것과 별개로, "어느 RO의 어느 부품이 어느 지점에서 조달됐는지"를 이 테이블에 한 줄 남긴다
- `stock_id`가 아니라 `source_org_id`를 직접 저장한다. `Stock`은 지점이 언제든 삭제할 수 있는 행이라(INV-06), `stock_id` FK로만 연결해두면 나중에 그 Stock이 삭제됐을 때 "누구한테서 받았는지"까지 같이 사라져버림 — 조달 이력은 Stock의 생애주기와 무관하게 영구 보존되어야 하므로 지점 정보를 직접 복사해서 저장
- RO 상세 화면에서 "이 부품, OO지점에서 받아옴" 같은 표시를 하고 싶을 때 이 로그를 조회하면 됨 (화면 노출 여부는 지점재고공유_PRD.md에 아직 반영 안 됨, 필요 시 추가)

### Engineer.org_id / RepairOrder.org_id

- 여러 지점이 하나의 시스템을 공용으로 쓰게 되면서(부품관리시스템_PRD.md §11-15) 추가된 필드. 로그인 세션의 지점을 기준으로 자동 지정되며, 사용자가 직접 입력하지 않는다
- `Engineer.is_active = false`인 엔지니어는 새 RO 접수 화면의 "담당 엔지니어" 선택지에서 제외되지만, 이미 그 엔지니어가 배정된 과거 `RepairOrder.engineer_id`는 그대로 유지된다 (완전 삭제 아님, 공통기능_PRD.md COM-08)

## RO 레벨 vs Part 레벨, 왜 각각 필요한가

- **`RepairOrder.received_date`** (= 모든 Part 중 가장 늦게 도착한 날짜): **고객 안내** 기준. §11-1에 따라 일부 부품만 와서는 고객에게 오라고 할 수 없어서, 전체 도착일을 기준으로 안내 발송 여부를 판단
- **`Part.received_date`** (부품별 실제 도착일): v0.2에서는 부품별 반품기한 자동 계산·강조에 사용됐으나, 그 기능 자체가 삭제됨(§11-11). v0.3에서는 순수하게 **RO 상세 화면에 부품별 입고 이력을 보여주기 위한 표시용 데이터**로만 남음 (§11-10 확정)

## 기존 코드(v0.1/v0.2) 대비 변경 필요 필드

| 기존/이전 필드 | 처리 |
|---|---|
| `appointmentDate` (v0.2, 단일 필드+덮어쓰기) | `appointment_date`(1차) / `rebooking_date`(2차) 두 컬럼으로 분리 |
| `isRebooked` (v0.2, boolean) | **삭제** — `rebooking_date` 값 존재 여부로 파생 가능 |
| `notification1SentAt` | `notified_date`으로 명칭 정리 |
| `notification2SentAt` / `finalNotificationSentAt` | 삭제 (v0.2에서 이미 삭제됨) |
| — | `renotified_date` 신규 추가 (FR-06-2 안내 자동 재전송 추적용) |
| `Part.status`의 `AWAITING_RETURN`/`RETURNED` (v0.2) | **삭제** — 반품은 RO 단위로만 처리 (§11-11) |
| `Part.status`/`incoming_status`의 `AWAITING_ORDER` (v0.2) | **삭제** — 접수 즉시 `ORDERED`로 시작, "미주문" 대기 상태 자체가 불필요함 (§11-14) |
| `RepairOrder.incoming_status`가 항상 Part 집계 파생값이던 것 (v0.2) | `AWAITING_RETURN`/`RETURNED`는 더 이상 Part 집계가 아니라 **RO 단위 직접 설정값**으로 변경 |
| `returnProcessed` (v0.1) | FR-14로 대체, RO 단위로 명확화 |
| — (v0.4 신규) | `Organization`, `Stock` 엔티티 신규 추가 (공통기능_PRD.md, 지점재고공유_PRD.md) |
| — (v0.4 신규) | `RepairOrder.org_id`, `Engineer.org_id` 추가 — 여러 지점 공용 시스템으로 전환 (§11-15) |
| — (v0.4 신규) | `Engineer.is_active` 추가 — 완전 삭제 대신 비활성화 (공통기능_PRD.md COM-08) |
| — (v0.4 신규) | `StockClaimLog` 엔티티 신규 추가 — 재고 조달 이력 추적, `stock_id` 대신 `source_org_id` 직접 저장 (재고공유_API명세.md claim-stock) |
| `RepairOrder.ro_number`의 UK 범위 | 현재 전역 UK로 되어있으나, 지점끼리 RO번호가 겹칠 수 있는지 확인 필요 (§11-16, 클라이언트 확인 후 `(org_id, ro_number)` 복합 UK로 변경될 수 있음) |
