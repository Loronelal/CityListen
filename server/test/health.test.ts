import { describe, it, expect } from 'vitest'
import app from '../src/index'

const mockEnv = { JWT_SECRET: 'test-secret-123', GROQ_API_KEY: 'test-groq-key' }

describe('Health Check', () => {
  it('GET /health returns 200 and status ok', async () => {
    const res = await app.request('/health', {}, mockEnv as any)
    expect(res.status).toBe(200)
    const body = await res.json() as any
    expect(body.status).toBe('ok')
  })
})