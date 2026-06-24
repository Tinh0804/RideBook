import { useState, useEffect } from 'react'
import { adminApi } from '@/features/admin/api/adminApi'
import Spinner from '@/components/Elements/Spinner'
import toast from 'react-hot-toast'

const AdminPricingPage = () => {
  const [vehicleTypes, setVehicleTypes] = useState([])
  const [timeSlots, setTimeSlots] = useState([])
  const [pricing, setPricing] = useState([])
  const [loading, setLoading] = useState(true)

  const fetchData = async () => {
    setLoading(true)
    try {
      const [vRes, tRes, pRes] = await Promise.all([
        adminApi.getAllVehicleTypes(),
        adminApi.getAllTimeSlots(),
        adminApi.getAllPricing(),
      ])
      setVehicleTypes(vRes.result || [])
      setTimeSlots(tRes.result || [])
      setPricing(pRes.result || [])
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchData() }, [])

  const handleUpdatePricing = async (vehicleTypeId, timeId, val, priceItem) => {
    try {
      if (!val || val <= 0) {
        // Xóa nếu có
        if (priceItem) {
          await adminApi.deletePricing(vehicleTypeId, timeId)
          toast.success('Đã xóa hệ số giá')
        }
      } else {
        // Update hoặc Create
        if (priceItem) {
          if (val !== priceItem.surcharge) {
            await adminApi.updatePricing(vehicleTypeId, timeId, { ...priceItem, surcharge: val })
            toast.success('Đã cập nhật hệ số giá')
          }
        } else {
          await adminApi.createPricing({
            id: { vehicleTypeId, timeId },
            surcharge: val
          })
          toast.success('Đã thêm hệ số giá')
        }
      }
      // Lấy lại data sau thay đổi
      const pRes = await adminApi.getAllPricing()
      setPricing(pRes.result || [])
    } catch (e) {
      console.error(e)
      toast.error('Có lỗi xảy ra khi cập nhật')
    }
  }

  if (loading) return <div className="flex justify-center py-16"><Spinner size="xl" /></div>

  return (
    <div className="space-y-6">
      <div>
        <h1 className="section-title">Quản lý Bảng giá</h1>
        <p className="text-content-muted text-sm mt-1">Thiết lập hệ số giá phụ thu cho từng loại xe theo khung giờ</p>
      </div>

      <div className="card overflow-hidden">
        <div className="px-5 py-4 border-b border-surface-border">
          <h2 className="font-display text-base font-bold text-content-main">Ma trận hệ số giá</h2>
          <p className="text-content-muted text-xs mt-1">
            Giá thực = Giá cơ bản (Loại xe) × Hệ số khung giờ. 
            <br />
            - Click vào ô để thay đổi giá trị. Hệ thống tự động lưu khi bạn click ra ngoài (Blur).
            <br />
            - Xóa trống ô (hoặc đặt giá trị &le; 0) để xóa hệ số giá tại khung giờ đó.
          </p>
        </div>

        <div className="overflow-x-auto p-5">
          <table className="w-full text-sm border-collapse min-w-[600px]">
            <thead>
              <tr>
                <th className="border border-surface-border p-3 text-left bg-surface-dark font-semibold text-content-muted text-xs uppercase w-48">
                  Loại xe \ Khung giờ
                </th>
                {timeSlots.map((t) => (
                  <th key={t.timeId} className="border border-surface-border p-3 text-center bg-surface-dark min-w-[120px]">
                    <span className="text-xs font-semibold text-content-muted uppercase block mb-1">{t.slotName}</span>
                    <span className="text-[10px] text-brand-400 bg-brand-500/10 px-2 py-0.5 rounded-lg font-mono">
                      {t.startTime} - {t.endTime}
                    </span>
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {vehicleTypes.length === 0 ? (
                <tr>
                  <td colSpan={timeSlots.length + 1} className="text-center text-content-muted py-8">
                    Chưa có dữ liệu loại xe
                  </td>
                </tr>
              ) : (
                vehicleTypes.map((v) => (
                  <tr key={v.vehicleTypeId} className="hover:bg-surface-border/10 transition-colors">
                    <td className="border border-surface-border p-3">
                      <div className="flex items-center gap-3">
                        {v.icon ? (
                          <img src={v.icon} alt={v.vehicleTypeName} className="w-10 h-10 object-cover rounded-lg border border-surface-border shrink-0" />
                        ) : (
                          <div className="w-10 h-10 rounded-lg bg-surface-dark border border-surface-border flex items-center justify-center text-xl shrink-0">🚗</div>
                        )}
                        <div>
                          <p className="font-medium text-content-main text-sm">{v.vehicleTypeName}</p>
                          <p className="text-xs text-brand-400 font-mono mt-0.5">{v.pricePerKm?.toLocaleString()} đ/km</p>
                        </div>
                      </div>
                    </td>
                    {timeSlots.map((t) => {
                      const priceItem = pricing.find((p) => p.vehicleType?.vehicleTypeId === v.vehicleTypeId && p.time?.timeId === t.timeId)
                      return (
                        <td key={t.timeId} className="border border-surface-border p-3 text-center align-middle">
                          <input
                            type="number"
                            step="0.1"
                            min="0.1"
                            defaultValue={priceItem?.surcharge || ''}
                            placeholder="—"
                            onBlur={(e) => {
                              const val = e.target.value ? parseFloat(e.target.value) : null
                              handleUpdatePricing(v.vehicleTypeId, t.timeId, val, priceItem)
                            }}
                            className="w-20 py-1.5 bg-surface-dark border border-surface-border rounded-lg text-center text-content-main font-mono text-sm focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500/30 transition-all shadow-inner placeholder:text-content-muted/30"
                          />
                        </td>
                      )
                    })}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

export default AdminPricingPage
