import { z } from 'zod'

// ─── Core Response Wrapper ───────────────────────────────────────────────
export const createApiResponseSchema = (resultSchema) => z.object({
  status: z.number().optional().default(200),
  message: z.string().optional().default(''),
  result: resultSchema.nullable().optional().catch(null),
})

// ─── Base Entities ───────────────────────────────────────────────────────
export const RoleSchema = z.object({
  roleName: z.string().catch(''),
  description: z.string().nullable().catch(''),
})

export const AccountSchema = z.object({
  accountId: z.string().catch(''),
  userName: z.string().catch(''),
  role: RoleSchema.optional().catch({}),
  accountStatus: z.boolean().catch(true),
  createdAt: z.string().nullable().optional().catch(null),
})

export const VehicleTypeSchema = z.object({
  vehicleTypeId: z.string().catch(''),
  vehicleTypeName: z.string().catch(''),
  pricePerKm: z.number().catch(0),
  maxPassengers: z.number().catch(0),
  icon: z.string().catch(''),
})

// ─── Feature DTOs ────────────────────────────────────────────────────────

export const AuthenticationResponseSchema = z.object({
  success: z.boolean().catch(false),
  token: z.string().optional().catch(''),
  refreshToken: z.string().catch(''),
  account: AccountSchema.optional().catch({}),
}).passthrough()

export const CustomerProfileSchema = z.object({
  customerId: z.string().catch(''),
  customerName: z.string().catch(''),
  phone: z.string().catch(''),
  address: z.string().catch(''),
  email: z.string().nullable().catch(''),
  gender: z.string().nullable().catch(''),
  avatar: z.string().nullable().catch(''),
  birthDate: z.string().nullable().catch(''),
  account: AccountSchema.optional().catch({}),
}).transform(data => ({
  ...data,
  id:   data.customerId,
  name: data.customerName,
}))

export const DriverProfileSchema = z.object({
  driverId: z.string().catch(''),
  driverName: z.string().catch(''),
  birthDate: z.string().nullable().catch(''),
  citizenId: z.string().catch(''),
  drivingLicense: z.string().catch(''),
  criminalRecord: z.string().nullable().catch(''),
  phone: z.string().catch(''),
  email: z.string().nullable().catch(''),
  licensePlate: z.string().catch(''),
  vehicleName: z.string().catch(''),
  avatar: z.string().nullable().catch(''),
  activityStatus: z.boolean().catch(true),
  gender: z.string().nullable().catch(''),
  address: z.string().nullable().catch(''),
  area: z.string().nullable().catch(''),
  score: z.number().nullable().catch(0),
  currentLat: z.number().nullable().catch(null),
  currentLng: z.number().nullable().catch(null),
  vehicleTypeId: z.string().nullable().catch(''),
  vehicleTypeName: z.string().nullable().catch(''),
  pricePerKm: z.number().nullable().catch(0),
  account: AccountSchema.optional().catch({}),
}).transform(data => ({
  ...data,
  id:   data.driverId,
  name: data.driverName,
  rating: data.score,
}))

export const BookingDetailSchema = z.object({
  bookingId: z.string().catch(''),
  customerId: z.string().nullable().catch(''),
  customerName: z.string().nullable().catch(''),
  customerPhone: z.string().nullable().catch(''),
  driverId: z.string().nullable().catch(''),
  driverName: z.string().nullable().catch(''),
  driverPhone: z.string().nullable().catch(''),
  vehicleTypeName: z.string().nullable().catch(''),
  licensePlate: z.string().nullable().catch(''),
  pickupLocation: z.string().catch(''),
  dropoffLocation: z.string().catch(''),
  pickupLat: z.number().nullable().catch(null),
  pickupLng: z.number().nullable().catch(null),
  dropoffLat: z.number().nullable().catch(null),
  dropoffLng: z.number().nullable().catch(null),
  originalPrice: z.number().catch(0),
  totalPrice: z.number().catch(0),
  bookingTime: z.string().nullable().catch(''),
  pickupTime: z.string().nullable().catch(''),
  arrivalTime: z.string().nullable().catch(''),
  bookingStatus: z.string().catch('PENDING'),
  distance: z.number().nullable().catch(0),
  duration: z.number().nullable().catch(0),
  paymentMethod: z.string().nullable().catch(''),
  paymentStatus: z.boolean().nullable().catch(false),
  promotionCode: z.string().nullable().catch(''),
  rating: z.number().nullable().catch(null),
  review: z.string().nullable().catch(''),
})

export const AvailableRideSchema = z.object({
  bookingId: z.string().catch(''),
  customerId: z.string().catch(''),
  pickupLocation: z.string().catch(''),
  dropoffLocation: z.string().catch(''),
  pickupLat: z.number().nullable().catch(null),
  pickupLng: z.number().nullable().catch(null),
  dropoffLat: z.number().nullable().catch(null),
  dropoffLng: z.number().nullable().catch(null),
  distance: z.number().catch(0),
  price: z.number().catch(0),
  bookingStatus: z.string().catch('PENDING'),
})

export const DriverDashboardSchema = z.object({
  totalRides: z.number().catch(0),
  totalIncome: z.number().catch(0),
  todayIncome: z.number().catch(0),
  averageRating: z.number().catch(0),
})

export const RevenueSummarySchema = z.object({
  totalRevenue: z.number().catch(0),
  totalTrips: z.number().catch(0),
})

export const RevenueDetailSchema = z.object({
  timeLabel: z.string().catch(''),
  tripCount: z.number().catch(0),
  revenue: z.number().catch(0),
})

export const DriverRevenueSchema = z.object({
  summary: RevenueSummarySchema.optional().catch({}),
  details: z.array(RevenueDetailSchema).catch([]),
})


export const DailyRevenueSchema = z.object({
  date: z.string().catch(''),
  grossRevenue: z.number().catch(0),
  netIncome: z.number().catch(0),
  platformFee: z.number().catch(0),
  cashIncome: z.number().catch(0),
  onlineIncome: z.number().catch(0),
  totalTrips: z.number().catch(0),
  questGoal: z.number().catch(0),
  questReward: z.number().catch(0),
  isQuestCompleted: z.boolean().catch(false),
  questEarned: z.number().catch(0),
  finalIncome: z.number().catch(0),
})

export const EstimatePriceSchema = z.object({
  vehicleTypeId: z.string().catch(''),
  distance: z.number().catch(0),
  basePrice: z.number().catch(0),
  surcharge: z.number().catch(0),
  surgeMultiplier: z.number().catch(1),
  originalPrice: z.number().catch(0),
  totalPrice: z.number().catch(0),
  discount: z.number().catch(0),
  quoteId: z.string().nullable().catch(null),
  expiryTime: z.number().nullable().catch(null),
})

export const PaymentResponseSchema = z.object({
  status: z.string().catch(''),
  message: z.string().catch(''),
  paymentUrl: z.string().catch(''),
  orderId: z.string().catch(''),
  transactionId: z.string().catch(''),
  amount: z.number().catch(0),
  paymentMethod: z.string().catch(''),
})

export const WalletSchema = z.object({
  walletId: z.string().catch(''),
  balance: z.number().catch(0),
  isActive: z.boolean().catch(false),
})

export const WalletTransactionSchema = z.object({
  transactionId: z.string().catch(''),
  amount: z.number().catch(0),
  type: z.string().catch(''),
  status: z.string().catch(''),
  createdAt: z.string().catch(''),
})

export const NotificationSchema = z.object({
  id: z.string().or(z.number()).catch(''),
  content: z.string().catch(''),
  read: z.boolean().catch(false),
  createdAt: z.string().catch(''),
})

export const PromotionSchema = z.object({
  promotionId: z.string().catch(''),
  promotionCode: z.string().catch(''),
  promotionName: z.string().catch(''),
  discountLimit: z.number().nullable().catch(0),
  startTime: z.string().nullable().catch(''),
  endTime: z.string().nullable().catch(''),
  applicationCondition: z.string().nullable().catch(''),
  quantity: z.number().nullable().catch(0),
  isActive: z.boolean().catch(false),
  promotionImage: z.string().nullable().catch(''),
  discountType: z.string().nullable().catch(''),
  discountValue: z.number().nullable().catch(0),
  minTripValue: z.number().nullable().catch(0),
  usageLimitPerUser: z.number().nullable().catch(0),
  // Admin stats fields
  usedCount: z.number().nullable().catch(null),
  savedCount: z.number().nullable().catch(null),
  isExpired: z.boolean().nullable().catch(null),
})

export const RatingSchema = z.object({
  ratingId: z.string().catch(''),
  bookingId: z.string().catch(''),
  score: z.number().catch(5),
  review: z.string().nullable().catch(''),
  createdAt: z.string().nullable().catch(''),
})

export const ChatMessageSchema = z.object({
  id: z.string().catch(''),
  bookingId: z.string().catch(''),
  senderId: z.string().catch(''),
  content: z.string().catch(''),
  timestamp: z.string().nullable().catch(''),
})

// Parse helpers
export const parseApiResponse = (schema, data, fallback = null) => {
  try {
    // Nếu data truyền vào không có .result, có thể nó đã được parse hoặc backend lỗi 
    // Chúng ta thử parse xem
    const apiSchema = createApiResponseSchema(schema)
    const parsed = apiSchema.parse(data)
    return parsed.result ?? fallback
  } catch (error) {
    console.error('Zod Parse Error:', error)
    return fallback
  }
}

export const parseApiArrayResponse = (itemSchema, data) => {
  try {
    const arraySchema = z.array(itemSchema).catch([])
    const apiSchema = createApiResponseSchema(arraySchema)
    const parsed = apiSchema.parse(data)
    return parsed.result ?? []
  } catch (error) {
    console.error('Zod Parse Error:', error)
    return []
  }
}
