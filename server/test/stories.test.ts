import { describe, it, expect, vi } from 'vitest'
import app from '../src/index'
import { sign } from 'hono/jwt'

const JWT_SECRET = 'test-secret-123'
const tokenUser1 = await sign({ sub: 'user_1' }, JWT_SECRET)
const AUTH = { Authorization: `Bearer ${tokenUser1}` }

// Создаем полный мок окружения
const mockEnv = {
  JWT_SECRET,
  GROQ_API_KEY: 'test-groq-key',
  CACHE: {
    get: vi.fn().mockResolvedValue(null),
    put: vi.fn().mockResolvedValue(undefined),
  },
  DB: {
    prepare: vi.fn().mockImplementation((query) => ({
      bind: vi.fn().mockImplementation((...args) => ({
        run: vi.fn().mockResolvedValue({ meta: { changes: 1 } }),
        all: vi.fn().mockImplementation(() => {
          if (query.includes('SELECT') && args[0] === 'wrong_id') {
            return Promise.resolve({ results: [] }) // Для теста 404
          }
          return Promise.resolve({ results: [{ id: args[0], user_id: 'user_1', place_id: 'osm:1', text: 'Test', language: 'ru', created_at: new Date().toISOString() }] })
        }),
      })),
    })),
  },
} as any

describe('Stories CRUD + RLS', () => {
  it('POST /v1/stories creates story (Mocked Groq)', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ choices: [{ message: { content: 'Тестовый рассказ' } }] }), { status: 200 })
    ))

    const res = await app.request('/v1/stories', {
      method: 'POST',
      headers: { ...AUTH, 'Content-Type': 'application/json' },
      body: JSON.stringify({ placeId: 'osm:1', language: 'ru' }),
    }, mockEnv)
    expect(res.status).toBe(200)
    const body = await res.json() as any
    expect(body.id).toBeDefined()
  })

  it('GET /v1/stories returns own stories', async () => {
    const res = await app.request('/v1/stories', { headers: AUTH }, mockEnv)
    expect(res.status).toBe(200)
    const body = await res.json() as any
    expect(body.items.length).toBeGreaterThan(0)
  })

  it('GET /v1/stories/:id returns story (RLS)', async () => {
    const res = await app.request(`/v1/stories/123`, { headers: AUTH }, mockEnv)
    expect(res.status).toBe(200)
    const body = await res.json() as any
    expect(body.id).toBe('123')
  })

  it('POST /v1/stories returns 400 on invalid body', async () => {
    const res = await app.request('/v1/stories', {
      method: 'POST',
      headers: { ...AUTH, 'Content-Type': 'application/json' },
      body: JSON.stringify({ placeId: 'osm:1', language: 'de' }),
    }, mockEnv)
    expect(res.status).toBe(400)
  })

  it('GET /v1/stories returns 401 without token', async () => {
    const res = await app.request('/v1/stories', {}, mockEnv)
    expect(res.status).toBe(401)
  })

  it('GET /v1/stories/:id returns 404 for non-existent ID', async () => {
    const res = await app.request('/v1/stories/wrong_id', { headers: AUTH }, mockEnv)
    expect(res.status).toBe(404)
  })

  it('DELETE /v1/stories/:id returns 404 for wrong ID', async () => {
    mockEnv.DB.prepare = vi.fn().mockReturnValue({
      bind: vi.fn().mockReturnValue({
        run: vi.fn().mockResolvedValue({ meta: { changes: 0 } }) // 0 изменений = 404
      })
    })
    const res = await app.request('/v1/stories/wrong_id', {
      method: 'DELETE',
      headers: AUTH
    }, mockEnv)
    expect(res.status).toBe(404)
  })

  it('POST /v1/stories returns 502 if Groq fails', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(
      new Response('Server Error', { status: 500 })
    ))
    
    const res = await app.request('/v1/stories', {
      method: 'POST',
      headers: { ...AUTH, 'Content-Type': 'application/json' },
      body: JSON.stringify({ placeId: 'osm:error', language: 'ru' }),
    }, mockEnv)
    expect(res.status).toBe(502)
  })
})