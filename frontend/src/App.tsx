import { BrowserRouter, Route, Routes, Navigate } from 'react-router-dom'
import RepairOrderListPage from '@/pages/RepairOrderListPage'
import DashboardPage from '@/pages/DashboardPage'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/repair-orders" element={<RepairOrderListPage />} />
      </Routes>
    </BrowserRouter>
  )
}
