import { useEffect, useRef } from 'react'
import { useParams, useSearchParams, useNavigate } from 'react-router-dom'
import { toast } from 'react-hot-toast'
import { useAuth } from '@/hooks/useAuth'
import Spinner from '@/components/Elements/Spinner'

const OAuthCallbackPage = () => {
  const { provider } = useParams()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { handleOAuthLogin } = useAuth()
  
  const code = searchParams.get('code')
  const error = searchParams.get('error')

  const processed = useRef(false)

  useEffect(() => {
    if (processed.current) return
    processed.current = true

    if (error) {
      toast.error('Đăng nhập thất bại: ' + error)
      navigate('/login', { replace: true })
      return
    }

    if (!code) {
      toast.error('Không tìm thấy mã xác thực từ ' + provider)
      navigate('/login', { replace: true })
      return
    }

    const redirectUri = `${window.location.origin}/oauth2/callback/${provider}`

    handleOAuthLogin({ code, provider, redirect_uri: redirectUri })
      .then(() => {
        toast.success(`Đăng nhập thành công qua ${provider}!`)
      })
      .catch((err) => {
        console.error('OAuth login error:', err)
        toast.error(err?.response?.data?.message || `Lỗi đăng nhập ${provider}`)
        navigate('/login', { replace: true })
      })

  }, [code, error, provider, handleOAuthLogin, navigate])

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-surface-dark">
      <Spinner size="xl" />
      <p className="mt-4 text-content-main font-semibold">
        Đang xử lý đăng nhập qua {provider}...
      </p>
    </div>
  )
}

export default OAuthCallbackPage
