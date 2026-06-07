import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'
import { TOKEN_KEY, REFRESH_TOKEN_KEY, USER_KEY } from '@/config'

// ─── Auth Store ──────────────────────────────────────────────────────────────
export const useAuthStore = create(
  persist(
    (set) => ({
      user:         null,   // Thông tin account cơ bản (id, userName, role)
      userProfile:  null,   // Thông tin profile đầy đủ từ API my-info (CustomerProfile | DriverProfile)
      accessToken:  null,
      refreshToken: null,
      isAuth:       false,
      account:      null,

      login: ({ user, accessToken, refreshToken, account }) => {
        localStorage.setItem(TOKEN_KEY,         accessToken)
        localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
        set({ user, accessToken, refreshToken, isAuth: true, account })
      },

      logout: () => {
        localStorage.clear()
        sessionStorage.clear()
        
        // Xóa tất cả cookies
        document.cookie.split(";").forEach((c) => {
          document.cookie = c
            .replace(/^ +/, "")
            .replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
        });

        set({ user: null, userProfile: null, accessToken: null, refreshToken: null, isAuth: false, account: null })
      },

      updateUser: (userData) =>
        set((state) => ({ user: { ...state.user, ...userData } })),

      setUserProfile: (profile) =>
        set({ userProfile: profile }),

      updateUserProfile: (profileData) =>
        set((state) => ({ userProfile: { ...state.userProfile, ...profileData } })),

      clearUserProfile: () =>
        set({ userProfile: null }),
    }),
    {
      name:    USER_KEY,
      // Dùng sessionStorage thay localStorage → mỗi tab trình duyệt có phiên làm việc độc lập
      // Tránh xung đột khi mở 2 tab Customer và Driver trên cùng một trình duyệt
      storage: createJSONStorage(() => sessionStorage),
      partialize: (s) => ({
        user:         s.user,
        userProfile:  s.userProfile,
        accessToken:  s.accessToken,
        refreshToken: s.refreshToken,
        isAuth:       s.isAuth,
      }),
      // Migration: normalize userProfile cũ chưa có các trường id/name chuẩn hóa
      migrate: (persistedState) => {
        const state = persistedState
        if (state?.userProfile && !state.userProfile.id) {
          const p = state.userProfile
          state.userProfile = {
            ...p,
            id:   p.customerId || p.driverId || '',
            name: p.customerName || p.driverName || '',
          }
        }
        return state
      },
    }
  )
)

// ─── Booking Store ───────────────────────────────────────────────────────────
export const useBookingStore = create((set) => ({
  currentBooking:    null,
  vehicleTypes:      [],
  activePromotion:   null,
  estimatedPrice:    null,

  setCurrentBooking:  (booking)   => set({ currentBooking: booking }),
  clearCurrentBooking:()           => set({ currentBooking: null, estimatedPrice: null }),
  setVehicleTypes:    (types)     => set({ vehicleTypes: types }),
  setActivePromotion: (promo)     => set({ activePromotion: promo }),
  setEstimatedPrice:  (price)     => set({ estimatedPrice: price }),
}))

// ─── UI Store ────────────────────────────────────────────────────────────────
export const useUIStore = create(
  persist(
    (set) => ({
      sidebarOpen:    true,
      notifCount:     0,
      theme:          'dark',

      toggleSidebar:      ()          => set((s) => ({ sidebarOpen: !s.sidebarOpen })),
      toggleTheme:        ()          => set((s) => ({ theme: s.theme === 'dark' ? 'light' : 'dark' })),
      setNotifCount:      (n)         => set({ notifCount: n }),
      decrementNotif:     ()          => set((s) => ({ notifCount: Math.max(0, s.notifCount - 1) })),
    }),
    {
      name: 'bookcar_ui',
      storage: createJSONStorage(() => localStorage),
      partialize: (s) => ({ theme: s.theme, sidebarOpen: s.sidebarOpen }),
    }
  )
)

// ─── Chat Store ──────────────────────────────────────────────────────────────
export const useChatStore = create((set) => ({
  messages:      {},   // { [bookingId]: Message[] }
  openChatId:    null,

  addMessage: (bookingId, msg) =>
    set((s) => ({
      messages: {
        ...s.messages,
        [bookingId]: [...(s.messages[bookingId] || []), msg],
      },
    })),

  setMessages: (bookingId, msgs) =>
    set((s) => ({
      messages: { ...s.messages, [bookingId]: msgs },
    })),

  setOpenChatId: (id) => set({ openChatId: id }),
  closChat:      ()   => set({ openChatId: null }),
}))

// ─── Driver Store ────────────────────────────────────────────────────────────
export const useDriverStore = create(
  persist(
    (set) => ({
      isOnline:         false,
      currentTrip:      null,
      availableBookings: [],

      setOnline:             (v)        => set({ isOnline: v }),
      setCurrentTrip:        (trip)     => set({ currentTrip: trip }),
      clearCurrentTrip:      ()         => set({ currentTrip: null }),
      setAvailableBookings:  (bookings) => set({ availableBookings: bookings }),
    }),
    {
      name: 'bookcar_driver',
      storage: createJSONStorage(() => localStorage),
      partialize: (s) => ({ isOnline: s.isOnline }),
    }
  )
)
