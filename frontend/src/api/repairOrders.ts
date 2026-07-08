import type { RepairOrder } from '@/types'
import client from './client'

export const repairOrderApi = {
  list: (engineerId?: number) =>
    client.get<RepairOrder[]>('/repair-orders', { params: engineerId ? { engineerId } : undefined }),

  get: (id: number) =>
    client.get<RepairOrder>(`/repair-orders/${id}`),

  create: (data: {
    roNumber: string
    vehicleNumber: string
    customerName: string
    customerPhone: string
    engineerId: number
    parts: { partNumber: string; partName: string }[]
  }) => client.post<number>('/repair-orders', data),

  markReceived: (id: number) =>
    client.patch<RepairOrder>(`/repair-orders/${id}/received`),

  registerAppointment: (id: number, appointmentDate: string) =>
    client.patch<RepairOrder>(`/repair-orders/${id}/appointment`, { appointmentDate }),

  markReturnProcessed: (id: number) =>
    client.patch<RepairOrder>(`/repair-orders/${id}/return-processed`),

  returnDeadlineApproaching: () =>
    client.get<RepairOrder[]>('/repair-orders/return-deadline-approaching'),
}
