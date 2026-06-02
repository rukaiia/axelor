package com.example.tir.processor;

import com.example.tir.service.XmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class Epd029Processor implements TirMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(Epd029Processor.class);

    @Override
    public String process(String xmlPayload) {
        log.info("[EPD029] Обработка сообщения");
        Document doc = XmlParser.parse(xmlPayload);
        String guaranteeNumber = XmlParser.getTagValue(doc, "GuaranteeNumber");
        String customsIndex = XmlParser.getTagValue(doc, "CustomsIndex");
        if (guaranteeNumber == null || guaranteeNumber.isEmpty()) {
            return XmlParser.buildSoapFault("CLIENT_VALIDATION_ERROR",
                "Отсутствует элемент GuaranteeNumber");
        }

        log.info("[EPD029] Транзит разрешён для {}", guaranteeNumber);
        return "<EPD029_ACK>" +
               "<GuaranteeNumber>" + guaranteeNumber + "</GuaranteeNumber>" +
               "<CustomsIndex>" + customsIndex + "</CustomsIndex>" +
               "<Status>TRANSIT_CONFIRMED</Status>" +
               "</EPD029_ACK>";
    }
}
