package com.example.expense.web;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.example.expense.service.ExpenseReportService;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class ExpenseReportController {

    private static final Logger log = LoggerFactory.getLogger(ExpenseReportController.class);

    @Inject
    private ExpenseReportService expenseReportService;

    public void saveExpenseLine(ActionRequest request, ActionResponse response) {
        log.info("onchange — saveExpenseLine");
        try {
            expenseReportService.saveExpenseLine(request.getContext());
        } catch (Exception e) {
            if (e.getCause() instanceof org.hibernate.StaleObjectStateException
                    || (e.getMessage() != null && e.getMessage().contains("StaleObject"))) {
                log.warn("Optimistic lock — перезагрузка формы");
                response.setReload(true);
            } else {
                log.error("Ошибка при сохранении строки расхода", e);
                response.setError("Ошибка при сохранении: " + e.getMessage());
            }
        }
    }
}