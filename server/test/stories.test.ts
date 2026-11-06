import { describe, it, expect, vi } from 'vitest'
import app from '../src/index'
import { sign } from 'hono/jwt'

const JWT_SECRET = 'test-secret-123'
const tokenUser1 = await sign({ sub: 'user_1' }, JWT_SECRET)
const AUTH = { Authorization: `Bearer ${tokenUser1}` }

describe('Stories CRUD + RLS', () => {
  let storyId: string

  it('POST /v1/stories creates story (Mocked Groq)', async () => {
    // Мокаем запрос к Groq API, чтобы тест проходил без реального интернета
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ choices: [{ message: { content: 'Тестовый рассказ' } }] }), { status: 200 })
    ))

    const res = await app.request('/v1/stories', {
      method: 'POST',
      headers: { ...AUTH, 'Content-Type': 'application/json' },
      body: JSON.stringify({ placeId: 'osm:1', language: 'ru' }),
    })
    expect(res.status).toBe(200)
    const body = await res.json()
    expect(body.id).toBeDefined()
    storyId = body.id
  })

  it('GET /v1/stories returns own stories', async () => {
    const res = await app.request('/v1/stories', { headers: AUTH })
    expect(res.status).toBe(200)
    const body = await res.json()
    expect(body.items.length).toBeGreaterThan(0)
  })

  it('GET /v1/stories/:id returns story (RLS)', async () => {
    const res = await app.request(`/v1/stories/${storyId}`, { headers: AUTH })
    expect(res.status).toBe(200)
    const body = await res.json()
    expect(body.id).toBe(storyId)
  })

  it('POST /v1/stories returns 400 on invalid body', async () => {
    const res = await app.request('/v1/stories', {
      method: 'POST',
      headers: { ...AUTH, 'Content-Type': 'application/json' },
      body: JSON.stringify({ placeId: 'osm:1', language: 'de' }), // Неверный язык
    })
    expect(res.status).toBe(400)
  })

  it('GET /v1/stories returns 401 without token', async () => {
    const res = await app.request('/v1/stories')
    expect(res.status).toBe(401)
  })
})