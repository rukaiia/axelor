package com.example.tir.processor;

import com.example.tir.service.XmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class Epd051Processor implements TirMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(Epd051Processor.class);

    @Override
    public String process(String xmlPayload) {
        log.info("[EPD051] Обработка сообщения");
        Document doc = XmlParser.parse(xmlPayload);

        String guaranteeNumber = XmlParser.getTagValue(doc, "GuaranteeNumber");
        if (guaranteeNumber == null || guaranteeNumber.isEmpty()) {
            return XmlParser.buildSoapFault("CLIENT_VALIDATION_ERROR",
                    "Отсутствует элемент GuaranteeNumber");
        }
        if (!guaranteeNumber.matches("^[A-Z]{2}\\d+$")) {
            return XmlParser.buildSoapFault("CLIENT_VALIDATION_ERROR",
                    "Неверный формат номера гарантии: " + guaranteeNumber);
        }
        if (guaranteeNumber == null || guaranteeNumber.isEmpty()) {
            return XmlParser.buildSoapFault("CLIENT_VALIDATION_ERROR",
                "Отсутствует элемент GuaranteeNumber");
        }
        log.info("[EPD051] Транзит отклонён для {}", guaranteeNumber);
        return "<EPD051_ACK>" +
               "<GuaranteeNumber>" + guaranteeNumber + "</GuaranteeNumber>" +
               "<Status>TRANSIT_DENIED_ACKNOWLEDGED</Status>" +
               "</EPD051_ACK>";
    }
}
