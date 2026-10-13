# Inference-сервис

Сервис инференса для Cogniflex.

## Структура

- `app/` - runtime-код сервиса.
- `app/detectors/` - обертки над моделями, которые используются gRPC-обработчиками.
- `app/inference/` - вспомогательные функции runtime-предобработки.
- `weights/` - локальные веса моделей для runtime. Крупные файлы весов не коммитятся.
- `config.yaml` - конфигурация сервиса, включая относительные пути к весам.
- `generated/` - сгенерированный Python-код gRPC, не коммитится.

Сервис использует protobuf-контракты из корневой директории `proto/`.
Он не должен импортировать код из `model/training`, потому что эта часть
предназначена для исследований и обучения моделей.

## Запуск

Нужны [uv](https://docs.astral.sh/uv/) и [just](https://just.systems/). Python нужной
версии (`.python-version`) uv скачает сам.

```bash
just sync                    # установить зависимости в .venv
just proto                   # сгенерировать gRPC-код в generated/ (main.py делает это и сам)
just run                     # запустить gRPC-сервер на :50051
just test-text "some text"   # отправить текст запущенному серверу
just test-photo path/to.jpg  # отправить изображение
just docker                  # собрать и запустить сервис в Docker
just                         # список всех команд
```

Перед запуском положите веса из `config.yaml` в `weights/`, иначе сервер не стартует.

Зависимости описаны в `pyproject.toml` и зафиксированы в `uv.lock`:

```bash
uv add <package>    # добавить зависимость
just upgrade        # обновить все зависимости в uv.lock
```
