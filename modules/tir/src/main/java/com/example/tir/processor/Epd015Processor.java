package com.example.tir.processor;

import com.example.tir.service.XmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class Epd015Processor implements TirMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(Epd015Processor.class);

    @Override
    public String process(String xmlPayload) {
        log.info("[EPD015] Обработка сообщения");
        Document doc = XmlParser.parse(xmlPayload);

        String guaranteeNumber = XmlParser.getTagValue(doc, "GuaranteeNumber");
        String iruReference = XmlParser.getTagValue(doc, "IruReference");
        String holderNumber = XmlParser.getTagValue(doc, "HolderNumber");
        if (guaranteeNumber == null || guaranteeNumber.isEmpty()) {
            return XmlParser.buildSoapFault("CLIENT_VALIDATION_ERROR",
                    "Отсутствует элемент GuaranteeNumber");
        }
        if (holderNumber == null || holderNumber.isEmpty()) {
            return XmlParser.buildSoapFault("CLIENT_VALIDATION_ERROR",
                    "Отсутствует элемент HolderNumber");
        }
        if (guaranteeNumber.startsWith("XX")) {
            log.info("[EPD015] Гарантия {} — отказ EPD016", guaranteeNumber);
            return "<EPD016>" +
                    "<GuaranteeNumber>" + guaranteeNumber + "</GuaranteeNumber>" +
                    "<IruReference>" + iruReference + "</IruReference>" +
                    "<Status>REJECTED</Status>" +
                    "<Reason>Гарантия отклонена</Reason>" +
                    "</EPD016>";
        } else if (guaranteeNumber.startsWith("KG")) {
            String customsIndex = "CI-" + guaranteeNumber + "-" + System.currentTimeMillis() % 10000;
            log.info("[EPD015] Гарантия {} — одобрено, индекс {}", guaranteeNumber, customsIndex);
            return "<EPD028>" +
                    "<GuaranteeNumber>" + guaranteeNumber + "</GuaranteeNumber>" +
                    "<IruReference>" + iruReference + "</IruReference>" +
                    "<CustomsIndex>" + customsIndex + "</CustomsIndex>" +
                    "<Status>ACCEPTED</Status>" +
                    "</EPD028>";
        } else {
            log.warn("[EPD015] Неверный формат гарантии: {}", guaranteeNumber);
            return XmlParser.buildSoapFault("CLIENT_VALIDATION_ERROR",
                    "Неверный формат номера гарантии: " + guaranteeNumber);
        }
    }
}

