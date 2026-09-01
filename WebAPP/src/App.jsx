import { BrowserRouter } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { useEffect } from 'react'
import { useUIStore } from '@/store/rootStore'
import AppRoutes from '@/routes/AppRoutes'
import { LoadScript } from '@react-google-maps/api'
import { MAPS_KEY } from '@/config'

const libraries = ['places']

const App = () => {
  const { theme } = useUIStore()

  useEffect(() => {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }, [theme])

  return (
    <LoadScript googleMapsApiKey={MAPS_KEY} libraries={libraries} region="VN" language="vi">
      <BrowserRouter>
        <AppRoutes />
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 3500,
            style: {
              background: '#111827',
              color: '#f9fafb',
              border: '1px solid #1F2937',
              borderRadius: '12px',
              fontSize: '14px',
              fontFamily: 'Outfit, sans-serif',
              padding: '12px 16px',
            },
            success: {
              iconTheme: { primary: '#22c55e', secondary: '#fff' },
            },
            error: {
              iconTheme: { primary: '#ef4444', secondary: '#fff' },
            },
          }}
        />
      </BrowserRouter>
    </LoadScript>
  )
}

export default App
