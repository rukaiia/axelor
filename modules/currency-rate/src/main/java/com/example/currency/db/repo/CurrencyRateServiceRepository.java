package com.example.currency.db.repo;

import java.time.LocalDate;
import java.util.List;
import com.example.currency.db.CurrencyRate;


public class CurrencyRateServiceRepository extends CurrencyRateRepository {

    public CurrencyRate findByCodeAndDate(String code, LocalDate date) {
        return all()
            .filter("self.code = :code AND self.rateDate = :date")
            .bind("code", code)
            .bind("date", date)
            .fetchOne();
    }
    public List<CurrencyRate> findAllByDate(LocalDate date) {
        return all()
            .filter("self.rateDate = :date")
            .bind("date", date)
            .order("self.code")
            .fetch();
    }
}
