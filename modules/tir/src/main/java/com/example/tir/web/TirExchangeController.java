package com.example.tir.web;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.example.tir.service.TirExchangeService;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class TirExchangeController {

    private static final Logger log = LoggerFactory.getLogger(TirExchangeController.class);

    @Inject
    private TirExchangeService tirExchangeService;

    public void exchange(ActionRequest request, ActionResponse response) {
        log.info("TIR exchange запрос");
        try {
            String xmlPayload = (String) request.getContext().get("xmlPayload");
            if (xmlPayload == null || xmlPayload.isEmpty()) {
                response.setError("XML payload не может быть пустым");
                return;
            }
            String result = tirExchangeService.exchange(xmlPayload);
            response.setValue("xmlResponse", result);
            response.setInfo("Сообщение обработано");
        } catch (Exception e) {
            log.error("Ошибка", e);
            response.setError("Ошибка: " + e.getMessage());
        }
    }

    public void exchangeFromUi(ActionRequest request, ActionResponse response) {
        log.info("TIR exchange из UI");
        try {
            String xmlPayload = (String) request.getContext().get("payload");
            if (xmlPayload == null || xmlPayload.isEmpty()) {
                response.setError("Введите XML сообщение");
                return;
            }
            String result = tirExchangeService.exchange(xmlPayload);
            response.setValue("response", result);
            response.setInfo("Сообщение обработано");
        } catch (Exception e) {
            log.error("Ошибка", e);
            response.setError("Ошибка: " + e.getMessage());
        }
    }
    public void sendSimple(ActionRequest request, ActionResponse response) {
        log.info("TIR простая отправка из UI");
        try {
            String messageType = (String) request.getContext().get("messageType");
            String guaranteeNumber = (String) request.getContext().get("guaranteeNumber");
            String iruReference = (String) request.getContext().get("iruReference");
            String customsIndex = (String) request.getContext().get("customsIndex");

            if (messageType == null || messageType.isEmpty()) {
                response.setError("Выберите тип сообщения");
                return;
            }
            if (guaranteeNumber == null || guaranteeNumber.isEmpty()) {
                response.setError("Введите номер гарантии");
                return;
            }
            if (!guaranteeNumber.matches("^[A-Z]{2}\\d+$")) {
                response.setError("Неверный формат номера гарантии. Пример: KG12345678");
                return;
            }
            if (guaranteeNumber.startsWith("XX")) {
                String xml = "<EPD015>" +
                        "<GuaranteeNumber>" + guaranteeNumber + "</GuaranteeNumber>" +
                        "<IruReference>" + (iruReference != null ? iruReference : "") + "</IruReference>" +
                        "<HolderNumber>TIRH-000000</HolderNumber>" +
                        "</EPD015>";
                String result = tirExchangeService.exchange(xml);
                response.setValue("response", result);
                response.setInfo("Сообщение обработано");
                return;
            }

            String xml;
            if ("EPD015".equals(messageType)) {
                xml = "<EPD015>" +
                        "<GuaranteeNumber>" + guaranteeNumber + "</GuaranteeNumber>" +
                        "<IruReference>" + (iruReference != null ? iruReference : "") + "</IruReference>" +
                        "<HolderNumber>TIRH-000000</HolderNumber>" +
                        "</EPD015>";
            } else if ("EPD028".equals(messageType)) {
                if (customsIndex == null || customsIndex.isEmpty()) {
                    response.setError("Для EPD028 необходимо указать таможенный индекс");
                    return;
                }
                xml = "<EPD028>" +
                        "<GuaranteeNumber>" + guaranteeNumber + "</GuaranteeNumber>" +
                        "<CustomsIndex>" + customsIndex + "</CustomsIndex>" +
                        "</EPD028>";
            } else {
                xml = "<" + messageType + ">" +
                        "<GuaranteeNumber>" + guaranteeNumber + "</GuaranteeNumber>" +
                        "</" + messageType + ">";
            }
            String result = tirExchangeService.exchange(xml);
            response.setValue("response", result);
            response.setInfo("Сообщение обработано");

        } catch (IllegalArgumentException e) {
            log.warn("Ошибка валидации: {}", e.getMessage());
            response.setError("Ошибка валидации: " + e.getMessage());
        }
        catch (Exception e) {
            log.error("Ошибка при обработке сообщения", e);
            response.setError("Произошла ошибка при обработке. Проверьте данные и попробуйте снова.");
        }
    }
}