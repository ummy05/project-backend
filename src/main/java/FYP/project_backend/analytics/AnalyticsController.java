package FYP.project_backend.analytics;

import FYP.project_backend.analytics.dto.AdminDashboardResponse;
import FYP.project_backend.analytics.dto.AnalyticsResponse;
import FYP.project_backend.analytics.dto.BusinessDashboardResponse;
import FYP.project_backend.analytics.dto.TouristDashboardResponse;

import FYP.project_backend.complaint.ComplaintRepository;
import FYP.project_backend.enums.*;
import FYP.project_backend.inspection.InspectionRepository;
import FYP.project_backend.license.License;
import FYP.project_backend.license.LicenseRepository;
import FYP.project_backend.payment.Payment;
import FYP.project_backend.payment.PaymentRepository;
import FYP.project_backend.permit.Permit;
import FYP.project_backend.permit.PermitRepository;
import FYP.project_backend.user.User;
import FYP.project_backend.user.UserRepository;

import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

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

import java.io.ByteArrayOutputStream;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.time.Month;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AnalyticsController {


    // =====================================================
    // REPOSITORIES
    // =====================================================

    private final UserRepository userRepository;

    private final LicenseRepository licenseRepository;

    private final PermitRepository permitRepository;

    private final InspectionRepository inspectionRepository;

    private final ComplaintRepository complaintRepository;

    private final PaymentRepository paymentRepository;


    // =====================================================
    // ADMIN DASHBOARD
    // =====================================================

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardResponse adminDashboard() {


        // =================================================
        // REVENUE
        // =================================================

        BigDecimal totalRevenue =
                approvedPayments()
                        .stream()
                        .map(Payment::getAmount)
                        .filter(amount -> amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal permitRevenue =
                approvedPayments()
                        .stream()
                        .filter(payment ->
                                payment.getPermit() != null
                        )
                        .map(Payment::getAmount)
                        .filter(amount -> amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal licenseRevenue =
                approvedPayments()
                        .stream()
                        .filter(payment ->
                                payment.getLicense() != null
                        )
                        .map(Payment::getAmount)
                        .filter(amount -> amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =================================================
        // SHEHAS
        // =================================================

        long totalShehas =
                userRepository
                        .findAll()
                        .stream()
                        .filter(user ->
                                user.getRole() == Role.SHEHA
                        )
                        .count();


        // =================================================
        // SHEHIAS
        //
        // Shehia is stored on User.
        // We count unique non-empty Shehia names.
        // =================================================

        long totalShehias =
                userRepository
                        .findAll()
                        .stream()
                        .map(User::getShehia)
                        .filter(
                                shehia ->
                                        shehia != null &&
                                                !shehia.isBlank()
                        )
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .distinct()
                        .count();


        // =================================================
        // BUILD RESPONSE
        // =================================================

        return AdminDashboardResponse.builder()


                // USERS
                .totalUsers(
                        userRepository.count()
                )

                .totalBusinessOwners(
                        userRepository.countByRole(
                                Role.BUSINESS_OWNER
                        )
                )

                .totalTourists(
                        userRepository.countByRole(
                                Role.TOURIST
                        )
                )

                .totalShehas(totalShehas)

                .totalShehias(totalShehias)


                // LICENSES
                .totalLicenses(
                        licenseRepository.count()
                )

                .approvedLicenses(
                        licenseRepository.countByStatus(
                                LicenseStatus.APPROVED
                        )
                )

                .pendingLicenses(
                        licenseRepository.countByStatus(
                                LicenseStatus.PENDING
                        )
                )

                .rejectedLicenses(
                        licenseRepository.countByStatus(
                                LicenseStatus.REJECTED
                        )
                )


                // PERMITS
                .totalPermits(
                        permitRepository.count()
                )

                .issuedPermits(
                        permitRepository
                                .findAll()
                                .stream()
                                .filter(permit ->
                                        permit.getIssueDate() != null
                                )
                                .count()
                )

                .approvedPermits(
                        countPermitsByStatus(
                                PermitStatus.APPROVED
                        )
                )

                .pendingPermits(
                        countPermitsByStatus(
                                PermitStatus.PENDING
                        )
                )

                .waitingPaymentPermits(
                        countPermitsByStatus(
                                PermitStatus.WAITING_PAYMENT
                        )
                )

                .rejectedPermits(
                        countPermitsByStatus(
                                PermitStatus.REJECTED
                        )
                )


                // INSPECTIONS
                .totalInspections(
                        inspectionRepository.count()
                )

                .passedInspections(
                        inspectionRepository.countByStatus(
                                InspectionStatus.PASSED
                        )
                )

                .failedInspections(
                        inspectionRepository.countByStatus(
                                InspectionStatus.FAILED
                        )
                )

                .pendingInspections(
                        inspectionRepository.countByStatus(
                                InspectionStatus.PENDING
                        )
                )


                // COMPLAINTS
                .totalComplaints(
                        complaintRepository.count()
                )

                .resolvedComplaints(
                        complaintRepository.countByStatus(
                                ComplaintStatus.RESOLVED
                        )
                )

                .pendingComplaints(
                        complaintRepository.countByStatus(
                                ComplaintStatus.PENDING
                        )
                )

                .rejectedComplaints(
                        complaintRepository.countByStatus(
                                ComplaintStatus.REJECTED
                        )
                )


                // PAYMENTS
                .totalPayments(
                        paymentRepository.count()
                )

                .approvedPayments(
                        paymentRepository.countByStatus(
                                PaymentStatus.APPROVED
                        )
                )

                .pendingPayments(
                        paymentRepository.countByStatus(
                                PaymentStatus.PENDING
                        )
                )

                .rejectedPayments(
                        paymentRepository.countByStatus(
                                PaymentStatus.REJECTED
                        )
                )


                // REVENUE
                .totalRevenue(totalRevenue)

                .permitRevenue(permitRevenue)

                .licenseRevenue(licenseRevenue)


                .build();
    }


    // =====================================================
    // BUSINESS OWNER DASHBOARD
    // =====================================================

    @GetMapping("/business-owner")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public BusinessDashboardResponse businessDashboard() {


        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        User owner =
                userRepository
                        .findByEmail(
                                authentication.getName()
                        )
                        .orElseThrow();


        // =================================================
        // OWNER PAYMENTS
        // =================================================

        var ownerPayments =
                paymentRepository.findByOwner(owner);


        BigDecimal totalPaid =
                ownerPayments
                        .stream()
                        .filter(payment ->
                                payment.getStatus()
                                        == PaymentStatus.APPROVED
                        )
                        .map(Payment::getAmount)
                        .filter(amount -> amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =================================================
        // OWNER LICENSES
        // =================================================

        var ownerLicenses =
                licenseRepository.findByOwner(owner);


        // =================================================
        // OWNER PERMITS
        // =================================================

        var ownerPermits =
                permitRepository.findByOwner(owner);


        return BusinessDashboardResponse.builder()


                // LICENSES
                .myLicenses(
                        ownerLicenses.size()
                )

                .approvedLicenses(
                        ownerLicenses
                                .stream()
                                .filter(license ->
                                        license.getStatus()
                                                == LicenseStatus.APPROVED
                                )
                                .count()
                )

                .pendingLicenses(
                        ownerLicenses
                                .stream()
                                .filter(license ->
                                        license.getStatus()
                                                == LicenseStatus.PENDING
                                )
                                .count()
                )

                .rejectedLicenses(
                        ownerLicenses
                                .stream()
                                .filter(license ->
                                        license.getStatus()
                                                == LicenseStatus.REJECTED
                                )
                                .count()
                )


                // PERMITS
                .myPermits(
                        ownerPermits.size()
                )

                .approvedPermits(
                        ownerPermits
                                .stream()
                                .filter(permit ->
                                        permit.getStatus()
                                                == PermitStatus.APPROVED
                                )
                                .count()
                )

                .pendingPermits(
                        ownerPermits
                                .stream()
                                .filter(permit ->
                                        permit.getStatus()
                                                == PermitStatus.PENDING
                                )
                                .count()
                )

                .waitingPaymentPermits(
                        ownerPermits
                                .stream()
                                .filter(permit ->
                                        permit.getStatus()
                                                == PermitStatus.WAITING_PAYMENT
                                )
                                .count()
                )

                .rejectedPermits(
                        ownerPermits
                                .stream()
                                .filter(permit ->
                                        permit.getStatus()
                                                == PermitStatus.REJECTED
                                )
                                .count()
                )


                // PAYMENTS
                .myPayments(
                        ownerPayments.size()
                )

                .approvedPayments(
                        ownerPayments
                                .stream()
                                .filter(payment ->
                                        payment.getStatus()
                                                == PaymentStatus.APPROVED
                                )
                                .count()
                )

                .pendingPayments(
                        ownerPayments
                                .stream()
                                .filter(payment ->
                                        payment.getStatus()
                                                == PaymentStatus.PENDING
                                )
                                .count()
                )


                // REVENUE
                .totalPaid(totalPaid)


                .build();
    }


    // =====================================================
    // TOURIST DASHBOARD
    // =====================================================

    @GetMapping("/tourist")
    @PreAuthorize("hasRole('TOURIST')")
    public TouristDashboardResponse touristDashboard() {


        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        User tourist =
                userRepository
                        .findByEmail(
                                authentication.getName()
                        )
                        .orElseThrow();


        var complaints =
                complaintRepository
                        .findByReportedBy(tourist);


        var permits =
                permitRepository
                        .findByOwner(tourist);


        return TouristDashboardResponse.builder()


                // COMPLAINTS
                .myComplaints(
                        complaints.size()
                )

                .resolvedComplaints(
                        complaints
                                .stream()
                                .filter(c ->
                                        c.getStatus()
                                                == ComplaintStatus.RESOLVED
                                )
                                .count()
                )

                .pendingComplaints(
                        complaints
                                .stream()
                                .filter(c ->
                                        c.getStatus()
                                                == ComplaintStatus.PENDING
                                )
                                .count()
                )

                .rejectedComplaints(
                        complaints
                                .stream()
                                .filter(c ->
                                        c.getStatus()
                                                == ComplaintStatus.REJECTED
                                )
                                .count()
                )


                // PERMITS
                .myPermits(
                        permits.size()
                )

                .approvedPermits(
                        permits
                                .stream()
                                .filter(p ->
                                        p.getStatus()
                                                == PermitStatus.APPROVED
                                )
                                .count()
                )

                .pendingPermits(
                        permits
                                .stream()
                                .filter(p ->
                                        p.getStatus()
                                                == PermitStatus.PENDING
                                )
                                .count()
                )

                .waitingPaymentPermits(
                        permits
                                .stream()
                                .filter(p ->
                                        p.getStatus()
                                                == PermitStatus.WAITING_PAYMENT
                                )
                                .count()
                )

                .rejectedPermits(
                        permits
                                .stream()
                                .filter(p ->
                                        p.getStatus()
                                                == PermitStatus.REJECTED
                                )
                                .count()
                )


                .build();
    }


    // =====================================================
    // ADMIN REPORT
    // =====================================================

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public AnalyticsResponse reports() {


        BigDecimal totalRevenue =
                approvedPayments()
                        .stream()
                        .map(Payment::getAmount)
                        .filter(amount -> amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal permitRevenue =
                approvedPayments()
                        .stream()
                        .filter(payment ->
                                payment.getPermit() != null
                        )
                        .map(Payment::getAmount)
                        .filter(amount -> amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal licenseRevenue =
                approvedPayments()
                        .stream()
                        .filter(payment ->
                                payment.getLicense() != null
                        )
                        .map(Payment::getAmount)
                        .filter(amount -> amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        long totalShehas =
                userRepository
                        .findAll()
                        .stream()
                        .filter(user ->
                                user.getRole() == Role.SHEHA
                        )
                        .count();


        long totalShehias =
                userRepository
                        .findAll()
                        .stream()
                        .map(User::getShehia)
                        .filter(
                                shehia ->
                                        shehia != null &&
                                                !shehia.isBlank()
                        )
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .distinct()
                        .count();


        return AnalyticsResponse.builder()


                // LICENSES
                .totalLicenses(
                        licenseRepository.count()
                )

                .approvedLicenses(
                        licenseRepository.countByStatus(
                                LicenseStatus.APPROVED
                        )
                )

                .pendingLicenses(
                        licenseRepository.countByStatus(
                                LicenseStatus.PENDING
                        )
                )

                .rejectedLicenses(
                        licenseRepository.countByStatus(
                                LicenseStatus.REJECTED
                        )
                )


                // PERMITS
                .totalPermits(
                        permitRepository.count()
                )

                .issuedPermits(
                        permitRepository
                                .findAll()
                                .stream()
                                .filter(permit ->
                                        permit.getIssueDate() != null
                                )
                                .count()
                )

                .approvedPermits(
                        countPermitsByStatus(
                                PermitStatus.APPROVED
                        )
                )

                .pendingPermits(
                        countPermitsByStatus(
                                PermitStatus.PENDING
                        )
                )

                .waitingPaymentPermits(
                        countPermitsByStatus(
                                PermitStatus.WAITING_PAYMENT
                        )
                )

                .rejectedPermits(
                        countPermitsByStatus(
                                PermitStatus.REJECTED
                        )
                )


                // INSPECTIONS
                .totalInspections(
                        inspectionRepository.count()
                )

                .passedInspections(
                        inspectionRepository.countByStatus(
                                InspectionStatus.PASSED
                        )
                )

                .failedInspections(
                        inspectionRepository.countByStatus(
                                InspectionStatus.FAILED
                        )
                )

                .pendingInspections(
                        inspectionRepository.countByStatus(
                                InspectionStatus.PENDING
                        )
                )


                // PAYMENTS
                .totalPayments(
                        paymentRepository.count()
                )

                .approvedPayments(
                        paymentRepository.countByStatus(
                                PaymentStatus.APPROVED
                        )
                )

                .pendingPayments(
                        paymentRepository.countByStatus(
                                PaymentStatus.PENDING
                        )
                )

                .rejectedPayments(
                        paymentRepository.countByStatus(
                                PaymentStatus.REJECTED
                        )
                )


                // COMPLAINTS
                .resolvedComplaints(
                        complaintRepository.countByStatus(
                                ComplaintStatus.RESOLVED
                        )
                )

                .pendingComplaints(
                        complaintRepository.countByStatus(
                                ComplaintStatus.PENDING
                        )
                )

                .rejectedComplaints(
                        complaintRepository.countByStatus(
                                ComplaintStatus.REJECTED
                        )
                )


                // SHEHA / SHEHIA
                .totalShehas(totalShehas)

                .totalShehias(totalShehias)


                // REVENUE
                .totalRevenue(totalRevenue)

                .permitRevenue(permitRevenue)

                .licenseRevenue(licenseRevenue)


                .build();
    }


    // =====================================================
    // MONTHLY REVENUE
    // =====================================================

    @GetMapping("/monthly-revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, BigDecimal> monthlyRevenue() {


        Map<String, BigDecimal> revenue =
                new LinkedHashMap<>();


        var payments =
                approvedPayments();


        for (Month month : Month.values()) {


            BigDecimal total =
                    payments
                            .stream()
                            .filter(payment ->
                                    payment.getPaymentDate() != null
                                            &&
                                            payment.getPaymentDate()
                                                    .getMonth()
                                                    == month
                            )
                            .map(Payment::getAmount)
                            .filter(amount ->
                                    amount != null
                            )
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );


            revenue.put(
                    month.name(),
                    total
            );
        }


        return revenue;
    }


    // =====================================================
    // MONTHLY LICENSES
    // =====================================================

    @GetMapping("/monthly-licenses")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> monthlyLicenses() {


        Map<String, Long> licenses =
                new LinkedHashMap<>();


        var allLicenses =
                licenseRepository.findAll();


        for (Month month : Month.values()) {


            long total =
                    allLicenses
                            .stream()
                            .filter(license ->
                                    license.getCreatedAt() != null
                                            &&
                                            license.getCreatedAt()
                                                    .getMonth()
                                                    == month
                            )
                            .count();


            licenses.put(
                    month.name(),
                    total
            );
        }


        return licenses;
    }


    // =====================================================
    // MONTHLY PERMITS
    // =====================================================

    @GetMapping("/monthly-permits")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> monthlyPermits() {


        Map<String, Long> permits =
                new LinkedHashMap<>();


        var allPermits =
                permitRepository.findAll();


        for (Month month : Month.values()) {


            long total =
                    allPermits
                            .stream()
                            .filter(permit ->
                                    permit.getCreatedAt() != null
                                            &&
                                            permit.getCreatedAt()
                                                    .getMonth()
                                                    == month
                            )
                            .count();


            permits.put(
                    month.name(),
                    total
            );
        }


        return permits;
    }


    // =====================================================
    // MONTHLY COMPLAINTS
    // =====================================================

    @GetMapping("/monthly-complaints")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> monthlyComplaints() {


        Map<String, Long> complaints =
                new LinkedHashMap<>();


        var allComplaints =
                complaintRepository.findAll();


        for (Month month : Month.values()) {


            long total =
                    allComplaints
                            .stream()
                            .filter(complaint ->
                                    complaint.getReportedAt() != null
                                            &&
                                            complaint.getReportedAt()
                                                    .getMonth()
                                                    == month
                            )
                            .count();


            complaints.put(
                    month.name(),
                    total
            );
        }


        return complaints;
    }


    // =====================================================
    // MONTHLY PERMIT REVENUE
    // =====================================================

    @GetMapping("/monthly-permit-revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, BigDecimal> monthlyPermitRevenue() {


        Map<String, BigDecimal> revenue =
                new LinkedHashMap<>();


        var payments =
                approvedPayments();


        for (Month month : Month.values()) {


            BigDecimal total =
                    payments
                            .stream()
                            .filter(payment ->
                                    payment.getPermit() != null
                                            &&
                                            payment.getPaymentDate() != null
                                            &&
                                            payment.getPaymentDate()
                                                    .getMonth()
                                                    == month
                            )
                            .map(Payment::getAmount)
                            .filter(amount ->
                                    amount != null
                            )
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );


            revenue.put(
                    month.name(),
                    total
            );
        }


        return revenue;
    }


    // =====================================================
    // EXPORT PDF
    // =====================================================

    @GetMapping("/export/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportPdf()
            throws Exception {


        AnalyticsResponse report =
                reports();


        ByteArrayOutputStream out =
                new ByteArrayOutputStream();


        Document document =
                new Document(
                        PageSize.A4,
                        40,
                        40,
                        50,
                        40
                );


        PdfWriter.getInstance(
                document,
                out
        );


        document.open();


        Font titleFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        20
                );


        Font headingFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        13
                );


        Font normalFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        11
                );


        Font footerFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_OBLIQUE,
                        9
                );


        Paragraph title =
                new Paragraph(
                        "ICT-Based Coastal Conservation and Revenue Monitoring System",
                        titleFont
                );


        title.setAlignment(
                Element.ALIGN_CENTER
        );


        document.add(title);


        Paragraph subTitle =
                new Paragraph(
                        "Administrative Analytics Report",
                        headingFont
                );


        subTitle.setAlignment(
                Element.ALIGN_CENTER
        );


        document.add(subTitle);


        document.add(
                new Paragraph(" ")
        );


        document.add(
                new Paragraph(
                        "Generated On : "
                                + LocalDateTime.now(),
                        normalFont
                )
        );


        document.add(
                new Paragraph(" ")
        );


        PdfPTable table =
                new PdfPTable(2);


        table.setWidthPercentage(100);

        table.setSpacingBefore(10);

        table.setSpacingAfter(20);

        table.setWidths(
                new float[]{4, 2}
        );


        PdfPCell h1 =
                new PdfPCell(
                        new Phrase(
                                "Description",
                                headingFont
                        )
                );


        PdfPCell h2 =
                new PdfPCell(
                        new Phrase(
                                "Value",
                                headingFont
                        )
                );


        h1.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );


        h2.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );


        table.addCell(h1);

        table.addCell(h2);


        // =================================================
        // REVENUE
        // =================================================

        addPdfRow(
                table,
                "Total Revenue",
                "TZS " + report.getTotalRevenue()
        );


        addPdfRow(
                table,
                "License Revenue",
                "TZS " + report.getLicenseRevenue()
        );


        addPdfRow(
                table,
                "Permit Revenue",
                "TZS " + report.getPermitRevenue()
        );


        // =================================================
        // LICENSES
        // =================================================

        addPdfRow(
                table,
                "Total Licenses",
                report.getTotalLicenses()
        );


        addPdfRow(
                table,
                "Approved Licenses",
                report.getApprovedLicenses()
        );


        addPdfRow(
                table,
                "Pending Licenses",
                report.getPendingLicenses()
        );


        addPdfRow(
                table,
                "Rejected Licenses",
                report.getRejectedLicenses()
        );


        // =================================================
        // PERMITS
        // =================================================

        addPdfRow(
                table,
                "Total Permits",
                report.getTotalPermits()
        );


        addPdfRow(
                table,
                "Issued Permits",
                report.getIssuedPermits()
        );


        addPdfRow(
                table,
                "Approved Permits",
                report.getApprovedPermits()
        );


        addPdfRow(
                table,
                "Pending Permits",
                report.getPendingPermits()
        );


        addPdfRow(
                table,
                "Waiting Payment Permits",
                report.getWaitingPaymentPermits()
        );


        addPdfRow(
                table,
                "Rejected Permits",
                report.getRejectedPermits()
        );


        // =================================================
        // SHEHA / SHEHIA
        // =================================================

        addPdfRow(
                table,
                "Total Shehas",
                report.getTotalShehas()
        );


        addPdfRow(
                table,
                "Total Shehias",
                report.getTotalShehias()
        );


        // =================================================
        // INSPECTIONS
        // =================================================

        addPdfRow(
                table,
                "Total Inspections",
                report.getTotalInspections()
        );


        addPdfRow(
                table,
                "Passed Inspections",
                report.getPassedInspections()
        );


        addPdfRow(
                table,
                "Pending Inspections",
                report.getPendingInspections()
        );


        addPdfRow(
                table,
                "Failed Inspections",
                report.getFailedInspections()
        );


        // =================================================
        // PAYMENTS
        // =================================================

        addPdfRow(
                table,
                "Total Payments",
                report.getTotalPayments()
        );


        addPdfRow(
                table,
                "Approved Payments",
                report.getApprovedPayments()
        );


        addPdfRow(
                table,
                "Pending Payments",
                report.getPendingPayments()
        );


        addPdfRow(
                table,
                "Rejected Payments",
                report.getRejectedPayments()
        );


        // =================================================
        // COMPLAINTS
        // =================================================

        addPdfRow(
                table,
                "Resolved Complaints",
                report.getResolvedComplaints()
        );


        addPdfRow(
                table,
                "Pending Complaints",
                report.getPendingComplaints()
        );


        addPdfRow(
                table,
                "Rejected Complaints",
                report.getRejectedComplaints()
        );


        document.add(table);


        document.add(
                new Paragraph(" ")
        );


        document.add(
                new Paragraph(
                        "Executive Summary",
                        headingFont
                )
        );


        document.add(
                new Paragraph(
                        "This report summarizes the overall performance of the "
                                + "Coastal Conservation and Revenue Monitoring System. "
                                + "It includes users, Shehas, Shehias, licenses, permits, "
                                + "inspections, complaints, payments and revenue collection.",
                        normalFont
                )
        );


        document.add(
                new Paragraph(" ")
        );


        Paragraph footer =
                new Paragraph(
                        "Generated automatically by Coastal Conservation and Revenue Monitoring System",
                        footerFont
                );


        footer.setAlignment(
                Element.ALIGN_CENTER
        );


        document.add(footer);


        document.close();


        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Coastal_Analytics_Report.pdf"
                )

                .contentType(
                        MediaType.APPLICATION_PDF
                )

                .body(
                        out.toByteArray()
                );
    }


    // =====================================================
    // EXPORT EXCEL
    // =====================================================

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportExcel()
            throws Exception {


        AnalyticsResponse report =
                reports();


        Workbook workbook =
                new XSSFWorkbook();


        Sheet sheet =
                workbook.createSheet(
                        "Analytics Report"
                );


        // =================================================
        // FONTS
        // =================================================

        org.apache.poi.ss.usermodel.Font titleFont =
                workbook.createFont();


        titleFont.setBold(true);

        titleFont.setFontHeightInPoints(
                (short) 18
        );


        org.apache.poi.ss.usermodel.Font headerFont =
                workbook.createFont();


        headerFont.setBold(true);

        headerFont.setColor(
                IndexedColors.WHITE.getIndex()
        );


        org.apache.poi.ss.usermodel.Font normalFont =
                workbook.createFont();


        normalFont.setFontHeightInPoints(
                (short) 11
        );


        // =================================================
        // STYLES
        // =================================================

        CellStyle titleStyle =
                workbook.createCellStyle();


        titleStyle.setFont(titleFont);

        titleStyle.setAlignment(
                HorizontalAlignment.CENTER
        );


        CellStyle headerStyle =
                workbook.createCellStyle();


        headerStyle.setFont(headerFont);

        headerStyle.setFillForegroundColor(
                IndexedColors.DARK_BLUE.getIndex()
        );

        headerStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        headerStyle.setBorderBottom(
                BorderStyle.THIN
        );


        CellStyle valueStyle =
                workbook.createCellStyle();


        valueStyle.setFont(normalFont);


        int rowIndex = 0;


        // =================================================
        // TITLE
        // =================================================

        Row titleRow =
                sheet.createRow(
                        rowIndex++
                );


        Cell titleCell =
                titleRow.createCell(0);


        titleCell.setCellValue(
                "ICT-Based Coastal Conservation and Revenue Monitoring System"
        );


        titleCell.setCellStyle(
                titleStyle
        );


        sheet.addMergedRegion(
                new org.apache.poi.ss.util.CellRangeAddress(
                        0,
                        0,
                        0,
                        1
                )
        );


        rowIndex++;


        Row dateRow =
                sheet.createRow(
                        rowIndex++
                );


        dateRow.createCell(0)
                .setCellValue(
                        "Generated On"
                );


        dateRow.createCell(1)
                .setCellValue(
                        LocalDateTime.now().toString()
                );


        rowIndex++;


        // =================================================
        // HEADER
        // =================================================

        Row header =
                sheet.createRow(
                        rowIndex++
                );


        Cell h1 =
                header.createCell(0);


        h1.setCellValue(
                "Description"
        );


        h1.setCellStyle(
                headerStyle
        );


        Cell h2 =
                header.createCell(1);


        h2.setCellValue(
                "Value"
        );


        h2.setCellStyle(
                headerStyle
        );


        // =================================================
        // DATA
        // =================================================

        Object[][] data = {


                // Revenue
                {
                        "Total Revenue",
                        "TZS " + report.getTotalRevenue()
                },

                {
                        "License Revenue",
                        "TZS " + report.getLicenseRevenue()
                },

                {
                        "Permit Revenue",
                        "TZS " + report.getPermitRevenue()
                },


                // Licenses
                {
                        "Total Licenses",
                        report.getTotalLicenses()
                },

                {
                        "Approved Licenses",
                        report.getApprovedLicenses()
                },

                {
                        "Pending Licenses",
                        report.getPendingLicenses()
                },

                {
                        "Rejected Licenses",
                        report.getRejectedLicenses()
                },


                // Permits
                {
                        "Total Permits",
                        report.getTotalPermits()
                },

                {
                        "Issued Permits",
                        report.getIssuedPermits()
                },

                {
                        "Approved Permits",
                        report.getApprovedPermits()
                },

                {
                        "Pending Permits",
                        report.getPendingPermits()
                },

                {
                        "Waiting Payment Permits",
                        report.getWaitingPaymentPermits()
                },

                {
                        "Rejected Permits",
                        report.getRejectedPermits()
                },


                // Sheha / Shehia
                {
                        "Total Shehas",
                        report.getTotalShehas()
                },

                {
                        "Total Shehias",
                        report.getTotalShehias()
                },


                // Inspections
                {
                        "Total Inspections",
                        report.getTotalInspections()
                },

                {
                        "Passed Inspections",
                        report.getPassedInspections()
                },

                {
                        "Pending Inspections",
                        report.getPendingInspections()
                },

                {
                        "Failed Inspections",
                        report.getFailedInspections()
                },


                // Payments
                {
                        "Total Payments",
                        report.getTotalPayments()
                },

                {
                        "Approved Payments",
                        report.getApprovedPayments()
                },

                {
                        "Pending Payments",
                        report.getPendingPayments()
                },

                {
                        "Rejected Payments",
                        report.getRejectedPayments()
                },


                // Complaints
                {
                        "Resolved Complaints",
                        report.getResolvedComplaints()
                },

                {
                        "Pending Complaints",
                        report.getPendingComplaints()
                },

                {
                        "Rejected Complaints",
                        report.getRejectedComplaints()
                }

        };


        for (Object[] item : data) {


            Row row =
                    sheet.createRow(
                            rowIndex++
                    );


            Cell c1 =
                    row.createCell(0);


            c1.setCellValue(
                    item[0].toString()
            );


            c1.setCellStyle(
                    valueStyle
            );


            Cell c2 =
                    row.createCell(1);


            c2.setCellValue(
                    item[1].toString()
            );


            c2.setCellStyle(
                    valueStyle
            );
        }


        rowIndex++;


        Row footer =
                sheet.createRow(
                        rowIndex
                );


        footer.createCell(0)
                .setCellValue(
                        "Generated automatically by Coastal Conservation and Revenue Monitoring System"
                );


        sheet.autoSizeColumn(0);

        sheet.autoSizeColumn(1);


        ByteArrayOutputStream out =
                new ByteArrayOutputStream();


        workbook.write(out);

        workbook.close();


        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Coastal_Analytics_Report.xlsx"
                )

                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )

                .body(
                        out.toByteArray()
                );
    }


    // =====================================================
    // HELPER:
    // APPROVED PAYMENTS
    // =====================================================

    private java.util.List<Payment> approvedPayments() {

        return paymentRepository
                .findByStatus(
                        PaymentStatus.APPROVED
                );
    }


    // =====================================================
    // HELPER:
    // COUNT PERMITS BY STATUS
    // =====================================================

    private long countPermitsByStatus(
            PermitStatus status
    ) {

        return permitRepository
                .findAll()
                .stream()
                .filter(permit ->
                        permit.getStatus() == status
                )
                .count();
    }


    // =====================================================
    // HELPER:
    // PDF ROW
    // =====================================================

    private void addPdfRow(
            PdfPTable table,
            String label,
            Object value
    ) {

        table.addCell(label);

        table.addCell(
                String.valueOf(value)
        );
    }

}