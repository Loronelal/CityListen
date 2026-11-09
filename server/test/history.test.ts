import { describe, it, expect, vi } from 'vitest'
import app from '../src/index'
import { sign } from 'hono/jwt'

const JWT_SECRET = 'test-secret-123'
const tokenUser1 = await sign({ sub: 'user_1' }, JWT_SECRET)
const AUTH = { Authorization: `Bearer ${tokenUser1}` }

const mockEnv = {
  JWT_SECRET,
  GROQ_API_KEY: 'test-groq-key',
  CACHE: {
    get: vi.fn().mockResolvedValue(null),
    put: vi.fn().mockResolvedValue(undefined),
  },
  DB: {
    prepare: vi.fn().mockReturnValue({
      bind: vi.fn().mockReturnValue({
        run: vi.fn().mockResolvedValue({ meta: { changes: 1 } }),
        all: vi.fn().mockResolvedValue({ results: [{ id: 'hist_1', user_id: 'user_1', story_id: 'story_123', listened_at: new Date().toISOString() }] })
      })
    })
  }
} as any

describe('History CRUD + RLS', () => {
  it('POST /v1/history creates history item', async () => {
    const res = await app.request('/v1/history', {
      method: 'POST',
      headers: { ...AUTH, 'Content-Type': 'application/json' },
      body: JSON.stringify({ storyId: 'story_123' }),
    }, mockEnv)
    expect(res.status).toBe(201)
    const body = await res.json() as any
    expect(body.id).toBeDefined()
  })

  it('GET /v1/history returns own history', async () => {
    const res = await app.request('/v1/history', { headers: AUTH }, mockEnv)
    expect(res.status).toBe(200)
    const body = await res.json() as any
    expect(body.items.length).toBeGreaterThan(0)
  })

  it('POST /v1/history returns 400 if storyId missing', async () => {
    const res = await app.request('/v1/history', {
      method: 'POST',
      headers: { ...AUTH, 'Content-Type': 'application/json' },
      body: JSON.stringify({ wrongField: '123' }),
    }, mockEnv)
    expect(res.status).toBe(400)
  })

  it('DELETE /v1/history/:id removes item', async () => {
    const res = await app.request('/v1/history/hist_1', { 
      method: 'DELETE', 
      headers: AUTH 
    }, mockEnv)
    expect(res.status).toBe(204)
  })

  it('GET /v1/history returns 401 without token', async () => {
    const res = await app.request('/v1/history', {}, mockEnv)
    expect(res.status).toBe(401)
  })

  it('DELETE /v1/history/:id returns 404 for wrong ID', async () => {
    mockEnv.DB.prepare = vi.fn().mockReturnValue({
      bind: vi.fn().mockReturnValue({
        run: vi.fn().mockResolvedValue({ meta: { changes: 0 } })
      })
    })
    const res = await app.request('/v1/history/wrong_id', {
      method: 'DELETE',
      headers: AUTH
    }, mockEnv)
    expect(res.status).toBe(404)
  })
})