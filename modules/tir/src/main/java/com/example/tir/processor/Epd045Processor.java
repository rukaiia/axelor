package com.example.tir.processor;

import com.example.tir.service.XmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class Epd045Processor implements TirMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(Epd045Processor.class);

    @Override
    public String process(String xmlPayload) {
        log.info("[EPD045] Обработка сообщения");
        Document doc = XmlParser.parse(xmlPayload);

        String guaranteeNumber = XmlParser.getTagValue(doc, "GuaranteeNumber");
        if (guaranteeNumber == null || guaranteeNumber.isEmpty()) {
            return XmlParser.buildSoapFault("CLIENT_VALIDATION_ERROR",
                "Отсутствует элемент GuaranteeNumber");
        }
        log.info("[EPD045] Процедура МДП завершена для {}", guaranteeNumber);
        return "<EPD045_ACK>" +
               "<GuaranteeNumber>" + guaranteeNumber + "</GuaranteeNumber>" +
               "<Status>PROCEDURE_COMPLETED</Status>" +
               "</EPD045_ACK>";
    }
}
