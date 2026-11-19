## Навигационный граф

```mermaid
graph TD
    App[CityListenRoot] --> Onboarding[OnboardingRoute]
    App --> Walk[WalkRoute<br/>Прогулка]
    App --> History[HistoryRoute<br/>История]
    App --> Map[MapRoute<br/>Карта]
    App --> Settings[SettingsRoute<br/>Настройки]
    History --> Story[StoryRoute<br/>storyId: String]
    Map --> Story
    Settings --> Profile[ProfileRoute<br/>Профиль]
    Settings --> Permission[PermissionRoute<br/>Разрешение]
    Walk --> Permission
    Permission --> Denied[PermissionDeniedRoute<br/>Отказ]
```

- `CityListenRoot` выбирает стартовый экран по флагу онбординга.
- `OnboardingRoute` после завершения заменяется на `WalkRoute`.
- `PermissionRoute` возвращает на предыдущий экран при согласии или «Не сейчас».
- `PermissionDeniedRoute` после выдачи разрешения в системных настройках возвращает назад.
- У всех маршрутов типобезопасные `@Serializable` объекты; аргумент есть только у `StoryRoute`.
