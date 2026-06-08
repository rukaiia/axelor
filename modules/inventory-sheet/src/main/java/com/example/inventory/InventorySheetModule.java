package com.example.inventory;

import com.axelor.app.AxelorModule;
import com.example.inventory.service.InventorySheetService;
import com.example.inventory.web.InventorySheetController;

public class InventorySheetModule extends AxelorModule {

    @Override
    protected void configure() {
        bind(InventorySheetService.class);
        bind(InventorySheetController.class);
    }
}
