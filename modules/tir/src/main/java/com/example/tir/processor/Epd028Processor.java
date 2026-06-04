package com.example.tir.processor;

import com.example.tir.service.XmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class Epd028Processor implements TirMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(Epd028Processor.class);

    @Override
    public String process(String xmlPayload) {
        log.info("[EPD028] Обработка сообщения");
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
        String customsIndex = XmlParser.getTagValue(doc, "CustomsIndex");

        if (guaranteeNumber == null || guaranteeNumber.isEmpty()) {
            return XmlParser.buildSoapFault("CLIENT_VALIDATION_ERROR",
                "Отсутствует элемент GuaranteeNumber");
        }

        log.info("[EPD028] Таможенный индекс {} подтверждён", customsIndex);
        return "<EPD029>" +
               "<GuaranteeNumber>" + guaranteeNumber + "</GuaranteeNumber>" +
               "<CustomsIndex>" + customsIndex + "</CustomsIndex>" +
               "<Status>TRANSIT_ALLOWED</Status>" +
               "</EPD029>";
    }
}
