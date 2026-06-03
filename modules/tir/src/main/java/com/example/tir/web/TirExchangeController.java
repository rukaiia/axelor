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
}