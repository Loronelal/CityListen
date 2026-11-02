import { Hono } from 'hono'
import { cors } from 'hono/cors'
import { logger } from 'hono/logger'
import { jwt } from 'hono/jwt'
import { z } from 'zod'

type Bindings = {
  GROQ_API_KEY: string
  JWT_SECRET: string
  CACHE: KVNamespace
  DB: D1Database
}

const app = new Hono<{ Bindings: Bindings }>()

app.use('*', logger())
app.use('*', cors())

const errorResponse = (code: string, message: string, status: number, details?: unknown) =>
  Response.json({ error: { code, message, details } }, { status })

app.get('/health', (c) =>
  c.json({ status: 'ok', version: '1.0.0', timestamp: new Date().toISOString() })
)

// ---------- 1. АВТОРИЗАЦИЯ (JWT) ----------
app.use('/v1/*', async (c, next) => {
  const jwtMiddleware = jwt({ secret: c.env.JWT_SECRET })
  return jwtMiddleware(c, next)
})

// ---------- 2. ВАЛИДАЦИЯ ----------
const generateSchema = z.object({
  placeId: z.string().min(1),
  language: z.enum(['ru', 'en']),
})

const historySchema = z.object({
  storyId: z.string().min(1),
})

// ==========================================
// 3. КОД С ИСПОЛЬЗОВАНИЕМ D1 И RLS
// ==========================================

// --- CRUD для STORIES ---

// CREATE
app.post('/v1/stories', async (c) => {
  const user = c.get('jwtPayload')
  const body = await c.req.json().catch(() => null)
  const parsed = generateSchema.safeParse(body)

  if (!parsed.success) return errorResponse('VALIDATION_ERROR', 'Invalid body', 400, parsed.error.flatten())
  const { placeId, language } = parsed.data

  const cacheKey = `story:${placeId}:${language}`
  const cached = await c.env.CACHE.get(cacheKey, 'json')
  
  let text = cached ? (cached as any).text : ''
  const storyId = `story_${crypto.randomUUID()}`
  const now = new Date().toISOString()

  if (!cached) {
    const prompt = `Ты — краевед. Составь короткий (3–5 предложений) рассказ об объекте с id ${placeId} на языке ${language}.`
    try {
      const groqRes = await fetch('https://api.groq.com/openai/v1/chat/completions', {
        method: 'POST',
        headers: { Authorization: `Bearer ${c.env.GROQ_API_KEY}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({
          model: 'llama-3.3-70b-versatile',
          messages: [{ role: 'user', content: prompt }],
        }),
      })
      const groqData = await groqRes.json()
      text = groqData.choices?.[0]?.message?.content ?? ''
      await c.env.CACHE.put(cacheKey, JSON.stringify({ text }), { expirationTtl: 86400 })
    } catch {
      return errorResponse('UPSTREAM_ERROR', 'Groq failed', 502)
    }
  }

  // RLS: Привязываем к user.sub
  await c.env.DB.prepare(
    'INSERT INTO stories (id, user_id, place_id, title, text, language, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)'
  ).bind(storyId, user.sub, placeId, 'Рассказ о месте', text, language, now).run()

  return c.json({ id: storyId, placeId, title: 'Рассказ о месте', text, language, createdAt: now, cached: !!cached })
})

// READ (Список)
app.get('/v1/stories', async (c) => {
  const user = c.get('jwtPayload')
  // RLS: WHERE user_id = ?
  const { results } = await c.env.DB.prepare(
    'SELECT * FROM stories WHERE user_id = ? ORDER BY created_at DESC'
  ).bind(user.sub).all()
  return c.json({ items: results })
})

// READ (Один)
app.get('/v1/stories/:id', async (c) => {
  const user = c.get('jwtPayload')
  const id = c.req.param('id')
  // RLS: WHERE id = ? AND user_id = ?
  const { results } = await c.env.DB.prepare(
    'SELECT * FROM stories WHERE id = ? AND user_id = ?'
  ).bind(id, user.sub).all()

  if (results.length === 0) return errorResponse('NOT_FOUND', 'Story not found or access denied', 404)
  return c.json(results[0])
})

// DELETE
app.delete('/v1/stories/:id', async (c) => {
  const user = c.get('jwtPayload')
  const id = c.req.param('id')
  // RLS
  const { meta } = await c.env.DB.prepare(
    'DELETE FROM stories WHERE id = ? AND user_id = ?'
  ).bind(id, user.sub).run()

  if (meta.changes === 0) return errorResponse('NOT_FOUND', 'Story not found or access denied', 404)
  return c.body(null, 204)
})


// --- CRUD для HISTORY ---

// CREATE
app.post('/v1/history', async (c) => {
  const user = c.get('jwtPayload')
  const body = await c.req.json().catch(() => null)
  const parsed = historySchema.safeParse(body)
  
  if (!parsed.success) return errorResponse('VALIDATION_ERROR', 'Invalid body', 400)

  const histId = `hist_${crypto.randomUUID()}`
  const now = new Date().toISOString()

  await c.env.DB.prepare(
    'INSERT INTO history (id, user_id, story_id, listened_at) VALUES (?, ?, ?, ?)'
  ).bind(histId, user.sub, parsed.data.storyId, now).run()

  return c.json({ id: histId, storyId: parsed.data.storyId, listenedAt: now }, 201)
})

// READ
app.get('/v1/history', async (c) => {
  const user = c.get('jwtPayload')
  const page = Math.max(1, parseInt(c.req.query('page') ?? '1'))
  const limit = Math.min(100, Math.max(1, parseInt(c.req.query('limit') ?? '20')))
  const offset = (page - 1) * limit

  // RLS
  const { results } = await c.env.DB.prepare(
    'SELECT * FROM history WHERE user_id = ? ORDER BY listened_at DESC LIMIT ? OFFSET ?'
  ).bind(user.sub, limit, offset).all()

  return c.json({ items: results, page, limit })
})

// DELETE
app.delete('/v1/history/:id', async (c) => {
  const user = c.get('jwtPayload')
  const id = c.req.param('id')

  // RLS
  const { meta } = await c.env.DB.prepare(
    'DELETE FROM history WHERE id = ? AND user_id = ?'
  ).bind(id, user.sub).run()

  if (meta.changes === 0) return errorResponse('NOT_FOUND', 'History item not found', 404)
  return c.body(null, 204)
})

export default app