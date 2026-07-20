package FYP.project_backend.analytics;

import FYP.project_backend.analytics.dto.AdminDashboardResponse;
import FYP.project_backend.analytics.dto.AnalyticsResponse;
import FYP.project_backend.analytics.dto.BusinessDashboardResponse;
import FYP.project_backend.analytics.dto.TouristDashboardResponse;
import FYP.project_backend.complaint.ComplaintRepository;
import FYP.project_backend.enums.*;
import FYP.project_backend.inspection.InspectionRepository;
import FYP.project_backend.license.LicenseRepository;
import FYP.project_backend.payment.Payment;
import FYP.project_backend.payment.PaymentRepository;
import FYP.project_backend.user.User;
import FYP.project_backend.user.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.Map;

/*==========================
        OPEN PDF
==========================*/

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/*==========================
      APACHE POI
==========================*/

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AnalyticsController {

    private final UserRepository userRepository;

    private final LicenseRepository licenseRepository;

    private final InspectionRepository inspectionRepository;

    private final ComplaintRepository complaintRepository;

    private final PaymentRepository paymentRepository;

    //=====================================================
    // ADMIN DASHBOARD
    //=====================================================

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardResponse adminDashboard(){

        BigDecimal revenue = paymentRepository

                .findByStatus(PaymentStatus.APPROVED)

                .stream()

                .map(Payment::getAmount)

                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AdminDashboardResponse.builder()

                .totalUsers(userRepository.count())

                .totalBusinessOwners(
                        userRepository.countByRole(Role.BUSINESS_OWNER)
                )

                .totalTourists(
                        userRepository.countByRole(Role.TOURIST)
                )

                .totalLicenses(
                        licenseRepository.count()
                )

                .approvedLicenses(
                        licenseRepository.countByStatus(
                                LicenseStatus.APPROVED)
                )

                .pendingLicenses(
                        licenseRepository.countByStatus(
                                LicenseStatus.PENDING)
                )

                .rejectedLicenses(
                        licenseRepository.countByStatus(
                                LicenseStatus.REJECTED)
                )

                .totalInspections(
                        inspectionRepository.count()
                )

                .passedInspections(
                        inspectionRepository.countByStatus(
                                InspectionStatus.PASSED)
                )

                .failedInspections(
                        inspectionRepository.countByStatus(
                                InspectionStatus.FAILED)
                )

                .pendingInspections(
                        inspectionRepository.countByStatus(
                                InspectionStatus.PENDING)
                )

                .totalComplaints(
                        complaintRepository.count()
                )

                .resolvedComplaints(
                        complaintRepository.countByStatus(
                                ComplaintStatus.RESOLVED)
                )

                .pendingComplaints(
                        complaintRepository.countByStatus(
                                ComplaintStatus.PENDING)
                )

                .rejectedComplaints(
                        complaintRepository.countByStatus(
                                ComplaintStatus.REJECTED)
                )

                .totalPayments(
                        paymentRepository.count()
                )

                .approvedPayments(
                        paymentRepository.countByStatus(
                                PaymentStatus.APPROVED)
                )

                .pendingPayments(
                        paymentRepository.countByStatus(
                                PaymentStatus.PENDING)
                )

                .rejectedPayments(
                        paymentRepository.countByStatus(
                                PaymentStatus.REJECTED)
                )

                .totalRevenue(revenue)

                .build();

    }

    //=====================================================
    // BUSINESS OWNER DASHBOARD
    //=====================================================

    @GetMapping("/business-owner")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public BusinessDashboardResponse businessDashboard(){

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User owner = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow();

        BigDecimal totalPaid = paymentRepository

                .findByOwner(owner)

                .stream()

                .filter(payment ->
                        payment.getStatus()==PaymentStatus.APPROVED)

                .map(Payment::getAmount)

                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BusinessDashboardResponse.builder()

                .myLicenses(
                        licenseRepository.findByOwner(owner).size()
                )

                .approvedLicenses(
                        licenseRepository.findByOwner(owner)

                                .stream()

                                .filter(l->l.getStatus()==LicenseStatus.APPROVED)

                                .count()
                )

                .pendingLicenses(
                        licenseRepository.findByOwner(owner)

                                .stream()

                                .filter(l->l.getStatus()==LicenseStatus.PENDING)

                                .count()
                )

                .rejectedLicenses(
                        licenseRepository.findByOwner(owner)

                                .stream()

                                .filter(l->l.getStatus()==LicenseStatus.REJECTED)

                                .count()
                )

                .myPayments(
                        paymentRepository.findByOwner(owner).size()
                )

                .approvedPayments(
                        paymentRepository.findByOwner(owner)

                                .stream()

                                .filter(p->p.getStatus()==PaymentStatus.APPROVED)

                                .count()
                )

                .pendingPayments(
                        paymentRepository.findByOwner(owner)

                                .stream()

                                .filter(p->p.getStatus()==PaymentStatus.PENDING)

                                .count()
                )

                .totalPaid(totalPaid)

                .build();

    }

    //=====================================================
    // TOURIST DASHBOARD
    //=====================================================

    @GetMapping("/tourist")
    @PreAuthorize("hasRole('TOURIST')")
    public TouristDashboardResponse touristDashboard(){

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User tourist = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow();

        return TouristDashboardResponse.builder()

                .myComplaints(
                        complaintRepository.findByReportedBy(tourist).size()
                )

                .resolvedComplaints(
                        complaintRepository.findByReportedBy(tourist)

                                .stream()

                                .filter(c->c.getStatus()==ComplaintStatus.RESOLVED)

                                .count()
                )

                .pendingComplaints(
                        complaintRepository.findByReportedBy(tourist)

                                .stream()

                                .filter(c->c.getStatus()==ComplaintStatus.PENDING)

                                .count()
                )

                .rejectedComplaints(
                        complaintRepository.findByReportedBy(tourist)

                                .stream()

                                .filter(c->c.getStatus()==ComplaintStatus.REJECTED)

                                .count()
                )

                .build();

    }

    //=====================================================
    // REPORTS
    //=====================================================

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public AnalyticsResponse reports(){

        BigDecimal revenue = paymentRepository

                .findByStatus(PaymentStatus.APPROVED)

                .stream()

                .map(Payment::getAmount)

                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AnalyticsResponse.builder()

                .approvedLicenses(
                        licenseRepository.countByStatus(LicenseStatus.APPROVED)
                )

                .pendingLicenses(
                        licenseRepository.countByStatus(LicenseStatus.PENDING)
                )

                .rejectedLicenses(
                        licenseRepository.countByStatus(LicenseStatus.REJECTED)
                )

                .passedInspections(
                        inspectionRepository.countByStatus(InspectionStatus.PASSED)
                )

                .failedInspections(
                        inspectionRepository.countByStatus(InspectionStatus.FAILED)
                )

                .pendingInspections(
                        inspectionRepository.countByStatus(InspectionStatus.PENDING)
                )

                .approvedPayments(
                        paymentRepository.countByStatus(PaymentStatus.APPROVED)
                )

                .pendingPayments(
                        paymentRepository.countByStatus(PaymentStatus.PENDING)
                )

                .rejectedPayments(
                        paymentRepository.countByStatus(PaymentStatus.REJECTED)
                )

                .resolvedComplaints(
                        complaintRepository.countByStatus(ComplaintStatus.RESOLVED)
                )

                .pendingComplaints(
                        complaintRepository.countByStatus(ComplaintStatus.PENDING)
                )

                .rejectedComplaints(
                        complaintRepository.countByStatus(ComplaintStatus.REJECTED)
                )

                .totalRevenue(revenue)

                .build();

    }

    @GetMapping("/monthly-revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, BigDecimal> monthlyRevenue() {

        Map<String, BigDecimal> revenue = new LinkedHashMap<>();

        for (Month month : Month.values()) {

            BigDecimal total = paymentRepository.findByStatus(PaymentStatus.APPROVED)

                    .stream()

                    .filter(payment ->
                            payment.getPaymentDate() != null &&
                                    payment.getPaymentDate().getMonth() == month)

                    .map(Payment::getAmount)

                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            revenue.put(month.name(), total);

        }

        return revenue;

    }

    @GetMapping("/monthly-licenses")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> monthlyLicenses() {

        Map<String, Long> licenses = new LinkedHashMap<>();

        for (Month month : Month.values()) {

            long total = licenseRepository.findAll()

                    .stream()

                    .filter(license ->
                            license.getCreatedAt() != null &&
                                    license.getCreatedAt().getMonth() == month)

                    .count();

            licenses.put(month.name(), total);

        }

        return licenses;

    }

    @GetMapping("/monthly-complaints")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> monthlyComplaints() {

        Map<String, Long> complaints = new LinkedHashMap<>();

        for (Month month : Month.values()) {

            long total = complaintRepository.findAll()

                    .stream()

                    .filter(complaint ->
                            complaint.getReportedAt() != null &&
                                    complaint.getReportedAt().getMonth() == month)

                    .count();

            complaints.put(month.name(), total);

        }

        return complaints;

    }

    @GetMapping("/export/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportPdf() throws Exception {

        AnalyticsResponse report = reports();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4,40,40,50,40);

        PdfWriter.getInstance(document,out);

        document.open();

        Font titleFont =
                FontFactory.getFont(FontFactory.HELVETICA_BOLD,20);

        Font headingFont =
                FontFactory.getFont(FontFactory.HELVETICA_BOLD,13);

        Font normalFont =
                FontFactory.getFont(FontFactory.HELVETICA,11);

        Font footerFont =
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE,9);

        Paragraph title = new Paragraph(
                "ICT-Based Coastal Conservation and Revenue Monitoring System",
                titleFont);

        title.setAlignment(Element.ALIGN_CENTER);

        document.add(title);

        Paragraph subTitle = new Paragraph(
                "Administrative Analytics Report",
                headingFont);

        subTitle.setAlignment(Element.ALIGN_CENTER);

        document.add(subTitle);

        document.add(new Paragraph(" "));

        document.add(new Paragraph(
                "Generated On : "
                        + LocalDateTime.now(),
                normalFont));

        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(2);

        table.setWidthPercentage(100);

        table.setSpacingBefore(10);

        table.setSpacingAfter(20);

        table.setWidths(new float[]{4,2});

        PdfPCell h1 = new PdfPCell(
                new Phrase("Description",headingFont));

        PdfPCell h2 = new PdfPCell(
                new Phrase("Value",headingFont));

        h1.setHorizontalAlignment(Element.ALIGN_CENTER);
        h2.setHorizontalAlignment(Element.ALIGN_CENTER);

        table.addCell(h1);
        table.addCell(h2);

        table.addCell("Total Revenue");
        table.addCell("TZS " + report.getTotalRevenue());

        table.addCell("Approved Licenses");
        table.addCell(String.valueOf(report.getApprovedLicenses()));

        table.addCell("Pending Licenses");
        table.addCell(String.valueOf(report.getPendingLicenses()));

        table.addCell("Rejected Licenses");
        table.addCell(String.valueOf(report.getRejectedLicenses()));

        table.addCell("Passed Inspections");
        table.addCell(String.valueOf(report.getPassedInspections()));

        table.addCell("Pending Inspections");
        table.addCell(String.valueOf(report.getPendingInspections()));

        table.addCell("Failed Inspections");
        table.addCell(String.valueOf(report.getFailedInspections()));

        table.addCell("Approved Payments");
        table.addCell(String.valueOf(report.getApprovedPayments()));

        table.addCell("Pending Payments");
        table.addCell(String.valueOf(report.getPendingPayments()));

        table.addCell("Rejected Payments");
        table.addCell(String.valueOf(report.getRejectedPayments()));

        table.addCell("Resolved Complaints");
        table.addCell(String.valueOf(report.getResolvedComplaints()));

        table.addCell("Pending Complaints");
        table.addCell(String.valueOf(report.getPendingComplaints()));

        table.addCell("Rejected Complaints");
        table.addCell(String.valueOf(report.getRejectedComplaints()));

        document.add(table);

        Paragraph summaryTitle = new Paragraph(
                "Executive Summary",
                headingFont);

        document.add(summaryTitle);

        document.add(new Paragraph(
                "This report summarizes the overall performance of the Coastal Conservation and Revenue Monitoring System. "
                        + "It includes revenue collection, licenses, inspections, payments and complaints handled during the reporting period.",
                normalFont));

        document.add(new Paragraph(" "));

        Paragraph footer = new Paragraph(
                "Generated automatically by Coastal Conservation and Revenue Monitoring System",
                footerFont);

        footer.setAlignment(Element.ALIGN_CENTER);

        document.add(footer);

        document.close();

        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Coastal_Analytics_Report.pdf")

                .contentType(MediaType.APPLICATION_PDF)

                .body(out.toByteArray());
    }


    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportExcel() throws Exception {

        AnalyticsResponse report = reports();

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Analytics Report");

        //====================================================
        // FONTS
        //====================================================

        org.apache.poi.ss.usermodel.Font titleFont =
                workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short)18);

        org.apache.poi.ss.usermodel.Font headerFont =
                workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        org.apache.poi.ss.usermodel.Font normalFont =
                workbook.createFont();
        normalFont.setFontHeightInPoints((short)11);

        //====================================================
        // STYLES
        //====================================================

        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(
                IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        CellStyle valueStyle = workbook.createCellStyle();
        valueStyle.setFont(normalFont);

        int rowIndex = 0;

        //====================================================
        // TITLE
        //====================================================

        Row titleRow = sheet.createRow(rowIndex++);

        Cell titleCell = titleRow.createCell(0);

        titleCell.setCellValue(
                "ICT-Based Coastal Conservation and Revenue Monitoring System");

        titleCell.setCellStyle(titleStyle);

        sheet.addMergedRegion(
                new org.apache.poi.ss.util.CellRangeAddress(
                        0,
                        0,
                        0,
                        1));

        rowIndex++;

        Row dateRow = sheet.createRow(rowIndex++);

        dateRow.createCell(0)
                .setCellValue("Generated On");

        dateRow.createCell(1)
                .setCellValue(LocalDateTime.now().toString());

        rowIndex++;

        //====================================================
        // HEADER
        //====================================================

        Row header = sheet.createRow(rowIndex++);

        Cell h1 = header.createCell(0);
        h1.setCellValue("Description");
        h1.setCellStyle(headerStyle);

        Cell h2 = header.createCell(1);
        h2.setCellValue("Value");
        h2.setCellStyle(headerStyle);

        //====================================================
        // DATA
        //====================================================

        Object[][] data = {

                {"Total Revenue",
                        "TZS " + report.getTotalRevenue()},

                {"Approved Licenses",
                        report.getApprovedLicenses()},

                {"Pending Licenses",
                        report.getPendingLicenses()},

                {"Rejected Licenses",
                        report.getRejectedLicenses()},

                {"Passed Inspections",
                        report.getPassedInspections()},

                {"Pending Inspections",
                        report.getPendingInspections()},

                {"Failed Inspections",
                        report.getFailedInspections()},

                {"Approved Payments",
                        report.getApprovedPayments()},

                {"Pending Payments",
                        report.getPendingPayments()},

                {"Rejected Payments",
                        report.getRejectedPayments()},

                {"Resolved Complaints",
                        report.getResolvedComplaints()},

                {"Pending Complaints",
                        report.getPendingComplaints()},

                {"Rejected Complaints",
                        report.getRejectedComplaints()}

        };

        for (Object[] item : data) {

            Row row = sheet.createRow(rowIndex++);

            Cell c1 = row.createCell(0);
            c1.setCellValue(item[0].toString());
            c1.setCellStyle(valueStyle);

            Cell c2 = row.createCell(1);
            c2.setCellValue(item[1].toString());
            c2.setCellStyle(valueStyle);

        }

        rowIndex++;

        Row footer = sheet.createRow(rowIndex);

        footer.createCell(0).setCellValue(
                "Generated automatically by Coastal Conservation and Revenue Monitoring System");

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        workbook.write(out);

        workbook.close();

        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Coastal_Analytics_Report.xlsx")

                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))

                .body(out.toByteArray());

    }

}