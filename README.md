# Task Manager







**Task Manager** — REST-приложение для управления задачами, разработанное в рамках финального проекта JavaRush.

## 📌 О проекте

Приложение предназначено для создания, редактирования, удаления и отслеживания задач.

В проекте используются Spring Boot, Spring Data JPA, Spring Security и PostgreSQL. Разработка ведётся поэтапно с использованием Git и Pull Request.

## 🛠 Технологический стек

| Технология           | Назначение                 |
| -------------------- | -------------------------- |
| Java 17              | Основной язык              |
| Spring Boot 4        | Основа приложения          |
| Spring WebMVC        | REST API                   |
| Spring Data JPA      | Работа с базой данных      |
| Spring Security      | Защита приложения          |
| JWT                  | Авторизация пользователей  |
| PostgreSQL 18        | Хранение данных            |
| Hibernate            | ORM                        |
| Maven                | Сборка проекта             |
| Docker Compose       | Запуск инфраструктуры      |
| Spring Boot Actuator | Мониторинг                 |
| Lombok               | Сокращение шаблонного кода |
| JUnit                | Тестирование               |

## 📂 Структура проекта

```text
task-manager/
│
├── src/
│   ├── main/
│   │   ├── java/com/javarush/taskmanager/
│   │   │   ├── config/          # Конфигурация приложения
│   │   │   ├── controller/      # REST-контроллеры
│   │   │   ├── dto/             # Объекты передачи данных
│   │   │   ├── entity/          # Сущности базы данных
│   │   │   ├── exception/       # Обработка исключений
│   │   │   ├── repository/      # Доступ к базе данных
│   │   │   ├── security/        # Безопасность и JWT
│   │   │   ├── service/         # Бизнес-логика
│   │   │   └── TaskManagerApplication.java
│   │   │
│   │   └── resources/
│   │       └── application.yaml # Настройки приложения
│   │
│   └── test/
│       └── java/com/javarush/taskmanager/
│           └──                 # Автоматические тесты
│
├── docker-compose.yml           # Контейнеры PostgreSQL
├── pom.xml                      # Зависимости Maven
├── mvnw                         # Maven Wrapper
├── mvnw.cmd
└── README.md
```

## 🗃 Статусы задач

На текущем этапе определены следующие статусы:

| Статус        | Описание           |
| ------------- | ------------------ |
| `NEW`         | Новая задача       |
| `IN_PROGRESS` | Задача в работе    |
| `COMPLETED`   | Выполненная задача |
| `CANCELLED`   | Отменённая задача  |

## 🚀 Запуск проекта

Для запуска потребуются:

- JDK 17
- Maven
- Docker Desktop

Запуск базы данных:

```bash
docker compose up -d
```

Запуск приложения через Maven:

```bash
./mvnw spring-boot:run
```

В Windows:

```bash
mvnw.cmd spring-boot:run
```

Приложение использует порт `8081`.

