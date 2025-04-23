package org.example.magazyn.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.magazyn.entity.*;
import org.example.magazyn.repository.ProductRepository;
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
    private ProductRepository productRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Override
    public byte[] generateProfitReport(String productCategory, Long zoneId, LocalDate startDate, LocalDate endDate) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Utwórz fonty z obsługą polskich znaków
            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font normalFont = new Font(baseFont, 12, Font.NORMAL);
            Font headerFont = new Font(baseFont, 12, Font.BOLD);

            // Add title
            Paragraph title = new Paragraph("Raport generowanego zysku", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Add generation date
            document.add(new Paragraph("Wygenerowano: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), normalFont));

            // Add category information
            if (productCategory != null && !productCategory.isEmpty()) {
                document.add(new Paragraph("Kategoria: " + productCategory, normalFont));
            } else {
                document.add(new Paragraph("Kategoria: Wszystkie", normalFont));
            }

            // Add zone information
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

            // Calculate profit from completed reservations
            List<Reservation> completedReservations = reservationRepository.findByStatus(Reservation.ReservationStatus.COMPLETED);

            // Filter by date range
            completedReservations = completedReservations.stream()
                    .filter(r -> {
                        LocalDate reservationDate = r.getReservationDate().toLocalDate();
                        return !reservationDate.isBefore(startDate) && !reservationDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());

            // Filter by category if specified
            if (productCategory != null && !productCategory.isEmpty()) {
                completedReservations = completedReservations.stream()
                        .filter(r -> r.getProduct().getCategory().equals(productCategory))
                        .collect(Collectors.toList());
            }

            // Filter by zone if specified
            if (zoneId != null) {
                completedReservations = completedReservations.stream()
                        .filter(r -> r.getProduct().getZone() != null &&
                                r.getProduct().getZone().getId().equals(zoneId))
                        .collect(Collectors.toList());
            }

            // Calculate profit
            double totalRevenue = 0;
            double totalCost = 0;

            for (Reservation reservation : completedReservations) {
                Product product = reservation.getProduct();
                int quantity = reservation.getQuantity();

                double revenue = product.getPrice() * quantity;
                double cost = product.getPurchasePrice() * quantity;

                totalRevenue += revenue;
                totalCost += cost;
            }

            double totalProfit = totalRevenue - totalCost;

            // Add profit information
            document.add(new Paragraph("Łączny przychód: " + String.format("%.2f zł", totalRevenue), normalFont));
            document.add(new Paragraph("Łączny koszt: " + String.format("%.2f zł", totalCost), normalFont));
            document.add(new Paragraph("Wygenerowany zysk: " + String.format("%.2f zł", totalProfit), normalFont));
            document.add(Chunk.NEWLINE);

            // Add products table
            PdfPTable table = new PdfPTable(10); // Increased to include total purchase and sale prices
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Headers
            PdfPCell cell = new PdfPCell(new Phrase("Produkt", headerFont));
            table.addCell(cell);
            table.addCell(new PdfPCell(new Phrase("Kategoria", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Marka", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Cena zakupu (jedn.)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Cena sprzedaży (jedn.)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Ilość", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Koszt całkowity", headerFont))); // Total purchase cost
            table.addCell(new PdfPCell(new Phrase("Przychód całkowity", headerFont))); // Total sale revenue
            table.addCell(new PdfPCell(new Phrase("Zysk jedn.", headerFont))); // Unit profit
            table.addCell(new PdfPCell(new Phrase("Zysk całkowity", headerFont))); // Total profit

            // Product details
            for (Reservation reservation : completedReservations) {
                Product product = reservation.getProduct();
                int quantity = reservation.getQuantity();
                double unitPurchasePrice = product.getPurchasePrice();
                double unitSalePrice = product.getPrice();
                double totalPurchaseCost = unitPurchasePrice * quantity;
                double totalSaleRevenue = unitSalePrice * quantity;
                double unitProfit = unitSalePrice - unitPurchasePrice;

                table.addCell(new PdfPCell(new Phrase(product.getName(), normalFont)));
                table.addCell(new PdfPCell(new Phrase(product.getCategory(), normalFont)));
                table.addCell(new PdfPCell(new Phrase(product.getBrand(), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f zł", unitPurchasePrice), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f zł", unitSalePrice), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(quantity), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f zł", totalPurchaseCost), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f zł", totalSaleRevenue), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f zł", unitProfit), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f zł", totalProfit), normalFont)));
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
                                             boolean includeCancelled, boolean includeTurnover) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Utwórz fonty z obsługą polskich znaków
            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font normalFont = new Font(baseFont, 12, Font.NORMAL);
            Font headerFont = new Font(baseFont, 12, Font.BOLD);

            // Add title
            Paragraph title = new Paragraph("Raport potwierdzonych rezerwacji", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Add generation date
            document.add(new Paragraph("Wygenerowano: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), normalFont));

            // Add filter parameters
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

            // Get all reservations
            List<Reservation> allReservations = reservationRepository.findAll();

            // Apply filters
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
                        .filter(r -> r.getProduct().getPrice() >= minPrice)
                        .collect(Collectors.toList());
            }

            if (maxPrice != null) {
                allReservations = allReservations.stream()
                        .filter(r -> r.getProduct().getPrice() <= maxPrice)
                        .collect(Collectors.toList());
            }

            // Count reservations by status
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

            if (includeTurnover) {
                double turnover = allReservations.stream()
                        .filter(r -> r.getStatus() == Reservation.ReservationStatus.COMPLETED)
                        .mapToDouble(r -> r.getProduct().getPrice() * r.getQuantity())
                        .sum();
                document.add(new Paragraph("Obrót: " + String.format("%.2f zł", turnover), normalFont));
            }

            document.add(Chunk.NEWLINE);

            // Add reservations table
            PdfPTable table = new PdfPTable(7); // Increased to include unit price and total price
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Headers
            table.addCell(new PdfPCell(new Phrase("Produkt", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Użytkownik", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Cena sprzedaży (jedn.)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Ilość", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Wartość całkowita", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Data", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Status", headerFont)));

            // Reservation details
            for (Reservation reservation : allReservations) {
                Product product = reservation.getProduct();
                int quantity = reservation.getQuantity();
                double unitSalePrice = product.getPrice();
                double totalSaleValue = unitSalePrice * quantity;

                table.addCell(new PdfPCell(new Phrase(product.getName(), normalFont)));
                table.addCell(new PdfPCell(new Phrase(reservation.getUser().getName(), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f zł", unitSalePrice), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(quantity), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f zł", totalSaleValue), normalFont)));
                table.addCell(new PdfPCell(new Phrase(reservation.getReservationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), normalFont)));
                table.addCell(new PdfPCell(new Phrase(getStatusInPolish(reservation.getStatus()), normalFont)));
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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