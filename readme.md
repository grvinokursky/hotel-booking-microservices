# Hotel Booking Microservices (МИФИ)

Проект представляет собой распределенную микросервисную систему для бронирования отелей, реализованную в рамках учебного задания. 

**Стек технологий:**
* Java 17, Spring Boot 3.5.16
* Spring Cloud (Eureka, Gateway, OpenFeign, LoadBalancer)
* Spring Security + JWT (аутентификация и авторизация)
* Spring Data JPA + H2 (In-Memory база данных)
* Resilience4j (Retry, CircuitBreaker)
* Swagger / OpenAPI 3 (документация)
* MapStruct, Lombok

---

## 1. Архитектура и запуск системы

Система состоит из четырех модуле:

| № | Модуль | Порт | Назначение |
|---|---|---|---|
| 1 | `eureka-server` | 8761 | Service Registry (реестр сервисов) |
| 2 | `hotel-service` | 8081 | Управление отелями и номерами, проверка доступности |
| 3 | `booking-service` | 8082 | Управление пользователями и бронированиями, Сага |
| 4 | `api-gateway` | 8080 | API Gateway (маршрутизация запросов) |

---

## 2. Поддерживаемые эндпойнты

Все запросы (кроме внутренних) проходят через **API Gateway (порт 8080)**. Для доступа к защищенным эндпойнтам требуется передавать заголовок `Authorization: Bearer <JWT_TOKEN>`.

### Пользователи и Аутентификация (Booking Service)
| Метод | Эндпойнт | Роль | Описание |
|---|---|---|---|
| POST | `/user/register` | Public | Регистрация пользователя (возвращает JWT) |
| POST | `/user/auth` | Public | Авторизация (возвращает JWT) |
| GET | `/user` | ADMIN | Список пользователей |
| POST | `/user` | ADMIN | Создать пользователя |
| PATCH | `/user/{id}` | ADMIN | Обновить данные пользователя |
| DELETE | `/user/{id}` | ADMIN | Удалить пользователя |

### Бронирования (Booking Service)
| Метод | Эндпойнт | Роль | Описание |
|---|---|---|---|
| POST | `/booking` | USER | Создать бронирование (с авто-подбором или конкретным `roomId`) |
| GET | `/bookings` | USER | История бронирований текущего пользователя |
| GET | `/booking/{id}` | USER | Получить бронирование по ID |
| DELETE | `/booking/{id}` | USER | Отменить бронирование (перевод в `CANCELLED`) |

### Отели и Номера (Hotel Service)
| Метод | Эндпойнт | Роль | Описание |
|---|---|---|---|
| GET | `/api/hotels` | USER | Список отелей |
| GET | `/api/rooms/recommend` | USER | Рекомендуемые номера (сортировка по `timesBooked`) |
| POST | `/api/hotels` | ADMIN | Добавить отель |
| POST | `/api/rooms` | ADMIN | Добавить номер в отель |

*Внутренние эндпойнты (`POST /api/rooms/{id}/confirm-availability` и `POST /api/rooms/{id}/release`) доступны только внутри сети микросервисов и не маршрутизируются через Gateway.*

### Просмотр эндпоинтов через Swagger:

Hotel Service: http://localhost:8081/swagger-ui/index.html

Booking Service: http://localhost:8082/swagger-ui/index.html

---

## 3. Как протестировать код

Выполните следующие запросы в терминале (например, Git Bash), чтобы проверить работу бронирования:

**Регистрация и получение USER_TOKEN:**

```bash
curl -X POST http://localhost:8080/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"student","password":"password123"}'
```

**Бизнес-логика бронирования (Сага: Успех)**

```bash
curl -X POST http://localhost:8080/booking \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test-req-success-1",
    "autoSelect": true,
    "startDate": "2026-12-01",
    "endDate": "2026-12-05"
  }'
```

**Проверка идемпотентности**

Повторите запрос из предыдущего пункта с тем же самым `requestId` (`test-req-success-1`).

Ожидаемый результат: Система вернет тот же самый JSON с тем же `id` брони, не создавая дубликат и не увеличивая `timesBooked` у номера. В логах Booking Service появится запись: _Booking already exists_.

**Проверка конкурентности**

Поробуйте забронировать тот же самый номер на те же даты, но с другим `requestId` (и укажите `autoSelect: false`, передав `roomId`, который выдался в пункте "Бизнес-логика бронирования (Сага: Успех)")

```bash
curl -X POST http://localhost:8080/booking \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test-req-conflict-2",
    "autoSelect": false,
    "roomId": <ROOM_ID_ИЗ_ПУНКТА_БИЗНЕС-ЛОГИКА_БРОНИРОВАНИЯ_(САГА:_УСПЕХ)>,
    "startDate": "2026-12-03",
    "endDate": "2026-12-07"
  }'
```

Ожидаемый результат: Ошибка 409 Conflict. Даты пересекаются, Hotel Service заблокирует создание второй брони.

**Проверка компенсации (Сага: Ошибка)**

1. Остановите модуль HotelService
2. Выполните запрос на создание новой брони:

```bash
curl -X POST http://localhost:8080/booking \
  -H "Authorization: Bearer <ВАШ_USER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test-req-fail-3",
    "autoSelect": true,
    "startDate": "2026-12-10",
    "endDate": "2026-12-15"
  }'
```

3. Проверка БД: Зайдите в H2 Console Booking-сервиса (http://localhost:8082/h2-console, JDBC URL: `jdbc:h2:mem:bookingdb`, User: `sa`, без пароля). Выполните запрос:

```bash
SELECT * FROM BOOKINGS WHERE REQUEST_ID = 'test-req-fail-3';
```

Убедитесь, что статус `test-req-fail-3`: `CANCELLED`