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
## vacancy-response: 

### 1. GET /api/vacancies/{id}/exists

```
{
  "vacancy_operations": "VACANCY_EXISTS",
  "ok": true,
  "payload": {
      "vacancy_id": "<id>"
      "published": true
  },
  "error_payload": null
}
```

### 2. GET  /api/vacancies/{id}/is-published

```
{
  "vacancy_operations": "VACANCY_IS_PUBLISHED",
  "ok": true,
  "payload": {
      "vacancy_id": "<id>"
      "published": true
  },
  "error_payload": null
}
```

### 3. GET /api/vacancies/{id}/title

```
{
  "vacancy_operations": "VACANCY_TITLE",
  "ok": true,
  "payload": {
      "vacancy_id": "<id>"
      "title": "<title>"
  },
  "error_payload": null
}
```

### 4. GET /api/vacancies/{vacancyId}/company-id

```
{
  "vacancy_operations": "VACANCY_COMPANY_ID",
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
```
{
  "vacancy_operations": "VACANCY_TITLE",
  "ok": false,
  "payload": null,
  "error_payload": {
      "code": "VACANCY_ID_INVALID"
      "message": "Given vacancy_id is not found"
  }
}
```
