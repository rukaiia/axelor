package com.example.tir;

import com.axelor.app.AxelorModule;
import com.example.tir.db.repo.TirMessageServiceRepository;
import com.example.tir.service.TirExchangeService;
import com.example.tir.web.TirExchangeController;

public class TirModule extends AxelorModule {

    @Override
    protected void configure() {
        bind(TirExchangeService.class);
        bind(TirExchangeController.class);
        bind(TirMessageServiceRepository.class);
    }
}
