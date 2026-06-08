package com.example.supply;

import com.axelor.app.AxelorModule;
import com.example.supply.service.SupplyContractService;
import com.example.supply.web.SupplyContractController;

public class SupplyContractModule extends AxelorModule {

    @Override
    protected void configure() {
        bind(SupplyContractService.class);
        bind(SupplyContractController.class);
    }
}
