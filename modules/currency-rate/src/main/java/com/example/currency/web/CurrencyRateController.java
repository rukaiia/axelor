package com.example.currency.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.example.currency.service.CurrencyRateService;
import com.google.inject.Inject;


public class CurrencyRateController {

    private static final Logger log = LoggerFactory.getLogger(CurrencyRateController.class);

    @Inject
    private CurrencyRateService currencyRateService;

    public void fetchRates(ActionRequest request, ActionResponse response) {
        log.info("Ручной запуск загрузки курсов НБКР из UI");
        try {
            int count = currencyRateService.fetchAndUpdate();
            response.setInfo(
                "Курсы валют успешно обновлены. Загружено: " + count + " записей."
            );
            response.setReload(true);

        } catch (Exception e) {
            log.error("Ошибка при ручной загрузке курсов НБКР", e);
            response.setError("Не удалось загрузить курсы: " + e.getMessage());
        }
    }
}
