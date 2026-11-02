import { cloudflareTest } from '@cloudflare/vitest-plugin'
import { defineConfig } from 'vitest/config'

export default defineConfig({
  plugins: [
    cloudflareTest({
      wrangler: { configPath: './wrangler.toml' },
      miniflare: {
        kvNamespaces: ['CACHE'],
        bindings: {
          JWT_SECRET: 'test-secret-123',
          GROQ_API_KEY: 'test-groq-key',
        },
      },
    }),
  ],
})