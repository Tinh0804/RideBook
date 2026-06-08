import { z } from 'zod'

const AccountSchema = z.object({
  accountId: z.string().catch(''),
  userName: z.string().catch(''),
  role: z.any().transform(r => typeof r === 'object' ? r?.roleName : r).catch(''),
  accountStatus: z.boolean().catch(true),
  createdAt: z.string().nullable().optional().catch(null),
})

const AuthenticationResponseSchema = z.object({
  success: z.boolean().catch(false),
  token: z.string().optional().catch(''),
  refreshToken: z.string().catch(''),
  account: AccountSchema.optional().catch({}),
}).passthrough()

const createApiResponseSchema = (resultSchema) => z.object({
  status: z.number().optional().default(200),
  message: z.string().optional().default(''),
  result: resultSchema.nullable().optional().catch(null),
})

const data = {
  "status": 200,
  "message": "Login successful",
  "result": {
    "success": true,
    "token": "123",
    "refreshToken": "456",
    "account": {
      "accountId": "838fee35-146e-4a91-bfea-3f909f1d6c5d",
      "userName": "0366900822",
      "role": {
        "roleName": "CUSTOMER",
        "description": "Khách hàng"
      },
      "accountStatus": true,
      "createdAt": "2026-04-06T08:35:55.718+00:00"
    }
  }
}

try {
  const apiSchema = createApiResponseSchema(AuthenticationResponseSchema)
  const parsed = apiSchema.parse(data)
  console.log('Parsed:', JSON.stringify(parsed, null, 2))
} catch (err) {
  console.error('Error:', err)
}
