package com.example.munyaka.services;

import com.example.munyaka.DTO.DebtorSummary;
import com.example.munyaka.repository.SaleRepository;
import com.example.munyaka.tables.Sale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DebtorService {

    private final SaleRepository saleRepository;

    public List<Sale> getPendingSales() {
        return saleRepository.findByPaymentStatusAndBalanceDueGreaterThan("PENDING", 0.0);
    }

    public List<DebtorSummary> getDebtors() {
        List<String> customerPhones = saleRepository.findCustomersWithPendingPayments();
        List<DebtorSummary> debtors = new ArrayList<>();

        for (String phone : customerPhones) {
            List<Sale> pendingSales = saleRepository.findByCustomerPhoneAndPaymentStatusAndBalanceDueGreaterThan(
                    phone, "PENDING", -0.0);

            if (!pendingSales.isEmpty()) {
                Sale firstSale = pendingSales.get(0);
                double totalDebt = pendingSales.stream().mapToDouble(Sale::getBalanceDue).sum();

                DebtorSummary debtor = DebtorSummary.builder()
                        .customerName(firstSale.getCustomerName())
                        .customerPhone(phone)
                        .totalDebt(totalDebt)
                        .lastSaleDate(String.valueOf(firstSale.getSaleDate()))
                        .paymentStatus("PENDING") // You can calculate overdue status here
                        .sales(pendingSales)
                        .build();

                debtors.add(debtor);
            }
        }

        return debtors;
    }
}

