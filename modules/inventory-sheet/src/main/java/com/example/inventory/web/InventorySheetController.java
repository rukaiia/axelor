package com.example.inventory.web;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.example.inventory.service.InventorySheetService;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class InventorySheetController {

    private static final Logger log = LoggerFactory.getLogger(InventorySheetController.class);

    @Inject
    private InventorySheetService inventorySheetService;

    public void saveInventoryLine(ActionRequest request, ActionResponse response) {
        log.info("onchange — saveInventoryLine");
        try {
            inventorySheetService.saveInventoryLine(request.getContext(), response);
        } catch (Exception e) {
            if (e.getCause() instanceof org.hibernate.StaleObjectStateException
                    || (e.getMessage() != null && e.getMessage().contains("StaleObject"))) {
                log.warn("Optimistic lock — перезагрузка формы");
                response.setReload(true);
            } else {
                log.error("Ошибка при сохранении строки ведомости", e);
                response.setError("Ошибка при сохранении: " + e.getMessage());
            }
        }
    }
}