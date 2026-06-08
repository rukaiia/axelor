package com.example.inventory.service;

import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.example.inventory.db.InventorySheet;
import com.example.inventory.db.InventorySheetLine;
import com.example.inventory.db.repo.InventorySheetLineRepository;
import com.example.inventory.db.repo.InventorySheetRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.List;

@Singleton
public class InventorySheetService {

    private static final Logger log = LoggerFactory.getLogger(InventorySheetService.class);

    @Inject
    private InventorySheetLineRepository lineRepo;

    @Inject
    private InventorySheetRepository sheetRepo;

    @Transactional
    public void saveInventoryLine(Context context, ActionResponse response) {
        Long id = (Long) context.get("id");



        BigDecimal plannedQty = toBigDecimal(context.get("plannedQty"));
        BigDecimal actualQty = toBigDecimal(context.get("actualQty"));
        BigDecimal unitCost = toBigDecimal(context.get("unitCost"));

        if (unitCost.compareTo(BigDecimal.ZERO) == 0) {
            log.info("Пропуск сохранения — unitCost равен нулю");
            return;
        }

        InventorySheetLine line;
        if (id != null) {
            line = lineRepo.find(id);
            if (line == null) {
                line = new InventorySheetLine();
            }
        } else {
            line = new InventorySheetLine();
        }

        Context parentContext = context.getParent();
        if (parentContext != null) {
            Long sheetId = null;
            if (parentContext.get("id") != null) {
                sheetId = Long.valueOf(parentContext.get("id").toString());
            } else if (parentContext.get("_id") != null) {
                sheetId = Long.valueOf(parentContext.get("_id").toString());
            }
            if (sheetId != null) {
                InventorySheet sheet = sheetRepo.find(sheetId);
                if (sheet != null) {
                    line.setSheet(sheet);
                }
            }
        } else {
            log.warn("parentContext == null");
        }

        line.setArticleNumber((String) context.get("articleNumber"));
        line.setProductName((String) context.get("productName"));
        line.setUnit((String) context.get("unit"));
        line.setNote((String) context.get("note"));

        BigDecimal variance = actualQty.subtract(plannedQty);
        BigDecimal varianceAmount = variance.multiply(unitCost).setScale(2, RoundingMode.HALF_UP);

        line.setPlannedQty(plannedQty);
        line.setActualQty(actualQty);
        line.setUnitCost(unitCost);
        line.setVariance(variance);
        line.setVarianceAmount(varianceAmount);

        lineRepo.save(line);
        log.info("Сохранена строка ведомости id={}, отклонение={}", line.getId(), variance);

        response.setValue("variance", variance);
        response.setValue("varianceAmount", varianceAmount);

        if (line.getSheet() != null) {
            updateSheetTotalVariance(line.getSheet());
        } else {
            log.warn("sheet == null, totalVariance не обновляется.");
        }
    }

    private void updateSheetTotalVariance(InventorySheet sheet) {
        List<InventorySheetLine> lines = lineRepo.all()
                .filter("self.sheet.id = ?1", sheet.getId())
                .fetch();

        BigDecimal total = lines.stream()
                .map(l -> l.getVarianceAmount() != null ? l.getVarianceAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        com.axelor.db.JPA.em()
                .createNativeQuery("UPDATE inventory_inventory_sheet SET total_variance = :total WHERE id = :id")
                .setParameter("total", total)
                .setParameter("id", sheet.getId())
                .executeUpdate();

        log.info("Обновлено итоговое отклонение ведомости id={}: {}", sheet.getId(), total);
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