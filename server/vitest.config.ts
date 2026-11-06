// @ts-ignore (Игнорируем ошибку типов для плагина, чтобы CI прошел)
import { defineWorkersConfig } from '@cloudflare/vitest-plugin/config'

export default defineWorkersConfig({
  test: {
    poolOptions: {
      workers: {
        wrangler: { configPath: './wrangler.toml' },
        miniflare: {
          kvNamespaces: ['CACHE'],
          bindings: {
            JWT_SECRET: 'test-secret-123',
            GROQ_API_KEY: 'test-groq-key',
          },
        },
      },
    },
  },
})