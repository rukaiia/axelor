package com.example.expense.service;

import com.axelor.rpc.Context;
import com.example.expense.db.ExpenseReport;
import com.example.expense.db.ExpenseReportLine;
import com.example.expense.db.repo.ExpenseReportLineRepository;
import com.example.expense.db.repo.ExpenseReportRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Singleton
public class ExpenseReportService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseReportService.class);

    @Inject
    private ExpenseReportLineRepository lineRepo;

    @Inject
    private ExpenseReportRepository reportRepo;

    @Transactional
    public void saveExpenseLine(Context context) {
        Long id = (Long) context.get("id");
        BigDecimal amount = toBigDecimal(context.get("amount"));
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            log.info("Пропуск сохранения — amount равен нулю");
            return;
        }

        ExpenseReportLine line;
        if (id != null) {
            line = lineRepo.find(id);
            if (line == null) {
                line = new ExpenseReportLine();
            }
        } else {
            line = new ExpenseReportLine();
        }


        Context parentContext = context.getParent();
        if (parentContext != null) {
            Long reportId = null;
            if (parentContext.get("id") != null) {
                reportId = Long.valueOf(parentContext.get("id").toString());
            } else if (parentContext.get("_id") != null) {
                reportId = Long.valueOf(parentContext.get("_id").toString());
            }
            if (reportId != null) {
                ExpenseReport report = reportRepo.find(reportId);
                if (report != null) {
                    line.setReport(report);
                }
            }
        } else {
            log.warn("parentContext == null");
        }

        line.setCategory((String) context.get("category"));
        line.setDescription((String) context.get("description"));
        line.setDocumentNumber((String) context.get("documentNumber"));
        line.setCurrency((String) context.get("currency"));
        line.setAmount(amount);

        Object expenseDateObj = context.get("expenseDate");
        if (expenseDateObj instanceof LocalDate) {
            line.setExpenseDate((LocalDate) expenseDateObj);
        }

        Object documentDateObj = context.get("documentDate");

        if (documentDateObj instanceof LocalDate) {
            line.setDocumentDate((LocalDate) documentDateObj);
        }

        lineRepo.save(line);
        log.info("Сохранена строка расхода id={}", line.getId());

        if (line.getReport() != null) {
            updateReportTotals(line.getReport());
        } else {
            log.warn("report == null, итоги не обновляются.");
        }
    }

    private void updateReportTotals(ExpenseReport report) {
        ExpenseReport managed = reportRepo.find(report.getId());
        if (managed == null) return;

        List<ExpenseReportLine> lines = lineRepo.all()
                .filter("self.report.id = ?1", managed.getId())
                .fetch();

        BigDecimal totalExpense = lines.stream()
                .map(l -> l.getAmount() != null ? l.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal advance = managed.getAdvanceAmount() != null
                ? managed.getAdvanceAmount() : BigDecimal.ZERO;

        managed.setTotalExpense(totalExpense);
        managed.setDifference(advance.subtract(totalExpense));
        reportRepo.save(managed);
        log.info("Обновлены итоги отчёта id={}: расходы={}, разница={}",
                managed.getId(), totalExpense, managed.getDifference());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}