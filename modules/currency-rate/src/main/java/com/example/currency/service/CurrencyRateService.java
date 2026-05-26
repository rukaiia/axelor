package com.example.currency.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.currency.db.CurrencyRate;
import com.example.currency.db.repo.CurrencyRateServiceRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class CurrencyRateService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyRateService.class);

    @Inject
    private CurrencyRateServiceRepository rateRepository;

    public int fetchAndUpdate() {
        log.info("Начало загрузки курсов НБКР");
        String xml = NbkrClient.fetchXml();
        List<CurrencyRate> rates = NbkrXmlParser.parse(xml);

        if (rates.isEmpty()) {
            log.warn("XML не содержит валют");
            return 0;
        }
        int savedCount = persistRates(rates);
        log.info("Загрузка завершена. Обработано: {} из {}", savedCount, rates.size());
        return savedCount;
    }

    public int persistRates(List<CurrencyRate> rates) {
        int successCount = 0;
        for (CurrencyRate incoming : rates) {
            try {
                saveOrUpdate(incoming);
                successCount++;
            } catch (Exception e) {
                log.error("Не удалось сохранить курс {} за {}: {}",
                        incoming.getCode(), incoming.getRateDate(), e.getMessage());
            }
        }
        return successCount;
    }

    @Transactional
    public void saveOrUpdate(CurrencyRate incoming) {
        String code = incoming.getCode();
        LocalDate rateDate = incoming.getRateDate();

        CurrencyRate existing = rateRepository.findByCodeAndDate(code, rateDate);
        if (existing != null) {
            existing.setName(incoming.getName());
            existing.setNominal(incoming.getNominal());
            existing.setRate(incoming.getRate());
            rateRepository.save(existing);
            log.debug("Обновлён: {} за {} → {}", code, rateDate, incoming.getRate());
        } else {
            rateRepository.save(incoming);
            log.debug("Создан:   {} за {} → {}", code, rateDate, incoming.getRate());
        }
    }
}