package com.example.tir.service;

import com.example.tir.db.TirMessage;
import com.example.tir.db.repo.TirMessageServiceRepository;
import com.example.tir.processor.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class TirExchangeService {

    private static final Logger log = LoggerFactory.getLogger(TirExchangeService.class);

    @Inject
    private TirMessageServiceRepository messageRepository;
    private final Map<String, TirMessageProcessor> processors = new HashMap<>();

    public TirExchangeService() {
        processors.put("EPD015", new Epd015Processor());
        processors.put("EPD028", new Epd028Processor());
        processors.put("EPD016", new Epd016Processor());
        processors.put("EPD029", new Epd029Processor());
        processors.put("EPD051", new Epd051Processor());
        processors.put("EPD045", new Epd045Processor());
    }

    @Transactional
    public String exchange(String xmlPayload) {
        log.info("Получено TIR сообщение");

        String messageType;
        try {
            messageType = XmlParser.getRootTag(xmlPayload);
        } catch (Exception e) {
            log.error("Ошибка парсинга XML: {}", e.getMessage());
            return XmlParser.buildSoapFault("CLIENT_VALIDATION_ERROR",
                    "Невалидный XML: " + e.getMessage());
        }
        log.info("Тип сообщения: {}", messageType);
        TirMessageProcessor processor = processors.get(messageType);
        if (processor == null) {
            log.warn("Неизвестный тип сообщения: {}", messageType);
            return XmlParser.buildSoapFault("UNKNOWN_MESSAGE_TYPE",
                    "Неизвестный тип сообщения: " + messageType);
        }

        String response;
        String status;
        try {
            response = processor.process(xmlPayload);
            status = response.contains("REJECTED") || response.contains("Fault")
                    ? "REJECTED" : "PROCESSED";
        } catch (Exception e) {
            log.error("Ошибка при обработке {}: {}", messageType, e.getMessage());
            response = XmlParser.buildSoapFault("PROCESSING_ERROR", e.getMessage());
            status = "ERROR";
        }
        saveMessage(xmlPayload, messageType, status, response);
        log.info("Сообщение {} обработано со статусом {}", messageType, status);
        return response;
    }

    public List<TirMessage> getAllMessages() {
        return messageRepository.findAllSorted();
    }

    private void saveMessage(String xmlPayload, String messageType, String status, String response) {
        try {
            Document doc = XmlParser.parse(xmlPayload);
            TirMessage message = new TirMessage();
            message.setMessageType(messageType);
            message.setGuaranteeNumber(XmlParser.getTagValue(doc, "GuaranteeNumber"));
            message.setIruReference(XmlParser.getTagValue(doc, "IruReference"));
            message.setCustomsIndex(XmlParser.getTagValue(doc, "CustomsIndex"));
            message.setStatus(status);
            message.setPayload(xmlPayload);
            message.setResponse(response);
            message.setCreatedAt(LocalDateTime.now());
            messageRepository.save(message);
        } catch (Exception e) {
            log.error("Ошибка при сохранении сообщения: {}", e.getMessage());
        }
    }
}