# VseSvoiSearchBot
*В процессе разработки*
Телеграм бот для поиска специалистов и отклика специалистов на запросы. 
Стек: Spring Boot, Maven, Telegram API, PostgreSQL, Spring Data Jpa
Принцип работы: Задает вопросы пользователю для составления структурированного запроса 
для поиска нужного специалиста. Затем отправляет запрос в закрытый чат специалистов. Специалисты могут откликнуться
на запрос через бот. 
Дает преимущества:
1) При составлении запроса пользователь не забудет указать важную информацию
2) Специалисты могут давать контакт бота своим клиентам, вместо того, чтобы искать спеца самим
3) Анонимность для пользователя, который ищет специалиста
4) Возможность получать все отклики в одном месте, меньше шансов их пропустить
5) Сохраняется закрытость чата специалистов для посторонних

Сейчас умеет: 
- приветствовать пользователя (реализовано в Bot_Father)
- выдавать ошибку, если не распознал команду
- Переходить в режим последовательного формирования запроса
- Сохранять пользователя в базе данных
- Сохранение запроса в БД
- Выводить подробное описание по команде /help
- Откат при формировании запроса
- Отправка сформированного запроса в чат специалистов (на данный момент используется тестовый чат)
- Возможность отвечать на запрос
- Получение ответов на запрос в чате бота

  Будет реализовано:
- Прекращение получения откликов
- Меню команд (реализовано в Bot_Father)
- Возможность формирования нескольких запросов, управление ими
- Клавиатура для навигации
- Переход на WebHook
- Контейнеризация и запуск на сервере
  
В перспективе:
- Добавление психологических тестов с возможностью отправить результаты своему врачу/психотерапевту
- Подключение других чатов специалистов
- Реализация защиты от спама
- Шифрование данных

Оптимизация:
~~- Уменьшение нагрузки на БД (Ленивая загрузка)~~
- Рефакторинг
- Логирование



