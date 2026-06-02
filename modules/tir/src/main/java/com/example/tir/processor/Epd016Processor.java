package com.example.tir.processor;

import com.example.tir.service.XmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class Epd016Processor implements TirMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(Epd016Processor.class);

    @Override
    public String process(String xmlPayload) {
        log.info("[EPD016] Обработка сообщения");
        Document doc = XmlParser.parse(xmlPayload);

        String guaranteeNumber = XmlParser.getTagValue(doc, "GuaranteeNumber");
        if (guaranteeNumber == null || guaranteeNumber.isEmpty()) {
            return XmlParser.buildSoapFault("CLIENT_VALIDATION_ERROR",
                    "Отсутствует элемент GuaranteeNumber");
        }
        return "<EPD016_ACK>" +
                "<GuaranteeNumber>" + guaranteeNumber + "</GuaranteeNumber>" +
                "<Status>REJECTION_ACKNOWLEDGED</Status>" +
                "</EPD016_ACK>";
    }
}