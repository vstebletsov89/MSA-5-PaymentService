Для описания процесса используются следующие состояния:

- `NEW` - платёж создан;
- `FUNDS_RESERVED` - средства клиента успешно списаны/зарезервированы;
- `FRAUD_CHECK_PENDING` - antifraud-проверка запущена, ожидается решение;
- `MANUAL_REVIEW_PENDING` - операция отправлена на ручную проверку;
- `FRAUD_APPROVED` - antifraud разрешил проведение операции;
- `FRAUD_REJECTED` - antifraud запретил проведение операции;
- `CUT_OFF_APPROVED` - операция автоматически разрешена по cut-off-time;
- `MERCHANT_TRANSFERRED` - средства переведены контрагенту;
- `REFUNDED` - средства возвращены клиенту;
- `COMPLETED` - платёж успешно завершён;
- `REJECTED` - платёж завершён отказом;
- `REVERSAL_COMPLETED` - выполнен реверс перевода контрагенту;
- `FAILED` - зафиксирован технический сбой, требующий компенсации.

## Таблица переходов

| Исходное состояние      | Переходное состояние | Событие                                |
|-------------------------|----------------------|----------------------------------------|
| -                       | `NEW` | `CREATE_PAYMENT_COMPLETED`             |
| `NEW`                   | `FUNDS_RESERVED` | `RESERVE_FUNDS_COMPLETED`              |
| `NEW`                   | `REJECTED` | `RESERVE_FUNDS_FAILED`                 |
| `FUNDS_RESERVED`        | `FRAUD_CHECK_PENDING` | `START_FRAUD_CHECK_COMPLETED`          |
| `FRAUD_CHECK_PENDING`   | `FRAUD_APPROVED` | `FRAUD_APPROVED_RECEIVED`              |
| `FRAUD_CHECK_PENDING`   | `FRAUD_REJECTED` | `FRAUD_REJECTED_RECEIVED`              |
| `FRAUD_CHECK_PENDING`   | `MANUAL_REVIEW_PENDING` | `MANUAL_REVIEW_REQUESTED`              |
| `FRAUD_CHECK_PENDING`   | `CUT_OFF_APPROVED` | `FRAUD_DECISION_TIMEOUT_REACHED`       |
| `MANUAL_REVIEW_PENDING` | `FRAUD_APPROVED` | `MANUAL_REVIEW_APPROVED`               |
| `MANUAL_REVIEW_PENDING` | `FRAUD_REJECTED` | `MANUAL_REVIEW_REJECTED`               |
| `MANUAL_REVIEW_PENDING` | `CUT_OFF_APPROVED` | `MANUAL_REVIEW_TIMEOUT_REACHED`        |
| `FRAUD_APPROVED`        | `MERCHANT_TRANSFERRED` | `TRANSFER_TO_MERCHANT_COMPLETED`       |
| `CUT_OFF_APPROVED`      | `MERCHANT_TRANSFERRED` | `TRANSFER_TO_MERCHANT_COMPLETED`       |
| `FRAUD_APPROVED`        | `REFUNDED` | `TRANSFER_TO_MERCHANT_FAILED`          |
| `CUT_OFF_APPROVED`      | `REFUNDED` | `TRANSFER_TO_MERCHANT_FAILED`          |
| `MERCHANT_TRANSFERRED`  | `COMPLETED` | `CONFIRM_PAYMENT_SUCCESS_COMPLETED`    |
| `FRAUD_REJECTED`        | `REFUNDED` | `REFUND_CUSTOMER_COMPLETED`            |
| `REFUNDED`              | `REJECTED` | `MARK_PAYMENT_REJECTED_COMPLETED`      |
| `REVERSAL_COMPLETED`    | `REFUNDED` | `REFUND_CUSTOMER_COMPLETED`            |
| `FUNDS_RESERVED`        | `REFUNDED` | `PAYMENT_CANCELLED`                    |
| `MERCHANT_TRANSFERRED`  | `FAILED` | `POST_TRANSFER_INCONSISTENCY_DETECTED` |
| `FAILED`                | `REVERSAL_COMPLETED` | `REVERSE_MERCHANT_TRANSFER_COMPLETED`  |


## Основные сценарии

### 1. Успешный сценарий
NEW
-> FUNDS_RESERVED
-> FRAUD_CHECK_PENDING
-> FRAUD_APPROVED
-> MERCHANT_TRANSFERRED
-> COMPLETED

Описание:

Платёж создаётся.
Средства клиента резервируются или списываются.
Запускается antifraud-проверка.
Если проверка разрешает операцию, деньги переводятся контрагенту.
После подтверждения платёж переходит в финальное состояние COMPLETED.

### 2. Отклонение antifraud
NEW
-> FUNDS_RESERVED
-> FRAUD_CHECK_PENDING
-> FRAUD_REJECTED
-> REFUNDED
-> REJECTED

Описание:

После списания средств запускается antifraud.
Если antifraud возвращает запрет, операция не проводится дальше.
Клиенту автоматически возвращаются деньги.
После возврата платёж переходит в финальное состояние REJECTED.

### 3. Ручная проверка
NEW
-> FUNDS_RESERVED
-> FRAUD_CHECK_PENDING
-> MANUAL_REVIEW_PENDING
-> FRAUD_APPROVED / FRAUD_REJECTED

Описание:

Antifraud не может сразу принять решение и переводит операцию на ручную проверку.
Пока оператор не примет решение, платёж остаётся в состоянии MANUAL_REVIEW_PENDING.
После ручной проверки возможны две ветки:
разрешение операции;
запрет операции с последующим возвратом клиенту.

### 4. Срабатывание cut-off-time

NEW
-> FUNDS_RESERVED
-> FRAUD_CHECK_PENDING
-> MANUAL_REVIEW_PENDING
-> CUT_OFF_APPROVED
-> MERCHANT_TRANSFERRED
-> COMPLETED

Описание:

Если antifraud или ручная проверка не дали ответа за установленное время,
срабатывает cut-off-time.
По условиям задачи операция считается разрешённой по умолчанию.
Далее выполняется перевод средств контрагенту и финализация платежа.

### 5. Сбой после перевода контрагенту

NEW
-> FUNDS_RESERVED
-> FRAUD_CHECK_PENDING
-> FRAUD_APPROVED
-> MERCHANT_TRANSFERRED
-> FAILED
-> REVERSAL_COMPLETED
-> REFUNDED
-> REJECTED

Описание:

Если после перевода денег контрагенту система обнаружила критический сбой или неконсистентность,
транзакция переводится в FAILED.
Затем выполняется реверс перевода контрагенту.
После этого клиенту возвращаются деньги.
Платёж завершается отказом.

В данной state machine терминальными считаются следующие состояния:

COMPLETED - платёж успешно проведён;
REJECTED - платёж отклонён, все компенсации завершены.

FAILED не является целевым терминальным бизнес-состоянием.
Оно отражает техническую проблему, после которой оркестратор должен автоматически запустить компенсационный сценарий.