# VseSvoiSearchBot
*В процессе разработки*
Телеграм бот для поиска специалистов и отклика специалистов на запросы. 
Стек: Spring Boot, Maven, Telegram API, PostgreSQL, Spring Data Jpa

Сейчас умеет: 
- приветствовать пользователя (команда /start)
- выдавать ошибку, если не распознал команду
- Переходить в режим последовательного формирования запроса
- Сохранять пользователя в базе данных
- - Сохранение запроса в БД

Будет реализовано:
- Отправка сформированного запроса в чат специалистов
- Возможность отвечать на запрос
- Получение ответов на запрос в чате бота
- Возможность формирования нескольких запросов, управление ими
- Клавиатура для навигации
- Переход на WebHook
- Контейнеризация и запуск на сервере
  
В перспективе:
- Добавление психологических тестов с возможностью отправить результаты своему врачу/психотерапевту
- Подключение других чатов специалистов
- Реализация защиты от спама

