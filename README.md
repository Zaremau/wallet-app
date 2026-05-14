# Wallet App

[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-✓-blue)](https://www.docker.com/)
[![Swagger](https://img.shields.io/badge/Swagger-✓-green)](https://swagger.io/)
[![Testcontainers](https://img.shields.io/badge/Testcontainers-✓-purple)](https://testcontainers.com/)

REST API приложение для управления цифровым кошельком. Позволяет создавать кошельки, пополнять баланс, снимать средства и проверять текущий баланс. Реализована пессимистическая блокировка для корректной работы в конкурентной среде.

---

## 📋 Содержание

- [Функциональность](#функциональность)
- [Технологический стек](#технологический-стек)
- [Архитектура](#архитектура)
- [Структура проекта](#структура-проекта)
- [Запуск проекта](#запуск-проекта)
- [API Документация](#api-документация)
- [Обработка ошибок](#обработка-ошибок)
- [Конкурентная работа](#конкурентная-работа)
- [Тестирование](#тестирование)
- [Конфигурация](#конфигурация)

---

## Функциональность

- ✅ Создание кошелька с начальным балансом
- ✅ Пополнение баланса (операция `DEPOSIT`)
- ✅ Снятие средств (операция `WITHDRAW`)
- ✅ Проверка текущего баланса кошелька
- ✅ Валидация входных данных через `jakarta.validation`
- ✅ Пессимистическая блокировка для конкурентных операций
- ✅ Единый формат ошибок через `ApiError`
- ✅ Полная документация API через Swagger UI
- ✅ Миграции базы данных через Liquibase
- ✅ Контейнеризация через Docker и Docker Compose

---

## Технологический стек

### Backend
- **Java 17**
- **Spring Boot 3.x**
- **Spring Web** — REST API
- **Spring Data JPA** — работа с базой данных
- **Liquibase** — миграции базы данных

### Валидация и документация
- **Jakarta Validation** — валидация DTO
- **SpringDoc OpenAPI / Swagger** — документация API

### База данных
- **PostgreSQL** — основная база данных

### Тестирование
- **JUnit 5** — модульное тестирование
- **Mockito** — мокирование
- **Testcontainers** — интеграционные тесты с реальной PostgreSQL
- **MockMvc** — тестирование контроллеров

### Инфраструктура
- **Docker** — контейнеризация приложения
- **Docker Compose** — запуск приложения с базой данных

---

## Архитектура

Приложение построено по классической трёхслойной архитектуре:
```
Controller (REST API)
↓
Service (Бизнес-логика)
↓
Repository (Доступ к данным через JPA)
```


### Основные сущности

**Wallet** — кошелёк пользователя
| Поле | Тип | Описание |
|------|-----|----------|
| `id` | `UUID` | Уникальный идентификатор |
| `balance` | `BigDecimal` | Текущий баланс |

**WalletRequest** — DTO для операций с кошельком
| Поле | Тип | Валидация |
|------|-----|-----------|
| `walletId` | `UUID` | `@NotNull` |
| `operationType` | `OperationType` | `DEPOSIT` или `WITHDRAW` |
| `amount` | `BigDecimal` | `@NotNull`, `@Positive` |

---

## Структура проекта
```
wallet-app/
├── .mvn/
│ └── wrapper/ # Maven Wrapper
├── src/
│ ├── main/
│ │ ├── java/
│ │ │ └── com/zarema/wallet_app/
│ │ │ ├── controller/
│ │ │ │ ├── WalletController.java # REST эндпоинты
│ │ │ │ └── GlobalExceptionHandler.java # Глобальный обработчик ошибок
│ │ │ ├── dto/
│ │ │ │ ├── WalletRequest.java # DTO для операций
│ │ │ │ ├── OperationType.java # Enum (DEPOSIT, WITHDRAW)
│ │ │ │ └── ApiError.java # Единый формат ошибки
│ │ │ ├── exception/
│ │ │ │ ├── WalletNotFoundException.java
│ │ │ │ └── InsufficientFundsException.java
│ │ │ ├── model/
│ │ │ │ └── Wallet.java # JPA сущность
│ │ │ ├── repository/
│ │ │ │ └── WalletRepository.java # Репозиторий с блокировкой
│ │ │ ├── service/
│ │ │ │ └── WalletService.java # Бизнес-логика
│ │ │ └── WalletAppApplication.java # Главный класс
│ │ └── resources/
│ │ ├── db/
│ │ │ └── changelog/
│ │ │ └── db.changelog-master.xml # Liquibase миграции
│ │ ├── application.yml # Основная конфигурация
│ │ ├── application-dev.yml # Dev окружение
│ │ └── application-test.yml # Test окружение
│ └── test/
│ └── java/
│ └── com/zarema/wallet_app/
│ ├── WalletAppApplicationTests.java # Интеграционные тесты
│ ├── WalletControllerTest.java # Тесты контроллера
│ └── WalletServiceTest.java # Тесты сервиса
├── .env # Переменные окружения
├── Dockerfile # Сборка Docker-образа
├── docker-compose.yaml # Запуск приложения и БД
├── mvnw # Maven Wrapper (Linux/Mac)
├── mvnw.cmd # Maven Wrapper (Windows)
└── pom.xml # Зависимости и сборка
 ```
---

## Запуск проекта

### Предварительные требования

- **Docker** и **Docker Compose**
- **Java 17** (для локального запуска)

### Быстрый запуск через Docker
1. **Клонируйте репозиторий:**
 ```bash
   git clone https://github.com/Zaremau/wallet-app.git
   cd wallet-app
```
3. **Запустите приложение с базой данных:**
```
bash
docker-compose up -d
```
4. **Проверьте статус:**
```
bash
docker-compose ps
```
5. **Доступ к сервисам:**

- Приложение: http://localhost:8087
- Swagger UI: http://localhost:8087/swagger-ui.html

## API Документация
### Полная документация API доступна через Swagger UI после запуска приложения:

- Swagger UI: http://localhost:8087/swagger-ui.html
- OpenAPI JSON: http://localhost:8087/v3/api-docs

### Эндпоинты
### Проведение транзакции
```
POST /api/v1/wallet
```
Тело запроса:
```
json
{
  "walletId": "550e8400-e29b-41d4-a716-446655440000",
  "operationType": "DEPOSIT",
  "amount": 100.00
}
```
### Возможные ответы:

| Код | Описание |
|-----|----------|
| `200` | Операция успешна |
| `404` | Кошелёк не найден |
| `400` | Ошибка валидации или нехватка средств |
| `409` | Ресурс заблокирован (конкурентный доступ) |

### Пример ошибки:
```
json
{
  "code": "INSUFFICIENT_FUNDS",
  "message": "Insufficient funds on wallet 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-05-14T12:00:00Z"
}
```
### Получение баланса:
```
GET /api/v1/wallets/{id}
```
### Пример ответа:
```
150.00
```
### Возможные ответы

| Код | Описание |
|-----|----------|
| `200` | Баланс успешно получен |
| `404` | Кошелёк не найден |

---

## Обработка ошибок

Все ошибки возвращаются в едином формате `ApiError`:

| Поле | Тип | Описание |
|------|-----|----------|
| `code` | `String` | Уникальный код ошибки |
| `message` | `String` | Человекочитаемое описание |
| `timestamp` | `LocalDateTime` | Время возникновения ошибки |
| `errors` | `Map<String, String>` | Ошибки по полям (для валидации) |

### Коды ошибок

| Код | HTTP статус | Описание |
|-----|-------------|----------|
| `WALLET_NOT_FOUND` | `404` | Кошелёк с указанным ID не существует |
| `INSUFFICIENT_FUNDS` | `400` | Недостаточно средств для списания |
| `INVALID_ARGUMENTS` | `400` | Ошибка валидации полей запроса |
| `INVALID_JSON_SYNTAX` | `400` | Некорректный JSON |
| `RESOURCE_LOCKED` | `409` | Кошелёк заблокирован другим запросом |
| `INTERNAL_SERVER_ERROR` | `500` | Внутренняя ошибка сервера |

---

## Конкурентная работа

Для обеспечения корректной работы при конкурентных запросах реализована **пессимистическая блокировка** на уровне базы данных.

### Как это работает:

1. При выполнении операции `DEPOSIT` или `WITHDRAW` вызывается метод `findByIdForUpdate(UUID id)`
2. Метод использует `@Lock(LockModeType.PESSIMISTIC_WRITE)` — это блокирует строку в БД для других транзакций
3. Установлен таймаут блокировки: **5000 мс** (5 секунд)
4. Если за 5 секунд блокировка не получена — выбрасывается `PessimisticLockingFailureException`
5. Клиент получает ответ `409 Conflict` с кодом `RESOURCE_LOCKED`

### SQL, который выполняется:

```sql
SELECT * FROM wallet WHERE id = ? FOR UPDATE
```
Это гарантирует, что при 1000 одновременных запросов на списание баланс будет корректным.

## Тестирование

Проект содержит три уровня тестов:

### 1. Модульные тесты сервиса (`WalletServiceTest`)

- Тестируют бизнес-логику в изоляции
- Используют Mockito для мокирования репозитория
- Покрывают все сценарии: пополнение, списание, недостаток средств, кошелёк не найден

```bash
./mvnw test -Dtest=WalletServiceTest
```
### 2. Тесты контроллера (`WalletControllerTest`)

- Тестируют слой контроллера через `MockMvc`
- Проверяют обработку ошибок (404, 409)
- Используют `@WebMvcTest` для изоляции веб-слоя

```bash
./mvnw test -Dtest=WalletControllerTest
```

### 3. Интеграционные тесты (`WalletAppApplicationTests`)

- Поднимают реальную PostgreSQL через **Testcontainers**
- Тестируют полный цикл: от HTTP-запроса до базы данных
- Включают тест на **конкурентные операции** (1000 запросов в 50 потоков)

```bash
./mvnw test -Dtest=WalletAppApplicationTests
```
### Запуск всех тестов

```bash
./mvnw test
```
## Конфигурация

Приложение поддерживает разделение конфигураций по окружениям:

| Файл | Окружение |
|------|-----------|
| `application.yml` | Базовая конфигурация |
| `application-dev.yml` | Разработка |
| `application-test.yml` | Тестирование |

### Основные параметры конфигурации

```yaml
server:
  port: 8087

spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/wallet}
    username: ${SPRING_DATASOURCE_USERNAME:user}
    password: ${SPRING_DATASOURCE_PASSWORD:password}
    hikari:
      maximum-pool-size: ${DB_POOL_SIZE:10}
  liquibase:
    enabled: true
```

Все параметры можно переопределить через переменные окружения в файле .env.

## Миграции базы данных

Для управления схемой базы данных используется **Liquibase**. Миграции находятся в:
```
src/main/resources/db/changelog/db.changelog-master.xml
src/main/resources/db/changelog/01-create-wallet-table.xml
src/main/resources/db/changelog/02-seed-wallets.xml
```

При запуске приложения миграции применяются автоматически.
