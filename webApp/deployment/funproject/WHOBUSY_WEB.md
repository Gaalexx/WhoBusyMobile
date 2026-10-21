# Готовый Web-клиент WhoBusy

`static/` — результат production Wasm-сборки WhoBusy2. Его нужно хранить в Git
вместе с `Dockerfile` и `.dockerignore`. Каталог называется `static`, а не `dist`,
потому что `frontend/dist/` исключён из Git в FunProject.

## Выпуск версии

1. В WhoBusy2 выполните:

   ```sh
   ./gradlew :webApp:exportFunProjectFrontend -PwhoBusy.web.useMockData=false -PwhoBusy.web.apiBaseUrl=
   ```

2. Скопируйте содержимое полученной `webApp/build/deploy/frontend/` в существующий
   `FunProject/frontend/`, заменив `Dockerfile` и `.dockerignore`.
   **Объедините папки, не удаляйте существующий frontend целиком:** его
   `nginx.conf` и HTTPS-шаблон должны остаться. Исходники React можно сохранить;
   новый Dockerfile их не собирает и не включает в образ.
3. При следующих обновлениях заменяйте целиком только `frontend/static/`,
   чтобы Git фиксировал также удаление файлов предыдущей сборки.
4. Закоммитьте и отправьте изменения `frontend/` в репозиторий FunProject.
5. На сервере получите этот коммит и выполните обычный деплой:

   ```sh
   git pull
   docker compose up -d --build
   ```

Существующий `docker-compose.yml` уже собирает `./frontend`, менять его не нужно.
Compose override и отдельного deploy-скрипта нет. Docker только помещает готовую
статику в nginx; Kotlin, Gradle, Node.js и npm на сервере не нужны.

## Сеть и HTTPS

Dockerfile использует существующий `frontend/nginx.conf`. Маршрутизация `/api/`
в `backend:8000`, порты, ACME и сертификатные volumes остаются из FunProject.
HTTPS нужно настроить существующим способом из `HTTPS_SETUP.md`; пакет не
заменяет конфигурацию nginx и не выпускает сертификаты. Не ослабляйте HttpOnly,
Secure или SameSite refresh-cookie.

Сборка по умолчанию обращается к `/api/` текущего origin без demo-данных.
После деплоя проверьте вход, восстановление сессии после перезагрузки и выход.
Браузеру требуется доступный WebGL2. Wasm-файлы увеличивают размер Git-репозитория
при каждом выпуске; они намеренно коммитятся по этой схеме публикации.
