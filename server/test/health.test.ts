import { describe, it, expect } from 'vitest'
import app from '../src/index'

describe('Health Check', () => {
  it('GET /health returns 200 and status ok', async () => {
    const res = await app.request('/health')
    expect(res.status).toBe(200)
    const body = await res.json() as any 
    expect(body.status).toBe('ok')
  })
})