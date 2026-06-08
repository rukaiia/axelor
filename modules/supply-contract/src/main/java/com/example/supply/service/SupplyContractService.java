package com.example.supply.service;

import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.example.supply.db.SupplyContract;
import com.example.supply.db.SupplyContractLine;
import com.example.supply.db.repo.SupplyContractLineRepository;
import com.example.supply.db.repo.SupplyContractRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Singleton
public class SupplyContractService {

    private static final Logger log = LoggerFactory.getLogger(SupplyContractService.class);

    @Inject
    private SupplyContractLineRepository lineRepo;

    @Inject
    private SupplyContractRepository contractRepo;

    @Transactional
    public void saveContractLine(Context context, ActionResponse response) {
        Long id = (Long) context.get("id");

        BigDecimal quantity = toBigDecimal(context.get("quantity"));
        BigDecimal unitPrice = toBigDecimal(context.get("unitPrice"));

        if (quantity.compareTo(BigDecimal.ZERO) == 0 || unitPrice.compareTo(BigDecimal.ZERO) == 0) {
            log.info("Пропуск сохранения — quantity и unitPrice равны нулю");
            return;
        }

        SupplyContractLine line;
        if (id != null) {
            line = lineRepo.find(id);
            if (line == null) {
                line = new SupplyContractLine();
            }
        } else {
            line = new SupplyContractLine();
        }

        Context parentContext = context.getParent();
        if (parentContext != null) {
            Long contractId = null;
            if (parentContext.get("id") != null) {
                contractId = Long.valueOf(parentContext.get("id").toString());
            } else if (parentContext.get("_id") != null) {
                contractId = Long.valueOf(parentContext.get("_id").toString());
            }
            if (contractId != null) {
                SupplyContract contract = contractRepo.find(contractId);
                if (contract != null) {
                    line.setContract(contract);
                }
            }
        } else {
            log.warn("parentContext == null");
        }

        line.setProductCode((String) context.get("productCode"));
        line.setProductName((String) context.get("productName"));
        line.setUnit((String) context.get("unit"));

        Object lineNumberObj = context.get("lineNumber");
        if (lineNumberObj != null) {
            line.setLineNumber(Integer.valueOf(lineNumberObj.toString()));
        }

        BigDecimal totalPrice = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);

        line.setQuantity(quantity);
        line.setUnitPrice(unitPrice);
        line.setTotalPrice(totalPrice);

        lineRepo.save(line);
        log.info("Сохранена строка договора id={}, totalPrice={}", line.getId(), totalPrice);

        response.setValue("totalPrice", totalPrice);

        if (line.getContract() != null) {
            updateContractTotalAmount(line.getContract());
        } else {
            log.warn("contract == null, totalAmount не обновляется.");
        }
    }

    @Transactional
    private void updateContractTotalAmount(SupplyContract contract) {
        List<SupplyContractLine> lines = lineRepo.all()
                .filter("self.contract.id = ?1", contract.getId())
                .fetch();

        BigDecimal total = lines.stream()
                .map(l -> l.getTotalPrice() != null ? l.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        com.axelor.db.JPA.em().clear();

        com.axelor.db.JPA.em()
                .createNativeQuery("UPDATE supply_supply_contract SET total_amount = :total WHERE id = :id")
                .setParameter("total", total)
                .setParameter("id", contract.getId())
                .executeUpdate();

        log.info("Обновлена итоговая сумма договора id={}: {}", contract.getId(), total);
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