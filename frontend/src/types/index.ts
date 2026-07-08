export type IncomingStatus = 'ORDER_PLACED' | 'RECEIVED' | 'RETURN_PENDING' | 'RETURNED'

export const INCOMING_STATUS_LABEL: Record<IncomingStatus, string> = {
  ORDER_PLACED: '주문중',
  RECEIVED: '입고완료',
  RETURN_PENDING: '반품예정',
  RETURNED: '반품완료',
}

export interface Engineer {
  id: number
  name: string
  phone: string
}

export interface Part {
  id: number
  partNumber: string
  partName: string
  received: boolean
}

export interface RepairOrder {
  id: number
  roNumber: string
  vehicleNumber: string
  customerName: string
  customerPhone: string
  engineer: Engineer
  incomingStatus: IncomingStatus
  receivedDate: string | null        // ISO date (yyyy-MM-dd)
  appointmentDate: string | null
  notification1SentAt: string | null
  notification2SentAt: string | null
  finalNotificationSentAt: string | null
  returnProcessed: boolean
  parts: Part[]
  returnDeadline: string | null      // 서버 계산값 (receivedDate + 30일)
}
