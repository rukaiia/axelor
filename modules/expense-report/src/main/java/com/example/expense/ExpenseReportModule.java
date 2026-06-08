package com.example.expense;

import com.axelor.app.AxelorModule;
import com.example.expense.service.ExpenseReportService;
import com.example.expense.web.ExpenseReportController;

public class ExpenseReportModule extends AxelorModule {

    @Override
    protected void configure() {
        bind(ExpenseReportService.class);
        bind(ExpenseReportController.class);
    }
}
