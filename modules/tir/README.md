

## Запуск

```
.\gradlew run
```

Открыть в браузере: `http://localhost:8080/axelor-erp`

---

## Как тестировать через Postman

### 1. Войти в систему

```
POST http://localhost:8080/axelor-erp/callback
Content-Type: application/json
```

```json
{
  "username": "admin",
  "password": "admin"
}
```

После этого Postman сохранит куки — остальные запросы отправлять в том же окне.

---

### 2. Отправить TIR сообщение

Все запросы идут сюда:

```
POST http://localhost:8080/axelor-erp/ws/action
Content-Type: application/json
```

# TIR Exchange — модуль для Axelor ERP

Модуль имитирует обмен сообщениями МДП (TIR-EPD) между таможней и IRU.

## Запуск

```
.\gradlew run
```

Открыть в браузере: `http://localhost:8080/axelor-erp`

---
---
Для тестирования вручную как клиент:



**EPD015 — одобрение:**
- Тип: EPD015
- Номер гарантии: KG12345678
- Референс IRU: IRU-2025-001
- Ожидаем: EPD028 ACCEPTED

**EPD015 — отказ:**
- Тип: EPD015
- Номер гарантии: XX99999999
- Референс IRU: IRU-2025-002
- Ожидаем: EPD016 REJECTED

**EPD028 — разрешение транзита:**
- Тип: EPD028
- Номер гарантии: KG12345678
- Таможенный индекс: CI-KG12345678-755
- Ожидаем: EPD029 TRANSIT_ALLOWED

**EPD045 — завершение:**
- Тип: EPD045
- Номер гарантии: KG12345678
- Ожидаем: EPD045_ACK PROCEDURE_COMPLETED

**EPD051 — отказ в транзите:**
- Тип: EPD051
- Номер гарантии: KG12345678
- Ожидаем: EPD051_ACK TRANSIT_DENIED_ACKNOWLEDGED

**Неверный формат — ошибка валидации:**
- Тип: EPD015
- Номер гарантии: чч12345678
- Ожидаем: ошибка "Неверный формат номера гарантии"


## Тестирование через интерфейс

Открыть в браузере **МДП (TIR) → Отправить сообщение**.

Вставить XML в поле и нажать кнопку **Отправить**. Ответ появится в поле ниже.

**Гарантия KG — вернёт EPD028:**
```
<EPD015><GuaranteeNumber>KG12345678</GuaranteeNumber><IruReference>IRU-2025-001</IruReference><HolderNumber>TIRH-998877</HolderNumber></EPD015>
```

**Гарантия XX — вернёт EPD016 (отказ):**
```
<EPD015><GuaranteeNumber>XX99999999</GuaranteeNumber><IruReference>IRU-2025-002</IruReference><HolderNumber>TIRH-998877</HolderNumber></EPD015>
```

**Нет HolderNumber — вернёт SOAP Fault:**
```
<EPD015><GuaranteeNumber>KG12345678</GuaranteeNumber><IruReference>IRU-2025-003</IruReference></EPD015>
```

**EPD028 → вернёт EPD029:**
```
<EPD028><GuaranteeNumber>KG12345678</GuaranteeNumber><CustomsIndex>CI-KG12345678-755</CustomsIndex></EPD028>
```

**EPD045:**
```
<EPD045><GuaranteeNumber>KG12345678</GuaranteeNumber></EPD045>
```

**EPD051:**
```
<EPD051><GuaranteeNumber>KG12345678</GuaranteeNumber></EPD051>
```

История обработанных сообщений: **МДП (TIR) → История сообщений**

---

## Тестирование через Postman

### 1. Войти в систему

```
POST http://localhost:8080/axelor-erp/callback
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}
```

После этого Postman сохранит куки — остальные запросы отправлять в том же окне.

### 2. Отправить TIR сообщение

Все запросы идут сюда:

```
POST http://localhost:8080/axelor-erp/ws/action
Content-Type: application/json
```

**Гарантия KG → EPD028:**
```json
{
  "action": "com.example.tir.web.TirExchangeController:exchange",
  "model": "com.example.tir.db.TirMessage",
  "data": {
    "context": {
      "xmlPayload": "<EPD015><GuaranteeNumber>KG12345678</GuaranteeNumber><IruReference>IRU-2025-001</IruReference><HolderNumber>TIRH-998877</HolderNumber></EPD015>"
    }
  }
}
```

**Гарантия XX → EPD016:**
```json
{
  "action": "com.example.tir.web.TirExchangeController:exchange",
  "model": "com.example.tir.db.TirMessage",
  "data": {
    "context": {
      "xmlPayload": "<EPD015><GuaranteeNumber>XX99999999</GuaranteeNumber><IruReference>IRU-2025-002</IruReference><HolderNumber>TIRH-998877</HolderNumber></EPD015>"
    }
  }
}
```

**Нет HolderNumber → SOAP Fault:**
```json
{
  "action": "com.example.tir.web.TirExchangeController:exchange",
  "model": "com.example.tir.db.TirMessage",
  "data": {
    "context": {
      "xmlPayload": "<EPD015><GuaranteeNumber>KG12345678</GuaranteeNumber><IruReference>IRU-2025-003</IruReference></EPD015>"
    }
  }
}
```

**EPD028 → EPD029:**
```json
{
  "action": "com.example.tir.web.TirExchangeController:exchange",
  "model": "com.example.tir.db.TirMessage",
  "data": {
    "context": {
      "xmlPayload": "<EPD028><GuaranteeNumber>KG12345678</GuaranteeNumber><CustomsIndex>CI-KG12345678-755</CustomsIndex></EPD028>"
    }
  }
}
```

**EPD045:**
```json
{
  "action": "com.example.tir.web.TirExchangeController:exchange",
  "model": "com.example.tir.db.TirMessage",
  "data": {
    "context": {
      "xmlPayload": "<EPD045><GuaranteeNumber>KG12345678</GuaranteeNumber></EPD045>"
    }
  }
}
```

**EPD051:**
```json
{
  "action": "com.example.tir.web.TirExchangeController:exchange",
  "model": "com.example.tir.db.TirMessage",
  "data": {
    "context": {
      "xmlPayload": "<EPD051><GuaranteeNumber>KG12345678</GuaranteeNumber></EPD051>"
    }
  }
}
```

### 3. История сообщений

В браузере: **МДП (TIR) → История сообщений**

Логи пишутся в `logs/tir-exchange.log`