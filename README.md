# itmo-work-backend-lab4

## Высокопроизводительные системы Лабораторная №4 

### 1. Переписать все сервисы на Clean Architecture 
- **deadline: 20.12.2025, готовность - вечером**

| Сервис               | Ответственный | Готовность  |
|----------------------|---------------|-------------|
| user-service         | Маша          | ✅          |
| company-service      | Арслан        | ✅          |
| vacancy-service      | Маша          | ✅          |
| application-service  | Егор          | ✅          |
| file-service         | Егор          | ✅          |
| notification-service | Арслан        | ✅          |

### 2. Добавить обработку ошибок в file-service и application-service 
- **deadline: 23.12.2025**

| Сервис               | Ответственный | Готовность  |
|----------------------|---------------|-------------|
| file-service         | Егор          | ❌          |
| application-service  | Егор          | ❌          |

### 3. Добавить Kafka в каждый сервис (для передачи файла между application-service и file-service отправлять поток байтов через Kafka Stream) 
- **deadline: 22.12.2025**

| Сервис               | Ответственный                                 | Готовность  |
|----------------------|-----------------------------------------------|-------------|
| file-service         | Егор (+ мы поможем смотря какие там проблемы) | ✅          |
| application-service  | Егор (+ мы поможем смотря какие там проблемы) | ❌          |
| user-service         | Маша                                          | ❌          |
| company-service      | Арслан                                        | ❌          |
| vacancy-service      | Маша                                          | ❌          |
| notification-service | Арслан                                        | ❌          |

### 4. Написать тесты для каждого сервиса. Покрытие сервисов и контроллеров >70%
-  **deadline: 23.12.2025**

| Сервис               | Ответственный | Готовность  |
|----------------------|---------------|-------------|
| user-service         | Маша          | ❌          |
| company-service      | Арслан        | ❌          |
| vacancy-service      | Маша          | ❌          |
| application-service  | Егор          | ❌          |
| file-service         | Егор          | ❌          |
| notification-service | Арслан        | ❌          |

# Json-форматы 

## application.response (vacancy-service -> application-response):

### 1. GET /api/vacancies/{id}/exists

```json
{
  "event_type": "VACANCY_EXISTS",
  "correlation_id": "<id>",
  "ok": true,
  "payload": {
      "vacancy_id": "<id>"
      "result": true
  },
  "error_payload": null
}
```

### 2. GET  /api/vacancies/{id}/is-published

```json
{
  "event_type": "VACANCY_IS_PUBLISHED",
  "correlation_id": "<id>",
  "ok": true,
  "payload": {
      "vacancy_id": "<id>"
      "result": true
  },
  "error_payload": null
}
```

### 3. GET /api/vacancies/{id}/title

```json
{
  "event_type": "VACANCY_TITLE",
  "correlation_id": "<id>",
  "ok": true,
  "payload": {
      "vacancy_id": "<id>"
      "title": "<title>"
  },
  "error_payload": null
}
```

### 4. GET /api/vacancies/{vacancyId}/company-id

```json
{
  "event_type": "VACANCY_COMPANY_ID",
  "correlation_id": "<id>",
  "ok": true,
  "payload": {
      "vacancy_id": "<id>"
      "company_id": "<company-id>"
  },
  "error_payload": null
}
```

### Пример сообщения в случае ошибки в ходе выполнения: 

Запрос - GET /api/vacancies/{id}/title
```json
{
  "event_type": "VACANCY_TITLE",
  "correlation_id": "<id>",
  "ok": false,
  "payload": null,
  "error_payload": {
      "code": "VACANCY_ID_INVALID"
      "message": "Given vacancy_id is not found"
  }
}
```

#### Возможные code, которые могут вернуться в vacancy.response: 

`UNSUPPORTED_OPERATION` - операция в event_type не существует или равна null

`BAD_REQUEST` - любые проблемы с vacancy_id, в message будет прописано в чем проблема

`INTERNAL_ERROR` - unexpected error 

`FORBIDDEN` - недостаточно прав для выполнения запроса

`UNAUTHORIZED` - токен не был указан 

## vacancy.response (company-service -> vacancy.service):

### 1. GET /api/company/{companyId}

```json
{
  "event_type": "COMPANY_EXISTS",
  "correlation_id": "<id>",
  "ok": true,
  "payload": {
      "company_id": "<company_id>",
      "result": true
  },
  "error_payload": null
}
```

### 2. GET /api/company/{companyId}/{userId}

```json
{
  "event_type": "COMPANY_VALIDATE_OWNERSHIP",
  "correlation_id": "<id>",
  "ok": true,
  "payload": {
      "company_id": "<company_id>",
      "result": true
  },
  "error_payload": null
}
```


## application.request:

#### 1. GET /{id}/title

```json
{
  "event_type": "VACANCY_TITLE",
  "correlation_id": "b4c5f8c1-9d4f-4b1f-b4de-92a21c999999",
  "reply_to": "application.responses",
  "payload": {
    "vacancy_id": "e7b8d7d0-3a55-4b5e-8c2b-91d7e9c1a111",
  }
}
```

#### 2. GET /{id}/exists

```json
{
  "event_type": "VACANCY_EXISTS",
  "correlation_id": "eab4f2c1-9a22-4444-bbbb-cccccccccccc",
  "reply_to": "application.responses",
  "payload": {
    "vacancy_id": "e7b8d7d0-3a55-4b5e-8c2b-91d7e9c1a111",
  }
}
```

#### 3. GET /{id}/is-published

```json
{
  "event_type": "VACANCY_IS_PUBLISHED",
  "correlation_id": "11111111-2222-3333-4444-555555555555",
  "reply_to": "application.responses",
  "payload": {
    "vacancy_id": "e7b8d7d0-3a55-4b5e-8c2b-91d7e9c1a111",
  }
}
```

#### 4. GET /{vacancyId}/company-id

```json
{
  "event_type": "VACANCY_COMPANY_ID",
  "correlation_id": "9999eeee-aaaa-bbbb-cccc-dddddddddddd",
  "reply_to": "application.responses",
  "payload": {
    "vacancy_id": "e7b8d7d0-3a55-4b5e-8c2b-91d7e9c1a111",
  }
}
```

## company.request:
 
### 1. GET /api/company/{companyId}

```json
{
  "event_type": "COMPANY_EXISTS",
  "correlation_id": "<id>",
  "reply_to": "vacancy.response",
  "payload": {
      "company_id": "<company_id>"
  }
}
```

### 2. GET /api/company/{companyId}

```json
{
  "event_type": "COMPANY_VALIDATE_OWNERSHIP",
  "correlation_id": "<id>",
  "reply_to": "vacancy.response",
  "payload": {
      "company_id": "<company_id>",
      "user_id": "<user_id>"
  }
}
```

## user.request:

### 1. POST /api/auth/register-company-owner

```json
{
  "event_type": "USER_CREATE_EVENT",
  "correlation_id": "<id>",
  "reply_to": "company.response",
  "payload": {
      "owner_full_name": "<full_name>",
      "owner_email": "<owner_email>",
      "owner_password": "<owner_password>",
  }
}
```

### 2. GET /api/user/{id}

```json
{
  "event_type": "USER_EXISTS_EVENT",
  "correlation_id": "<id>",
  "reply_to": "application.response",
  "payload": {
      "owner_full_name": "<full_name>",
      "owner_email": "<owner_email>",
      "owner_password": "<owner_password>",
  }
}
```

