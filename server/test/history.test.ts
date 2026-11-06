import { describe, it, expect } from 'vitest'
import app from '../src/index'
import { sign } from 'hono/jwt'

const JWT_SECRET = 'test-secret-123'
const tokenUser1 = await sign({ sub: 'user_1' }, JWT_SECRET)
const AUTH = { Authorization: `Bearer ${tokenUser1}` }

describe('History CRUD + RLS', () => {
  let histId: string

  it('POST /v1/history creates history item', async () => {
    const res = await app.request('/v1/history', {
      method: 'POST',
      headers: { ...AUTH, 'Content-Type': 'application/json' },
      body: JSON.stringify({ storyId: 'story_123' }),
    })
    expect(res.status).toBe(201)
    const body = await res.json() as any // Фикс для tsc
    expect(body.id).toBeDefined()
    histId = body.id
  })

  it('GET /v1/history returns own history', async () => {
    const res = await app.request('/v1/history', { headers: AUTH })
    expect(res.status).toBe(200)
    const body = await res.json() as any // Фикс для tsc
    expect(body.items.length).toBeGreaterThan(0)
  })

  it('POST /v1/history returns 400 if storyId missing', async () => {
    const res = await app.request('/v1/history', {
      method: 'POST',
      headers: { ...AUTH, 'Content-Type': 'application/json' },
      body: JSON.stringify({ wrongField: '123' }),
    })
    expect(res.status).toBe(400)
  })

  it('DELETE /v1/history/:id removes item', async () => {
    const res = await app.request(`/v1/history/${histId}`, { 
      method: 'DELETE', 
      headers: AUTH 
    })
    expect(res.status).toBe(204)
  })

  it('GET /v1/history returns 401 without token', async () => {
    const res = await app.request('/v1/history')
    expect(res.status).toBe(401)
  })

  it('DELETE /v1/history/:id returns 404 for wrong ID', async () => {
    const res = await app.request('/v1/history/wrong_id', {
      method: 'DELETE',
      headers: AUTH
    })
    expect(res.status).toBe(404)
  })
})