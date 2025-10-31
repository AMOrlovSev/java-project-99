### Hexlet tests and linter status:
[![Actions Status](https://github.com/AMOrlovSev/java-project-99/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/AMOrlovSev/java-project-99/actions)

### GitHub Actions:
[![Java CI](https://github.com/AMOrlovSev/java-project-99/actions/workflows/JavaCI.yml/badge.svg)](https://github.com/AMOrlovSev/java-project-99/actions/workflows/JavaCI.yml)

### SonarQube:
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=AMOrlovSev_java-project-99&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=AMOrlovSev_java-project-99)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=AMOrlovSev_java-project-99&metric=coverage)](https://sonarcloud.io/summary/new_code?id=AMOrlovSev_java-project-99)

# Task Manager API

REST API для управления задачами с поддержкой пользователей, статусов и меток.

## 🚀 Функциональность

- **Аутентификация** - JWT-based аутентификация
- **Пользователи** - Регистрация и управление пользователями
- **Статусы задач** - Управление статусами (draft, to_review, published и др.)
- **Метки** - Категоризация задач
- **Задачи** - Полнофункциональное управление задачами с фильтрацией
- **Авторизация** - Ролевая модель (ADMIN/USER)

## 🛠 Технологии

- **Java 17** + **Spring Boot 3**
- **Spring Security** + JWT
- **PostgreSQL** + **Hibernate**
- **MapStruct** - маппинг DTO
- **Lombok** - сокращение boilerplate кода
- **Swagger/OpenAPI 3** - документация API
- **Sentry** - мониторинг ошибок
- **DataFaker** - генерация тестовых данных

## 📦 Deploy

Приложение развернуто на Render:

- **Production**: https://java-project-99-bntq.onrender.com

## 📚 API Документация

После запуска приложения документация доступна по адресу:
- Swagger UI: `https://java-project-99-bntq.onrender.com/swagger-ui/index.html`

## 🔐 Аутентификация

Используйте endpoint `/api/login` для получения JWT токена:

```json
{
  "username": "user@example.com",
  "password": "password"
}
```

Добавьте токен в заголовки запросов:

```text
Authorization: Bearer <your_jwt_token>
```

## 📋 Основные Endpoints

- `POST /api/login` - аутентификация
- `GET /api/tasks` - список задач (с фильтрацией)
- `POST /api/users` - регистрация пользователя
- `GET /api/task_statuses` - список статусов
- `GET /api/labels` - список меток

## 🎯 Особенности

- Пагинация и фильтрация задач
- Валидация входных данных
- Обработка ошибок с Sentry
- CORS настройки
- Автоматическая генерация тестовых данных
- RSA ключи для JWT

## 🔧 Конфигурация

Основные настройки в `application.yml`:

- Настройки базы данных
- RSA ключи для JWT
- Данные администратора
- Sentry DSN