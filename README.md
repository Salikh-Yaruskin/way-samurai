# Demo Project Notes

## Thymeleaf Guide

Этот раздел описывает правила написания Thymeleaf-шаблонов в проекте. Его можно дополнять по мере появления новых соглашений.

### Где лежат шаблоны

Все HTML-шаблоны лежат в:

```text
src/main/resources/templates
```

Примеры:

```text
templates/index.html
templates/auth/login.html
templates/auth/register.html
templates/media/index.html
templates/fragments/header.html
```

Статические файлы, если они понадобятся, кладутся в:

```text
src/main/resources/static
```

Например:

```text
static/css/style.css
static/js/app.js
```

### Базовая структура страницы

Минимальный шаблон:

```html
<!DOCTYPE html>
<html lang="ru" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Название страницы</title>
</head>
<body>

<div th:replace="~{fragments/header :: header}"></div>

<main>
    <h1>Заголовок</h1>
</main>

</body>
</html>
```

Если на странице нужны проверки авторизации через `sec:*`, добавляй namespace:

```html
<html lang="ru"
      xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
```

### Фрагменты

Общие части страницы выносим в `templates/fragments`.

Пример фрагмента:

```html
<header th:fragment="header"
        xmlns:th="http://www.thymeleaf.org"
        xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
    <a th:href="@{/}">Главная</a>
</header>
```

Подключение фрагмента:

```html
<div th:replace="~{fragments/header :: header}"></div>
```

Правило: фрагмент не должен быть полноценным HTML-документом. В нем не нужны `<!DOCTYPE html>`, `<html>`, `<head>`, `<body>`.

### Ссылки

Внутренние ссылки пишем через `th:href`, а не обычный `href`:

```html
<a th:href="@{/login}">Войти</a>
<a th:href="@{/media/{id}(id=${file.id})}">Открыть файл</a>
```

Так Thymeleaf корректно соберет URL с учетом контекста приложения.

### Формы

Обычная форма:

```html
<form th:action="@{/login}" method="post">
    <p>
        <label for="username">Имя пользователя</label><br>
        <input type="text" id="username" name="username" required>
    </p>

    <button type="submit">Отправить</button>
</form>
```

Форма с объектом модели:

```html
<form th:action="@{/register}" th:object="${request}" method="post">
    <p>
        <label for="username">Имя пользователя</label><br>
        <input type="text" id="username" th:field="*{username}" required>
    </p>
</form>
```

Правило:

```text
name="username" используем для простых форм.
th:field="*{username}" используем, когда есть th:object.
```

### Загрузка файлов

Для загрузки файлов обязательно указывать:

```html
enctype="multipart/form-data"
```

Пример:

```html
<form th:action="@{/media}" method="post" enctype="multipart/form-data">
    <p>
        <label for="file">Файл</label><br>
        <input id="file" name="file" type="file" required>
    </p>

    <button type="submit">Загрузить</button>
</form>
```

Если забыть `enctype`, файл не попадет в `MultipartFile` на сервере.

### Ошибки валидации

Для формы с `th:object` ошибки поля выводим так:

```html
<p th:if="${#fields.hasErrors('username')}" th:errors="*{username}">
    Ошибка ввода имени
</p>
```

Полный пример:

```html
<form th:action="@{/register}" th:object="${request}" method="post">
    <p>
        <label for="username">Имя пользователя</label><br>
        <input type="text" id="username" th:field="*{username}" required>
    </p>
    <p th:if="${#fields.hasErrors('username')}" th:errors="*{username}">
        Ошибка ввода имени
    </p>
</form>
```

### Условия

Показать блок только при условии:

```html
<p th:if="${param.error}">
    Неверный логин или пароль.
</p>
```

Показать элемент только авторизованному пользователю:

```html
<a sec:authorize="isAuthenticated()" th:href="@{/profile}">Профиль</a>
```

Показать элемент только администратору:

```html
<a sec:authorize="hasRole('ADMIN')" th:href="@{/admin}">Админка</a>
```

### Циклы

Список элементов выводим через `th:each`:

```html
<article th:each="file : ${files}">
    <h3 th:text="${file.originalFilename}">filename.jpg</h3>
</article>
```

Проверка пустого списка:

```html
<p th:if="${#lists.isEmpty(files)}">
    Файлы еще не загружены.
</p>
```

Проверка непустого списка:

```html
<div th:if="${!#lists.isEmpty(files)}">
    ...
</div>
```

### Вывод текста

Текст из модели выводим через `th:text`:

```html
<span th:text="${file.contentType}">image/jpeg</span>
```

Текст внутри тега нужен как fallback, чтобы страницу было проще читать без запуска приложения.

### CSRF

Если CSRF включен, в POST-формах нужен токен:

```html
<input type="hidden"
       th:if="${_csrf != null}"
       th:name="${_csrf.parameterName}"
       th:value="${_csrf.token}">
```

Сейчас в проекте CSRF отключен в `SecurityConfig`, но скрытое поле можно оставлять. Оно не мешает, если `_csrf == null`.

### Правила проекта

Текущие соглашения:

```text
1. Пишем страницы максимально просто: обычный HTML + Thymeleaf.
2. Не используем Bootstrap.
3. Не используем CSS, если задача прямо не требует стилизации.
4. Общую навигацию держим во fragments/header.html.
5. Для ссылок используем th:href.
6. Для форм используем th:action.
7. Для форм с объектом используем th:object и th:field.
8. Для файловых форм всегда пишем enctype="multipart/form-data".
9. Для повторяющихся блоков используем th:each.
10. Для условий используем th:if.
```

### Место для будущих правил

Дописывай сюда новые соглашения:

```text
- ...
```
