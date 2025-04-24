package org.example.magazyn.service;

import java.time.LocalDate;

public interface ReportTABService {
    byte[] generateProfitReport(String productCategory, Long zoneId, LocalDate startDate, LocalDate endDate,
                                boolean includeTotalRevenue, boolean includeTotalCost, boolean includeProfit);

    byte[] generateReservationsReport(String productCategory, String productBrand, Double minPrice, Double maxPrice,
                                      boolean includeTotal, boolean includeConfirmed,
                                      boolean includeCancelled, boolean includeTurnover);
}