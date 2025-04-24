package org.example.magazyn.controller;

import org.example.magazyn.entity.Zone;
import org.example.magazyn.repository.ProductRepository;
import org.example.magazyn.repository.ZoneRepository;
import org.example.magazyn.service.ReportTABService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reports")
public class ReportTABController {

    @Autowired
    private ReportTABService reportTABService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @GetMapping
    public String showReportsPage(Model model) {
        List<String> categories = productRepository.findAll().stream()
                .map(product -> product.getCategory())
                .distinct()
                .collect(Collectors.toList());

        List<String> brands = productRepository.findAll().stream()
                .map(product -> product.getBrand())
                .distinct()
                .collect(Collectors.toList());

        List<Zone> zones = zoneRepository.findAll();

        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);
        model.addAttribute("zones", zones);

        return "reports";
    }

    @PostMapping("/profit")
    public ResponseEntity<Resource> generateProfitReport(
            @RequestParam(required = false) String productCategory,
            @RequestParam(required = false) Long warehouseZone,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        byte[] pdfContent = reportTABService.generateProfitReport(productCategory, warehouseZone, start, end);

        ByteArrayResource resource = new ByteArrayResource(pdfContent);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=raport_zysku_" +
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfContent.length)
                .body(resource);
    }

    @PostMapping("/reservations")
    public ResponseEntity<Resource> generateReservationsReport(
            @RequestParam(required = false) String productCategory,
            @RequestParam(required = false) String productBrand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(value = "aggregation", required = false) List<String> aggregations) {

        boolean includeTotal = aggregations != null && aggregations.contains("totalReservations");
        boolean includeConfirmed = aggregations != null && aggregations.contains("confirmedReservations");
        boolean includeCancelled = aggregations != null && aggregations.contains("cancelledReservations");
        boolean includeTurnover = aggregations != null && aggregations.contains("turnover");

        byte[] pdfContent = reportTABService.generateReservationsReport(
                productCategory, productBrand, minPrice, maxPrice,
                includeTotal, includeConfirmed, includeCancelled, includeTurnover);

        ByteArrayResource resource = new ByteArrayResource(pdfContent);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=raport_rezerwacji_" +
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfContent.length)
                .body(resource);
    }
}