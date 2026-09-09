# 재고공유 API 명세 (v0.1)

Base path: `/api/stocks`. PRD(`지점재고공유_PRD.md`) INV 번호 기준으로 정리.
ERD는 `ERD.md`의 `Organization`, `Stock` 엔티티 참고.

**설계 방향**: 부품관리시스템_API명세.md와 동일한 원칙을 따른다 — 상태 전이별 액션 엔드포인트를
잘게 나누지 않고 `PATCH`로 통합하되, 다른 지점의 재고를 실제로 차감하는 것처럼 부수효과가 있는
액션은 별도 엔드포인트로 분리한다. 모든 엔드포인트는 로그인 세션(`공통기능_PRD.md` 참고)의 지점으로
스코핑된다.

**공통 응답 객체 (`Stock`)**

```json
{
  "id": 42,
  "orgId": 7,
  "branchName": "강남점",
  "branchPhone": "02-1234-5678",
  "branchAddress": "서울시 강남구 ...",
  "partNumber": "P-1001",
  "partName": "브레이크 패드",
  "quantity": 3
}
```

## 등록

| Method | Path | 설명 | FR |
|---|---|---|---|
| POST | `/api/stocks` | 장기재고 등록. `orgId`는 요청 본문에 담지 않고 로그인 세션에서 자동 지정 | INV-01 |

**Request Body**

```json
{
  "partNumber": "P-1001",
  "partName": "브레이크 패드",
  "quantity": 3
}
```

**Response Body**: 공통 `Stock` 객체

## 조회

| Method | Path | 설명 | FR |
|---|---|---|---|
| GET | `/api/stocks?tab=all&partNumber=&partName=` | `[전체 검색]` 탭. 전체 지점 대상 검색, 요청 지점의 등록 주소 기준 가까운 순 정렬. `quantity=0`인 항목은 결과에서 제외 | INV-02, INV-03, INV-08 |
| GET | `/api/stocks?tab=mine` | `[내 지점 재고]` 탭. 로그인한 지점이 등록한 재고만 조회, `quantity=0`이어도 계속 노출 | INV-06 |

**정렬 로직** (`tab=all`일 때만 적용): 요청 지점(`Organization.latitude`/`longitude`)과 결과 지점 좌표 간
직선거리를 계산해 가까운 순으로 정렬한다 (§11 확정 — 지점 등록 주소 기준, 사용자 GPS 위치 아님)

**Response Body**: `{ "items": [공통 Stock 객체, ...] }`

## 수정 / 삭제

| Method | Path | 설명 | FR |
|---|---|---|---|
| PATCH | `/api/stocks/{id}` | 수량 등 필드 수정. 본인 지점이 등록한 재고만 가능 | INV-06 |
| DELETE | `/api/stocks/{id}` | 재고 항목 완전 삭제 (수량이 0이 되는 것과는 별개 — 직원이 명시적으로 목록에서 지우고 싶을 때 사용). 본인 지점이 등록한 재고만 가능 | INV-06 |

**Request Body** (PATCH, 보낸 필드만 수정)

```json
{ "quantity": 2 }
```

**Response Body** (PATCH): 수정 반영된 최신 공통 `Stock` 객체. `quantity`가 0이 되어도 로우는
삭제되지 않고 유지되며, `[전체 검색]` 결과에서만 자동으로 빠진다 (INV-08)

## 부품관리시스템 RO 연동

| Method | Path | 설명 | FR |
|---|---|---|---|
| POST | `/api/repair-orders/{roId}/parts/{partId}/claim-stock` | RO 화면 팝업에서 검색한 재고 중 하나를 선택해 "이 지점 것을 가져다 쓴다"고 확정. 선택한 `Stock.quantity`를 1 차감하고 `StockClaimLog`에 조달 이력을 남기는 부수효과가 있어 `PATCH`로 통합하지 않고 별도 액션으로 분리 | INV-04, INV-05 |

**Request Body**

```json
{ "stockId": 42 }
```

**Response Body**

```json
{ "stockId": 42, "remainingQuantity": 2, "sourceOrgName": "강남점" }
```

- 이 호출은 재고관리시스템 쪽 API(`/api/repair-orders/...`)에 속하지만, 효과가 `Stock`에 미치므로 이
  문서에도 함께 기록한다. 실제 검색 UI는 RO 접수/수정 화면에서 팝업으로 뜬다 (§11 확정, 도로명주소
  검색과 같은 패턴)
- 이 엔드포인트는 `Stock.quantity`를 차감하는 동시에 `StockClaimLog`에 `part_id`(조달받은 부품),
  `source_org_id`(재고를 내준 지점), `claimed_at`을 기록한다 (§ ERD.md StockClaimLog 참고, 확정).
  `stock_id`가 아니라 `source_org_id`를 직접 저장하므로, 나중에 그 `Stock` 행이 삭제돼도(INV-06)
  조달 이력은 남는다

## Open Questions (구현 전 확인 필요)

- 거리 계산 방식(Haversine 등 구체적 공식, 반경 제한 여부)은 순수 구현 디테일 — 클라이언트 확인 불필요, 개발 시 결정
- `quantity` 차감 시 이미 0이거나 요청 수량보다 적을 경우의 동시성 처리(두 지점이 동시에 같은 재고를 `claim`하려는 경쟁 상황)는 API 레벨에서 별도 검증하지 않음 — 1인 운영 내부 도구 원칙상 최소한으로 처리 (동시 요청 시 마지막에 처리된 쪽이 음수 재고를 만들 수 있음, 실사용 빈도상 허용 가능한 리스크로 판단)
