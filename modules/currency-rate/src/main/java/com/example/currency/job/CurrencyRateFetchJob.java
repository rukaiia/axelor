package com.example.currency.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.inject.Beans;
import com.example.currency.service.CurrencyRateService;


public class CurrencyRateFetchJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(CurrencyRateFetchJob.class);

    @Override
    public void execute(JobExecutionContext context) {
        log.info("[SCHEDULER] Запущена задача загрузки курсов НБКР");
        try {
            CurrencyRateService service = Beans.get(CurrencyRateService.class);
            int count = service.fetchAndUpdate();
            log.info("[SCHEDULER] Успешно обработано {} курсов", count);

        } catch (Exception e) {
            log.error("[SCHEDULER] Ошибка при загрузке курсов: {}",
                      e.getMessage(), e);
        }
    }
}
