# UI caching (клиентское кеширование)

## Зачем

При переключении вкладок `Профиль` / `Участники` экраны пересоздавались, а данные заново запрашивались и UI уходил в `Loading`.

## Что сделано

- `MainScreen` держит `ProfileViewModel` и `UsersViewModel` на своём scope и передаёт их вниз, чтобы VM не пересоздавались при переключении вкладок.
- Добавлен in-memory TTL-кеш на 60 секунд:
  - `UsersViewModel.load(query, force)` — кеш по ключу `query.trim()`.
  - `ProfileViewModel.refresh(force)` — кеш агрегированного состояния профиля.
  - `UserProfileViewModel.load(nickname, force)` — кеш по `nickname`.

## Как принудительно обновить

- Для экрана с ошибкой/кнопкой «Повторить» используется `force = true` (обходит кеш).

