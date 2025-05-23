package org.example.magazyn.service.impl;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import lombok.RequiredArgsConstructor;
import org.example.magazyn.dto.ReportDto;
import org.example.magazyn.entity.Report;
import org.example.magazyn.entity.Reservation;
import org.example.magazyn.entity.User;
import org.example.magazyn.repository.ReportRepository;
import org.example.magazyn.repository.ReservationRepository;
import org.example.magazyn.repository.UserRepository;
import org.example.magazyn.service.ReportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Principal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final ReportRepository reportRepository;
    private final ReservationRepository reservationRepository;

    @Value("${app.reports.directory}")
    private String reportsDirectory;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Report generateReportForReservation(Reservation reservation, Principal principal) {
        if (reportRepository.findByReservation(reservation).isPresent()) {
            throw new IllegalStateException("Raport dla tej rezerwacji już istnieje");
        }

        File directory = new File(reportsDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        User generatedByUser = userRepository.findByEmail(principal.getName());
        if (generatedByUser == null) {
            throw new IllegalArgumentException("Użytkownik nie został znaleziony");
        }

        String filename = "reservation_report_" + reservation.getId() + ".pdf";
        String filePath = reportsDirectory + File.separator + filename;

        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.add(new Paragraph("Raport Rezerwacji")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16)
                    .setBold());

            Table table = new Table(2);
            table.addCell("ID Rezerwacji");
            table.addCell(String.valueOf(reservation.getId()));

            table.addCell("Nazwa Produktu");
            table.addCell(reservation.getProduct().getName());

            table.addCell("Marka");
            table.addCell(String.valueOf(reservation.getProduct().getBrand()));

            table.addCell("Opis");
            table.addCell(String.valueOf(reservation.getProduct().getDescription()));

            table.addCell("Ilosc");
            table.addCell(String.valueOf(reservation.getQuantity()));

            table.addCell("Cena");
            table.addCell(String.valueOf(reservation.getProduct().getPrice()));

            table.addCell("Data dokonania rezerwacji");
            table.addCell(reservation.getReservationDate()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            table.addCell("Rezerwacji dokonal");
            table.addCell(reservation.getUser().getEmail());

            table.addCell("Rezerwacje potwierdzil");
            table.addCell(reservation.getStatusChangedByUser() != null
                    ? reservation.getStatusChangedByUser().getEmail()
                    : "Nie dotyczy");

            table.addCell("Status rezerwacji");
            table.addCell(reservation.getStatus().toString());

            document.add(table);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Report report = new Report();
        report.setReservation(reservation);
        report.setFilePath(filePath);
        report.setGeneratedByUser(generatedByUser);

        return reportRepository.save(report);
    }

    @Override
    public ReportDto getReportByReservationId(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono rezerwacji"));

        Report report = reportRepository.findByReservation(reservation)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono raportu"));

        ReportDto reportDto = new ReportDto();
        reportDto.setId(report.getId());
        reportDto.setReservationId(reservation.getId());
        reportDto.setFilePath(report.getFilePath());
        reportDto.setReportGenerationDate(report.getReportGenerationDate());
        reportDto.setGeneratedByUser(
                reservation.getStatusChangedByUser() != null
                        ? reservation.getStatusChangedByUser().getEmail()
                        : "Nie dotyczy"
        );
        return reportDto;
    }
}