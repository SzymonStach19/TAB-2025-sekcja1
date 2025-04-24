package org.example.magazyn.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.magazyn.entity.*;
import org.example.magazyn.repository.ReservationRepository;
import org.example.magazyn.repository.ZoneRepository;
import org.example.magazyn.service.ReportTABService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportTABServiceImpl implements ReportTABService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Override
    public byte[] generateProfitReport(String productCategory, Long zoneId, LocalDate startDate, LocalDate endDate,
                                       boolean includeTotalRevenue, boolean includeTotalCost, boolean includeProfit) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font normalFont = new Font(baseFont, 12, Font.NORMAL);
            Font headerFont = new Font(baseFont, 12, Font.BOLD);

            Paragraph title = new Paragraph("Raport generowanego zysku", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("Wygenerowano: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), normalFont));

            if (productCategory != null && !productCategory.isEmpty()) {
                document.add(new Paragraph("Kategoria: " + productCategory, normalFont));
            } else {
                document.add(new Paragraph("Kategoria: Wszystkie", normalFont));
            }

            if (zoneId != null) {
                Zone zone = zoneRepository.findById(zoneId).orElse(null);
                if (zone != null) {
                    document.add(new Paragraph("Strefa: " + zone.getName(), normalFont));
                }
            } else {
                document.add(new Paragraph("Strefa: Wszystkie", normalFont));
            }

            document.add(new Paragraph("Zakres dat: " + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                    " do " + endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), normalFont));
            document.add(Chunk.NEWLINE);

            List<Reservation> completedReservations = reservationRepository.findByStatus(Reservation.ReservationStatus.COMPLETED);

            completedReservations = completedReservations.stream()
                    .filter(r -> {
                        LocalDate reservationDate = r.getReservationDate().toLocalDate();
                        return !reservationDate.isBefore(startDate) && !reservationDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());

            if (productCategory != null && !productCategory.isEmpty()) {
                completedReservations = completedReservations.stream()
                        .filter(r -> r.getProduct().getCategory().equals(productCategory))
                        .collect(Collectors.toList());
            }

            if (zoneId != null) {
                completedReservations = completedReservations.stream()
                        .filter(r -> r.getProduct().getZone() != null &&
                                r.getProduct().getZone().getId().equals(zoneId))
                        .collect(Collectors.toList());
            }

            double totalRevenue = 0;
            double totalCost = 0;

            for (Reservation reservation : completedReservations) {
                int quantity = reservation.getQuantity();
                // Use prices from reservation instead of product
                double revenue = reservation.getPrice() * quantity;
                double cost = reservation.getPurchasePrice() * quantity;

                totalRevenue += revenue;
                totalCost += cost;
            }

            double totalProfit = totalRevenue - totalCost;

            // Wyświetlenie agregacji w zależności od wybranych opcji
            if (includeTotalRevenue) {
                document.add(new Paragraph("Łączny przychód: " + String.format("%.2f zł", totalRevenue), normalFont));
            }

            if (includeTotalCost) {
                document.add(new Paragraph("Łączny koszt: " + String.format("%.2f zł", totalCost), normalFont));
            }

            if (includeProfit) {
                document.add(new Paragraph("Wygenerowany zysk: " + String.format("%.2f zł", totalProfit), normalFont));
            }

            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            PdfPCell cell = new PdfPCell(new Phrase("Produkt", headerFont));
            table.addCell(cell);
            table.addCell(new PdfPCell(new Phrase("Kategoria", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Marka", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Cena zakupu (jedn.)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Cena sprzedaży (jedn.)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Ilość", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Koszt całkowity", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Przychód całkowity", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Zysk jedn.", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Zysk całkowity", headerFont)));

            for (Reservation reservation : completedReservations) {
                Product product = reservation.getProduct();
                int quantity = reservation.getQuantity();
                // Use prices from reservation instead of product
                double unitPurchasePrice = reservation.getPurchasePrice();
                double unitSalePrice = reservation.getPrice();
                double totalPurchaseCost = unitPurchasePrice * quantity;
                double totalSaleRevenue = unitSalePrice * quantity;
                double unitProfit = unitSalePrice - unitPurchasePrice;
                double productProfit = (unitSalePrice * quantity) - (unitPurchasePrice * quantity);

                table.addCell(new PdfPCell(new Phrase(product.getName(), normalFont)));
                table.addCell(new PdfPCell(new Phrase(product.getCategory(), normalFont)));
                table.addCell(new PdfPCell(new Phrase(product.getBrand(), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f zł", unitPurchasePrice), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f zł", unitSalePrice), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(quantity), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f zł", totalPurchaseCost), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f zł", totalSaleRevenue), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f zł", unitProfit), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f zł", productProfit), normalFont)));
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] generateReservationsReport(String productCategory, String productBrand, Double minPrice, Double maxPrice,
                                             boolean includeTotal, boolean includeConfirmed,
                                             boolean includeCancelled, boolean groupByCategory) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font normalFont = new Font(baseFont, 12, Font.NORMAL);
            Font headerFont = new Font(baseFont, 12, Font.BOLD);
            Font categoryFont = new Font(baseFont, 14, Font.BOLD);

            Paragraph title = new Paragraph("Raport potwierdzonych rezerwacji", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Wygenerowano: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), normalFont));

            if (productCategory != null && !productCategory.isEmpty()) {
                document.add(new Paragraph("Kategoria: " + productCategory, normalFont));
            } else {
                document.add(new Paragraph("Kategoria: Wszystkie", normalFont));
            }

            if (productBrand != null && !productBrand.isEmpty()) {
                document.add(new Paragraph("Marka: " + productBrand, normalFont));
            } else {
                document.add(new Paragraph("Marka: Wszystkie", normalFont));
            }

            if (minPrice != null || maxPrice != null) {
                document.add(new Paragraph("Zakres ceny: " +
                        (minPrice != null ? minPrice + " zł" : "Min") + " - " +
                        (maxPrice != null ? maxPrice + " zł" : "Max"), normalFont));
            }

            document.add(Chunk.NEWLINE);

            List<Reservation> allReservations = reservationRepository.findAll();

            if (productCategory != null && !productCategory.isEmpty()) {
                allReservations = allReservations.stream()
                        .filter(r -> r.getProduct().getCategory().equals(productCategory))
                        .collect(Collectors.toList());
            }

            if (productBrand != null && !productBrand.isEmpty()) {
                allReservations = allReservations.stream()
                        .filter(r -> r.getProduct().getBrand().equals(productBrand))
                        .collect(Collectors.toList());
            }

            if (minPrice != null) {
                allReservations = allReservations.stream()
                        .filter(r -> r.getPrice() >= minPrice) // Use reservation price instead of product price
                        .collect(Collectors.toList());
            }

            if (maxPrice != null) {
                allReservations = allReservations.stream()
                        .filter(r -> r.getPrice() <= maxPrice) // Use reservation price instead of product price
                        .collect(Collectors.toList());
            }

            // Agregacje dla wszystkich rezerwacji
            if (includeTotal) {
                long totalReservations = allReservations.size();
                document.add(new Paragraph("Łączna liczba rezerwacji: " + totalReservations, normalFont));
            }

            if (includeConfirmed) {
                long completedReservations = allReservations.stream()
                        .filter(r -> r.getStatus() == Reservation.ReservationStatus.COMPLETED)
                        .count();
                document.add(new Paragraph("Ilość potwierdzonych rezerwacji: " + completedReservations, normalFont));
            }

            if (includeCancelled) {
                long cancelledReservations = allReservations.stream()
                        .filter(r -> r.getStatus() == Reservation.ReservationStatus.CANCELLED)
                        .count();
                document.add(new Paragraph("Ilość anulowanych rezerwacji: " + cancelledReservations, normalFont));
            }

            document.add(Chunk.NEWLINE);

            // Jeśli wybrano grupowanie po kategoriach i nie wybrano konkretnej kategorii
            if (groupByCategory && (productCategory == null || productCategory.isEmpty())) {
                // Pobierz wszystkie unikalne kategorie
                List<String> categories = allReservations.stream()
                        .map(r -> r.getProduct().getCategory())
                        .distinct()
                        .collect(Collectors.toList());

                // Dla każdej kategorii generuj osobną tabelę
                for (String category : categories) {
                    List<Reservation> categoryReservations = allReservations.stream()
                            .filter(r -> r.getProduct().getCategory().equals(category))
                            .collect(Collectors.toList());

                    Paragraph categoryTitle = new Paragraph("Kategoria: " + category, categoryFont);
                    categoryTitle.setSpacingBefore(10f);
                    document.add(categoryTitle);

                    // Agregacje dla danej kategorii
                    if (includeTotal) {
                        long totalCategoryReservations = categoryReservations.size();
                        document.add(new Paragraph("Łączna liczba rezerwacji: " + totalCategoryReservations, normalFont));
                    }

                    if (includeConfirmed) {
                        long completedCategoryReservations = categoryReservations.stream()
                                .filter(r -> r.getStatus() == Reservation.ReservationStatus.COMPLETED)
                                .count();
                        document.add(new Paragraph("Ilość potwierdzonych rezerwacji: " + completedCategoryReservations, normalFont));
                    }

                    if (includeCancelled) {
                        long cancelledCategoryReservations = categoryReservations.stream()
                                .filter(r -> r.getStatus() == Reservation.ReservationStatus.CANCELLED)
                                .count();
                        document.add(new Paragraph("Ilość anulowanych rezerwacji: " + cancelledCategoryReservations, normalFont));
                    }

                    // Tabela dla danej kategorii
                    addReservationsTable(document, categoryReservations, headerFont, normalFont);
                }
            } else {
                // Standardowa tabela bez grupowania
                addReservationsTable(document, allReservations, headerFont, normalFont);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Updated to include price information
    private void addReservationsTable(Document document, List<Reservation> reservations, Font headerFont, Font normalFont) throws DocumentException {
        PdfPTable table = new PdfPTable(7); // Added 2 columns for prices
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        table.addCell(new PdfPCell(new Phrase("Produkt", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Użytkownik", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Ilość", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Cena zakupu", headerFont))); // New column
        table.addCell(new PdfPCell(new Phrase("Cena sprzedaży", headerFont))); // New column
        table.addCell(new PdfPCell(new Phrase("Data", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Status", headerFont)));

        for (Reservation reservation : reservations) {
            Product product = reservation.getProduct();
            int quantity = reservation.getQuantity();

            table.addCell(new PdfPCell(new Phrase(product.getName(), normalFont)));
            table.addCell(new PdfPCell(new Phrase(reservation.getUser().getName(), normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(quantity), normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.format("%.2f zł", reservation.getPurchasePrice()), normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.format("%.2f zł", reservation.getPrice()), normalFont)));
            table.addCell(new PdfPCell(new Phrase(reservation.getReservationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), normalFont)));
            table.addCell(new PdfPCell(new Phrase(getStatusInPolish(reservation.getStatus()), normalFont)));
        }

        document.add(table);
    }

    private String getStatusInPolish(Reservation.ReservationStatus status) {
        switch (status) {
            case ACTIVE:
                return "Aktywna";
            case COMPLETED:
                return "Potwierdzona";
            case CANCELLED:
                return "Anulowana";
            default:
                return status.toString();
        }
    }
}