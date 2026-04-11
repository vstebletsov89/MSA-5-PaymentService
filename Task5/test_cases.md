# Task5: Таблица интеграционных и end-to-end тестов для OrchestrPay

## Подход к тестированию

Для платёжного процесса OrchestrPay используется следующая стратегия:

- **интеграционные тесты** покрывают основную часть сценариев, ошибок и компенсаций;
- **end-to-end тесты** покрывают только критичные сквозные бизнес-сценарии;
- отказоустойчивость, SLA, observability и архитектурные ограничения проверяются отдельно.

---

## Технологический стек тестирования

- **JUnit 5** — базовый тестовый фреймворк
- **Testcontainers** — запуск PostgreSQL, Redis, Zeebe и других зависимостей в изолированном окружении
- **Camunda/Zeebe Test** — тестирование BPMN-процесса, timer boundary events, message correlation и job workers
- **Mockito / WireMock** — моки внешних сервисов и интеграций
- **ArchUnit** — проверка архитектурных ограничений
- **Docker Compose** — запуск полного окружения для E2E-проверок

---

## Интеграционные тесты

| Название                                                                              | Тип | Компоненты | Предусловия |
|---------------------------------------------------------------------------------------|-----|------------|-------------|
| Создание платежа и запуск процесса                                                    | Интеграционный | Payment Orchestrator, Zeebe, PostgreSQL | Zeebe и PostgreSQL запущены через Testcontainers, BPMN-процесс задеплоен |
| Успешное списание/резервирование средств клиента                                      | Интеграционный | Payment Orchestrator, worker `reserve-funds`, Zeebe | Процесс запущен, платёж создан |
| Ошибка списания средств приводит к отклонению процесса                                | Интеграционный | Payment Orchestrator, worker `reserve-funds`, Zeebe | Worker списания возвращает ошибку |
| Antifraud обрабатывает все основные решения (`APPROVED`, `REJECTED`, `MANUAL_REVIEW`) | Интеграционный | Payment Orchestrator, worker `fraud-check`, Zeebe | Средства успешно списаны, antifraud возвращает разные решения |
| Ручная проверка: оператор принимает решение                                           | Интеграционный | Payment Orchestrator, Zeebe, message correlation | Процесс находится на `Task_ManualReview`, отправляется сообщение |
| Срабатывание cut-off при отсутствии ручного решения                                   | Интеграционный | Payment Orchestrator, Zeebe, timer boundary event | Процесс находится в ожидании ручной проверки |
| Ошибка перевода контрагенту переводит процесс в compensation flow                     | Интеграционный | Payment Orchestrator, worker `transfer-to-merchant`, Zeebe | Transfer worker возвращает ошибку |
| Компенсационный сценарий после pivot выполняется корректно (reverse -> refund)        | Интеграционный | Payment Orchestrator, workers `reverse-merchant-transfer`, `refund-customer`, Zeebe | Процесс находится в compensation flow |
| Возврат средств клиенту после reject-ветки                                            | Интеграционный | Payment Orchestrator, worker `refund-customer`, Zeebe | Antifraud/manual review завершились отказом |
| Отправка уведомлений в success и reject сценариях                                     | Интеграционный | Payment Orchestrator, workers `notify-success`, `notify-rejection`, Zeebe | Процесс завершает success или reject flow |


## End-to-End тесты

| Название | Тип | Компоненты | Предусловия |
|----------|-----|------------|-------------|
| Полный успешный сценарий платежа (happy path) | E2E | Zeebe, Payment Orchestrator, PostgreSQL, Redis, все worker'ы | Полный docker-compose стек запущен, BPMN задеплоен, antifraud возвращает `APPROVED`, перевод контрагенту успешен |
| Отклонение antifraud -> автоматический возврат средств | E2E | Zeebe, Payment Orchestrator, PostgreSQL, все worker'ы | Полный стек запущен, antifraud возвращает `REJECTED` |
| Ручная проверка с подтверждением оператором | E2E | Zeebe, Payment Orchestrator, PostgreSQL, все worker'ы | Полный стек запущен, antifraud возвращает `MANUAL_REVIEW`, затем отправляется сообщение ручного подтверждения |
| Ручная проверка с отказом оператора | E2E | Zeebe, Payment Orchestrator, PostgreSQL, все worker'ы | Полный стек запущен, antifraud возвращает `MANUAL_REVIEW`, затем отправляется сообщение ручного отказа |
| Cut-off-time при отсутствии ответа оператора | E2E | Zeebe, Payment Orchestrator, PostgreSQL, все worker'ы | Полный стек запущен, antifraud возвращает `MANUAL_REVIEW`, ручное решение не приходит до истечения таймера |
| Компенсация после ошибки после pivot-точки | E2E | Zeebe, Payment Orchestrator, PostgreSQL, все worker'ы | Полный стек запущен, antifraud возвращает `APPROVED`, перевод контрагенту выполнен, затем эмулируется post-pivot failure |

---