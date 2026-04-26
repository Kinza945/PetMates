# PetMates Mobile API Contract (Draft)

Дата: 2026-04-21  
Источник требований: `C:\Users\calculator2.0\AndroidStudioProjects\PetMates\build\petmates_docx\document_clean.txt` (из `PetMates.docx`)

Цель документа: зафиксировать минимальный контракт (эндпоинты, параметры, модели, ошибки), который нужен Android-клиенту, чтобы:
1. Работать в режиме гостя (без авторизации).
2. Работать в режиме авторизованного пользователя (отклики, приглашения, уведомления, управление проектами).
3. Позже подключить реальный сервер без переписывания UI/логики.

## Термины
1. Проект = pet-проект (`projects`)
2. Вакансия = заявка проекта на участника (`vacancies`)
3. Отклик = ответ пользователя на вакансию (`responses`)
4. Приглашение = приглашение пользователя в проект (`invites`)
5. Уведомление = событие по отклику/приглашению/проекту (`notifications`)

## Роли и доступ
1. Guest (неавторизованный): просмотр ленты проектов, поиск/фильтры, просмотр пользователей и чужих профилей/проектов.
2. User (авторизованный): всё из Guest + отклики, приглашения, управление своими проектами, уведомления.
3. Moderator/Admin/Owner: админские функции (не блокируем разработку клиента, но закладываем коды ошибок).

## Общие правила API
Базовый префикс: `https://{host}/v1`

### Аутентификация
1. `Authorization: Bearer <access_token>`
2. Для guest-запросов заголовок не требуется.

### Пагинация
Поддерживаем 2 режима (любой из них, но желательно выбрать 1 и придерживаться везде):
1. Offset-пагинация: `limit`, `offset`
2. Cursor-пагинация: `limit`, `cursor` (непрозрачная строка)

Рекомендация: cursor-пагинация для ленты/уведомлений/поиска.

### Сортировка
Единый параметр: `sort`
1. Значения: `created_at_desc`, `created_at_asc`, `last_online_desc`, `rating_desc`
2. Если `sort` не указан, дефолт: `created_at_desc`

### Формат ошибок
Единый JSON-формат для всех не-2xx:
```json
{
  "code": "validation_error",
  "message": "Human readable message",
  "details": {
    "field": "email",
    "reason": "invalid_format"
  },
  "trace_id": "b5c5c3c2-...."
}
```

Коды ошибок (минимум):
1. `unauthorized` (401)
2. `forbidden` (403)
3. `not_found` (404)
4. `conflict` (409)
5. `validation_error` (422)
6. `rate_limited` (429)
7. `internal_error` (500)

## Сущности (контрактные модели)
Ниже приведены минимальные поля, которые реально нужны клиенту сейчас. Сервер может возвращать больше.

### User
```json
{
  "user_id": "uuid",
  "nickname": "DogI1X",
  "avatar_url": "https://...",
  "real_name": "Гринькин Вадим",
  "age": 21,
  "gender": "male",
  "country": "Россия",
  "city": "Краснодар",
  "workplace": "ИМСИТ",
  "profile_role": "Python Data Science",
  "system_role": "user",
  "description": "....",
  "hard_skills": ["#kotlin", "#ktor"],
  "soft_skills": ["#teamwork"],
  "contacts": [{"name": "Telegram", "link": "https://t.me/..."}],
  "last_online_at": "2026-04-21T09:20:00Z",
  "created_at": "2026-02-01T10:00:00Z"
}
```

### Project
```json
{
  "project_id": "uuid",
  "owner_id": "uuid",
  "name": "Приложение Contacts",
  "short_description": "Короткое описание для карточек",
  "full_description": "Полное описание",
  "status": "in_progress",
  "status_changed_at": "2026-04-20T10:00:00Z",
  "rating_count": 4,
  "created_at": "2026-04-01T10:00:00Z"
}
```

### Vacancy
```json
{
  "vacancy_id": "uuid",
  "project_id": "uuid",
  "title": "Frontend-разработчик",
  "role": "Frontend",
  "description": "Описание",
  "required_tags": ["#react", "#web"],
  "is_open": true,
  "published_at": "2026-04-10T10:00:00Z"
}
```

### Response
```json
{
  "response_id": "uuid",
  "user_id": "uuid",
  "vacancy_id": "uuid",
  "status": "pending",
  "created_at": "2026-04-12T10:00:00Z"
}
```

### Invite
```json
{
  "invite_id": "uuid",
  "user_id": "uuid",
  "project_id": "uuid",
  "role": "Backend",
  "status": "pending",
  "created_at": "2026-04-12T10:00:00Z"
}
```

### Notification
```json
{
  "notification_id": "uuid",
  "user_id": "uuid",
  "category": "response",
  "event_type": "response.rejected",
  "reference_type": "response",
  "reference_id": "uuid",
  "context_data": {
    "project_name": "Приложение Contacts",
    "vacancy_title": "Frontend-разработчик"
  },
  "is_read": false,
  "created_at": "2026-04-12T10:00:00Z"
}
```

## Эндпоинты (backlog для backend)

### 1) Лента проектов (getFeedProjects)
`GET /v1/feed/projects`

Query:
1. `q` (string, optional) поиск по названию
2. `tags` (repeatable или comma-separated, optional) фильтр по тегам
3. `status` (enum: `in_progress|paused|completed`, optional)
4. `sort` (string, optional)
5. `limit`, `offset` или `cursor`

Response: `200 OK`
```json
{
  "items": [/* Project[] */],
  "next_cursor": "opaque-or-null"
}
```

### 2) Поиск пользователей (searchUsers)
`GET /v1/users`

Query:
1. `q` (string, optional) ник/роль/навыки
2. `country` (string, optional)
3. `city` (string, optional)
4. `skills` (repeatable/comma-separated, optional)
5. `sort` (optional): `last_online_desc`
6. `limit`, `offset` или `cursor`

Response: `200 OK`
```json
{
  "items": [/* User[] */],
  "next_cursor": "opaque-or-null"
}
```

### 3) Публичный профиль пользователя
`GET /v1/users/{nickname}`

Response: `200 OK`
```json
{
  "user": {/* User */},
  "projects": [/* Project[] */]
}
```

### 4) Детали проекта (getProjectDetails)
`GET /v1/projects/{project_id}`

Response: `200 OK`
```json
{
  "project": {/* Project */},
  "owner": {/* User */},
  "members": [/* ProjectMember[] */],
  "vacancies": [/* Vacancy[] */]
}
```

### 5) Вакансии проекта (getVacancies)
`GET /v1/projects/{project_id}/vacancies`

Query:
1. `is_open` (boolean, optional)
2. `q` (string, optional) поиск по title/role

Response: `200 OK` -> `Vacancy[]`

### 6) Откликнуться на вакансию (respond)
`POST /v1/vacancies/{vacancy_id}/responses`

Body (опционально, если хотим комментарий позже):
```json
{
  "message": "optional"
}
```

Response: `201 Created` -> `Response`

Ошибки:
1. `401 unauthorized` если гость
2. `409 conflict` если отклик уже существует (1 отклик на 1 вакансию от 1 пользователя)

### 7) Принять/отклонить отклик (acceptReject response)
`PATCH /v1/responses/{response_id}`

Body:
```json
{
  "status": "accepted"
}
```

Response: `200 OK` -> `Response`

Ошибки:
1. `403 forbidden` если не владелец проекта
2. `422 validation_error` если статус некорректный

### 8) Пригласить пользователя в проект (invite)
`POST /v1/projects/{project_id}/invites`

Body:
```json
{
  "user_id": "uuid",
  "role": "Backend",
  "message": "optional"
}
```

Response: `201 Created` -> `Invite`

Ошибки:
1. `403 forbidden` если не владелец проекта
2. `409 conflict` если приглашение уже отправлено и активно

### 9) Принять/отклонить приглашение
`PATCH /v1/invites/{invite_id}`

Body:
```json
{
  "status": "accepted"
}
```

Response: `200 OK` -> `Invite`

### 10) Мои входящие/исходящие отклики (для экрана "Заявки")
`GET /v1/me/responses`

Query:
1. `direction` (enum: `incoming|outgoing`)
2. `status` (optional: `pending|accepted|rejected`)
3. `limit`, `offset` или `cursor`

Response: `200 OK`
```json
{
  "items": [
    {
      "response": {/* Response */},
      "vacancy": {/* Vacancy */},
      "project": {/* Project */},
      "user": {/* User */} 
    }
  ],
  "next_cursor": "opaque-or-null"
}
```

Примечание:
1. Для `incoming` сервер возвращает отклики на вакансии проектов текущего пользователя.
2. Для `outgoing` сервер возвращает мои отклики.

### 11) Уведомления: список (notifications list)
`GET /v1/me/notifications`

Query:
1. `is_read` (boolean, optional)
2. `category` (optional: `response|invitation|project`)
3. `project_id` (optional) фильтр по проекту, если сервер умеет
4. `limit`, `offset` или `cursor`

Response: `200 OK`
```json
{
  "items": [/* Notification[] */],
  "next_cursor": "opaque-or-null"
}
```

### 12) Уведомления: отметить прочитанным (notifications markRead)
`POST /v1/me/notifications/mark-read`

Body:
```json
{
  "notification_ids": ["uuid", "uuid"]
}
```

Response: `204 No Content`

### 13) Уведомления: удалить (notifications delete)
`DELETE /v1/me/notifications/{notification_id}`

Response: `204 No Content`

### 14) Проекты: создать/редактировать (для будущей формы)
`POST /v1/projects`
`PATCH /v1/projects/{project_id}`

Минимальные поля для валидации (из документа):
1. `status`
2. `name`
3. `short_description`

## Список “обязательных” server-sent event types (event_type)
1. `response.created`
2. `response.accepted`
3. `response.rejected`
4. `invite.created`
5. `invite.accepted`
6. `invite.declined`
7. `project.status_changed`
8. `project.deleted` (модерация)
9. `user.banned` (модерация)

