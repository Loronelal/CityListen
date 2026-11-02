-- Таблица рассказов
CREATE TABLE IF NOT EXISTS stories (
  id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,    -- ID пользователя (для RLS)
  place_id TEXT NOT NULL,
  title TEXT,
  text TEXT,
  language TEXT,
  created_at TEXT
);

-- Таблица истории
CREATE TABLE IF NOT EXISTS history (
  id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,    -- ID пользователя (для RLS)
  story_id TEXT NOT NULL,
  listened_at TEXT
);

-- Индексы для ускорения выборки (RLS будет фильтровать по user_id)
CREATE INDEX IF NOT EXISTS idx_stories_user ON stories(user_id);
CREATE INDEX IF NOT EXISTS idx_history_user ON history(user_id);