import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Layout, Space, Table, Tag, Typography } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import type { RepairOrder } from '@/types'
import { INCOMING_STATUS_LABEL } from '@/types'
import { repairOrderApi } from '@/api/repairOrders'
import dayjs from 'dayjs'

const { Header, Content } = Layout
const { Title } = Typography

const STATUS_COLOR: Record<string, string> = {
  ORDER_PLACED: 'default',
  RECEIVED: 'blue',
  RETURN_PENDING: 'orange',
  RETURNED: 'red',
}

const columns: ColumnsType<RepairOrder> = [
  { title: 'RO번호', dataIndex: 'roNumber', key: 'roNumber', width: 140 },
  { title: '차량번호', dataIndex: 'vehicleNumber', key: 'vehicleNumber', width: 120 },
  { title: '고객명', dataIndex: 'customerName', key: 'customerName', width: 100 },
  {
    title: '담당 엔지니어',
    dataIndex: ['engineer', 'name'],
    key: 'engineer',
    width: 120,
  },
  {
    title: '입고 현황',
    dataIndex: 'incomingStatus',
    key: 'incomingStatus',
    width: 110,
    render: (status: string) => (
      <Tag color={STATUS_COLOR[status]}>{INCOMING_STATUS_LABEL[status as keyof typeof INCOMING_STATUS_LABEL]}</Tag>
    ),
  },
  {
    title: '입고일',
    dataIndex: 'receivedDate',
    key: 'receivedDate',
    width: 110,
    render: (d: string | null) => (d ? dayjs(d).format('YYYY-MM-DD') : '-'),
  },
  {
    title: '반품기한',
    dataIndex: 'returnDeadline',
    key: 'returnDeadline',
    width: 110,
    render: (d: string | null) => (d ? dayjs(d).format('YYYY-MM-DD') : '-'),
  },
  {
    title: '1차 통보',
    dataIndex: 'notification1SentAt',
    key: 'notification1SentAt',
    width: 100,
    render: (d: string | null) => (d ? <Tag color="green">발송완료</Tag> : <Tag>미발송</Tag>),
  },
  {
    title: '2차 통보',
    dataIndex: 'notification2SentAt',
    key: 'notification2SentAt',
    width: 100,
    render: (d: string | null) => (d ? <Tag color="green">발송완료</Tag> : <Tag>미발송</Tag>),
  },
  {
    title: '예약 여부',
    dataIndex: 'appointmentDate',
    key: 'appointmentDate',
    width: 120,
    render: (d: string | null) =>
      d ? (
        <Tag color="blue">예약완료 ({dayjs(d).format('MM/DD')})</Tag>
      ) : (
        <Tag>미예약</Tag>
      ),
  },
  {
    title: '점검일',
    dataIndex: 'appointmentDate',
    key: 'appointmentDateDisplay',
    width: 110,
    render: (d: string | null) => (d ? dayjs(d).format('YYYY-MM-DD') : '-'),
  },
]

export default function RepairOrderListPage() {
  const [data, setData] = useState<RepairOrder[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    repairOrderApi.list()
      .then(res => setData(res.data))
      .finally(() => setLoading(false))
  }, [])

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ background: '#003087', padding: '0 24px' }}>
        <Title level={3} style={{ color: '#fff', margin: '14px 0', lineHeight: '36px' }}>
          부품 목록
        </Title>
      </Header>
      <Content style={{ padding: 24 }}>
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
          <Link to="/dashboard">← 대시보드</Link>
          <Table
            columns={columns}
            dataSource={data}
            rowKey="id"
            loading={loading}
            scroll={{ x: 'max-content' }}
            pagination={{ pageSize: 20 }}
            size="middle"
          />
        </Space>
      </Content>
    </Layout>
  )
}
