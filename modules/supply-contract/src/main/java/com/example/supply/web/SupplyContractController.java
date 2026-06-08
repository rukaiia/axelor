package com.example.supply.web;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.example.supply.service.SupplyContractService;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class SupplyContractController {

    private static final Logger log = LoggerFactory.getLogger(SupplyContractController.class);

    @Inject
    private SupplyContractService supplyContractService;

    public void saveContractLine(ActionRequest request, ActionResponse response) {
        log.info("onchange — saveContractLine");
        try {
            supplyContractService.saveContractLine(request.getContext(), response);
        } catch (Exception e) {
            if (e.getCause() instanceof org.hibernate.StaleObjectStateException
                    || (e.getMessage() != null && e.getMessage().contains("StaleObject"))) {
                log.warn("Optimistic lock — перезагрузка формы");
                response.setReload(true);
            } else {
                log.error("Ошибка при сохранении строки договора", e);
                response.setError("Ошибка при сохранении: " + e.getMessage());
            }
        }
    }
}