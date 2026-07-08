import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Alert, Card, Layout, Space, Spin, Typography } from 'antd'
import { WarningOutlined } from '@ant-design/icons'
import type { RepairOrder } from '@/types'
import { repairOrderApi } from '@/api/repairOrders'
import dayjs from 'dayjs'

const { Header, Content } = Layout
const { Title, Text } = Typography

export default function DashboardPage() {
  const [approaching, setApproaching] = useState<RepairOrder[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    repairOrderApi.returnDeadlineApproaching()
      .then(res => setApproaching(res.data))
      .finally(() => setLoading(false))
  }, [])

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ background: '#003087', padding: '0 24px' }}>
        <Title level={3} style={{ color: '#fff', margin: '14px 0', lineHeight: '36px' }}>
          부품 입고·예약 관리 시스템
        </Title>
      </Header>
      <Content style={{ padding: 24 }}>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          {loading ? (
            <Spin size="large" />
          ) : approaching.length > 0 ? (
            <Alert
              type="error"
              icon={<WarningOutlined />}
              showIcon
              message={`반품 기한 임박 ${approaching.length}건`}
              description={
                <ul style={{ marginTop: 8, paddingLeft: 20 }}>
                  {approaching.map(ro => (
                    <li key={ro.id} style={{ fontSize: 16, marginBottom: 4 }}>
                      <strong>{ro.roNumber}</strong> · {ro.vehicleNumber} · {ro.customerName} ·{' '}
                      반품기한{' '}
                      <Text type="danger">
                        {ro.returnDeadline ? dayjs(ro.returnDeadline).format('MM/DD') : '-'}
                      </Text>
                    </li>
                  ))}
                </ul>
              }
            />
          ) : (
            <Alert type="success" message="반품 기한 임박 건 없음" showIcon />
          )}

          <Card title="전체 현황">
            <Link to="/repair-orders" style={{ fontSize: 16 }}>부품 목록 보기 →</Link>
          </Card>
        </Space>
      </Content>
    </Layout>
  )
}
