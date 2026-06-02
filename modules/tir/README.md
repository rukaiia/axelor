

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

**Гарантия KG — должен вернуть EPD028:**

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

**Гарантия XX — должен вернуть EPD016 (отказ):**

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

**Нет HolderNumber — должен вернуть SOAP Fault:**

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

**EPD028:**

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

---

### 3. История сообщений

В браузере: **МДП (TIR) → История сообщений**

Логи пишутся в `logs/tir-exchange.log`