import { useState, useEffect, useCallback, useRef } from 'react'
import { Outlet } from 'react-router-dom'
import { RiMapPinLine, RiRefreshLine, RiLockLine, RiShieldCheckLine } from 'react-icons/ri'

/**
 * Wrapper component bắt buộc tài xế phải bật định vị (Geolocation)
 * trước khi sử dụng app. Tương thích tốt với cả Chrome, Safari, Firefox.
 *
 * Vấn đề trên Chrome:
 * - Chrome yêu cầu HTTPS (trừ localhost) để Geolocation hoạt động
 * - Permissions API có thể trả kết quả khác Safari
 * - Khi user deny, Chrome không hiện popup lại, phải vào Settings
 *
 * Giải pháp:
 * - Luôn dùng getCurrentPosition làm nguồn sự thật duy nhất
 * - Permissions API chỉ dùng để lắng nghe thay đổi realtime (onchange)
 * - Phân biệt rõ lỗi HTTPS vs denied vs timeout
 * - Tự động retry khi tab được focus lại
 */
const RequireGeolocation = () => {
  // 'checking' | 'granted' | 'denied' | 'insecure' | 'unavailable' | 'unsupported'
  const [status, setStatus] = useState('checking')
  const permissionRef = useRef(null)
  const isMounted = useRef(true)

  // Phát hiện trình duyệt
  const isChrome = /Chrome/.test(navigator.userAgent) && !/Edg/.test(navigator.userAgent)
  const isSecureContext = window.isSecureContext

  const checkGeolocation = useCallback(() => {
    if (!navigator.geolocation) {
      setStatus('unsupported')
      return
    }

    // Chrome chặn Geolocation trên HTTP (trừ localhost)
    if (!isSecureContext) {
      setStatus('insecure')
      return
    }

    setStatus('checking')

    // Luôn dùng getCurrentPosition — cách chắc chắn nhất trên mọi trình duyệt
    navigator.geolocation.getCurrentPosition(
      () => {
        if (isMounted.current) setStatus('granted')
      },
      (err) => {
        if (!isMounted.current) return
        if (err.code === 1) {
          // PERMISSION_DENIED
          setStatus('denied')
        } else if (err.code === 2) {
          // POSITION_UNAVAILABLE — GPS tắt ở cấp hệ điều hành
          setStatus('unavailable')
        } else {
          // TIMEOUT — thử lại với accuracy thấp hơn
          navigator.geolocation.getCurrentPosition(
            () => {
              if (isMounted.current) setStatus('granted')
            },
            (retryErr) => {
              if (!isMounted.current) return
              setStatus(retryErr.code === 1 ? 'denied' : 'unavailable')
            },
            { timeout: 15000, enableHighAccuracy: false, maximumAge: 60000 }
          )
        }
      },
      { timeout: 8000, enableHighAccuracy: true, maximumAge: 0 }
    )
  }, [isSecureContext])

  // Khởi tạo + lắng nghe Permissions API cho realtime updates
  useEffect(() => {
    isMounted.current = true
    checkGeolocation()

    // Lắng nghe Permissions API onchange (hỗ trợ Chrome, Firefox)
    if (navigator.permissions?.query) {
      navigator.permissions.query({ name: 'geolocation' })
        .then((result) => {
          permissionRef.current = result
          result.onchange = () => {
            if (!isMounted.current) return
            if (result.state === 'granted') {
              setStatus('granted')
            } else if (result.state === 'denied') {
              setStatus('denied')
            }
            // Nếu chuyển sang 'prompt', thử lại getCurrentPosition
            if (result.state === 'prompt') {
              checkGeolocation()
            }
          }
        })
        .catch(() => { }) // Không sao nếu Permissions API không hoạt động
    }

    return () => {
      isMounted.current = false
      if (permissionRef.current) {
        permissionRef.current.onchange = null
      }
    }
  }, [checkGeolocation])

  // Tự động kiểm tra lại khi user quay lại tab (sau khi vào Settings bật quyền)
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible' && status !== 'granted') {
        checkGeolocation()
      }
    }
    document.addEventListener('visibilitychange', handleVisibilityChange)
    return () => document.removeEventListener('visibilitychange', handleVisibilityChange)
  }, [status, checkGeolocation])

  if (status === 'granted') {
    return <Outlet />
  }

  // Nội dung tuỳ theo trạng thái
  const content = {
    unsupported: {
      title: 'Trình duyệt không hỗ trợ',
      desc: 'Trình duyệt của bạn không hỗ trợ dịch vụ định vị. Vui lòng sử dụng Chrome, Safari hoặc Firefox để tiếp tục.',
      icon: <RiMapPinLine size={40} className="text-red-500" />,
    },
    insecure: {
      title: 'Yêu cầu kết nối bảo mật',
      desc: (
        <>
          Trình duyệt yêu cầu kết nối <strong>HTTPS</strong> để sử dụng định vị.
          Vui lòng truy cập trang web qua <strong>https://</strong> thay vì http://.
        </>
      ),
      icon: <RiLockLine size={40} className="text-amber-500" />,
    },
    denied: {
      title: 'Cần bật quyền định vị',
      desc: 'Ứng dụng tài xế yêu cầu quyền truy cập vị trí để hoạt động. Vui lòng cho phép quyền định vị rồi nhấn "Thử lại".',
      icon: <RiMapPinLine size={40} className="text-red-500" />,
    },
    unavailable: {
      title: 'Không thể lấy vị trí',
      desc: 'Dịch vụ định vị không khả dụng. Vui lòng kiểm tra GPS trên thiết bị đã được bật, sau đó nhấn "Thử lại".',
      icon: <RiMapPinLine size={40} className="text-amber-500" />,
    },
    checking: {
      title: 'Đang kiểm tra định vị',
      desc: 'Đang kiểm tra quyền truy cập vị trí, vui lòng đợi...',
      icon: <RiShieldCheckLine size={40} className="text-brand-500" />,
    },
  }

  const c = content[status] || content.checking

  return (
    <div className="h-full flex items-center justify-center bg-[#e8ece3] dark:bg-surface-dark p-6">
      <div className="bg-white/95 dark:bg-surface-card/95 backdrop-blur-md rounded-3xl shadow-2xl border border-gray-200 dark:border-surface-border p-8 md:p-10 max-w-md w-full text-center">

        {/* Icon */}
        <div className="mx-auto w-20 h-20 rounded-full bg-red-50 dark:bg-red-500/10 flex items-center justify-center mb-6">
          {c.icon}
        </div>

        {/* Title */}
        <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-3">
          {c.title}
        </h2>

        {/* Description */}
        <p className="text-gray-500 dark:text-gray-400 text-sm leading-relaxed mb-6">
          {c.desc}
        </p>

        {/* Loading spinner khi checking */}
        {status === 'checking' && (
          <div className="flex justify-center mb-6">
            <div className="w-8 h-8 border-4 border-brand-500/20 border-t-brand-500 rounded-full animate-spin" />
          </div>
        )}

        {/* Hướng dẫn chi tiết cho từng trường hợp */}
        {status === 'denied' && (
          <div className="bg-amber-50 dark:bg-amber-500/10 border border-amber-200 dark:border-amber-500/20 rounded-xl p-4 mb-6 text-left">
            <p className="text-amber-800 dark:text-amber-300 text-xs font-semibold mb-2">💡 Hướng dẫn bật định vị:</p>

            {isChrome ? (
              <ul className="text-amber-700 dark:text-amber-400 text-xs space-y-1.5 list-disc pl-4">
                <li>Nhấn vào biểu tượng <strong>🔒</strong> (hoặc <strong>ⓘ</strong>) bên trái thanh địa chỉ</li>
                <li>Nhấn <strong>"Cài đặt trang web"</strong> (Site settings)</li>
                <li>Tìm mục <strong>"Vị trí"</strong> → chọn <strong>"Cho phép"</strong></li>
                <li>Quay lại trang này, nhấn <strong>"Thử lại"</strong></li>
              </ul>
            ) : (
              <ul className="text-amber-700 dark:text-amber-400 text-xs space-y-1.5 list-disc pl-4">
                <li>Nhấn vào biểu tượng <strong>🔒 khoá</strong> trên thanh địa chỉ</li>
                <li>Tìm mục <strong>"Vị trí"</strong> hoặc <strong>"Location"</strong></li>
                <li>Chuyển sang <strong>"Cho phép"</strong> (Allow)</li>
                <li>Nhấn nút <strong>"Thử lại"</strong> bên dưới</li>
              </ul>
            )}

            {/* Hướng dẫn thêm cho mobile */}
            <div className="mt-3 pt-3 border-t border-amber-200 dark:border-amber-500/20">
              <p className="text-amber-700 dark:text-amber-400 text-xs">
                📱 <strong>Trên điện thoại:</strong> Vào <em>Cài đặt → Quyền riêng tư → Dịch vụ định vị</em> và bật cho trình duyệt.
              </p>
            </div>
          </div>
        )}

        {status === 'unavailable' && (
          <div className="bg-blue-50 dark:bg-blue-500/10 border border-blue-200 dark:border-blue-500/20 rounded-xl p-4 mb-6 text-left">
            <p className="text-blue-800 dark:text-blue-300 text-xs font-semibold mb-2">📍 Kiểm tra GPS:</p>
            <ul className="text-blue-700 dark:text-blue-400 text-xs space-y-1.5 list-disc pl-4">
              <li><strong>iPhone:</strong> Cài đặt → Quyền riêng tư → Dịch vụ định vị → Bật</li>
              <li><strong>Android:</strong> Cài đặt → Vị trí → Bật</li>
              <li>Đảm bảo bạn đang ở nơi có tín hiệu GPS tốt</li>
            </ul>
          </div>
        )}

        {status === 'insecure' && (
          <div className="bg-amber-50 dark:bg-amber-500/10 border border-amber-200 dark:border-amber-500/20 rounded-xl p-4 mb-6 text-left">
            <p className="text-amber-800 dark:text-amber-300 text-xs font-semibold mb-2">🔒 Lưu ý bảo mật:</p>
            <p className="text-amber-700 dark:text-amber-400 text-xs">
              Chrome và các trình duyệt hiện đại chỉ cho phép truy cập vị trí trên kết nối HTTPS an toàn.
              Hãy liên hệ quản trị viên để cấu hình HTTPS cho trang web.
            </p>
          </div>
        )}

        {/* Retry button */}
        {status !== 'checking' && (
          <button
            onClick={checkGeolocation}
            className="w-full py-3.5 rounded-xl font-bold bg-brand-500 text-white shadow-[0_0_20px_rgba(34,197,94,0.3)] hover:bg-brand-600 transition-all flex items-center justify-center gap-2 active:scale-95"
          >
            <RiRefreshLine size={20} />
            Thử lại
          </button>
        )}
      </div>
    </div>
  )
}

export default RequireGeolocation
