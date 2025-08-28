package ua.dymohlo.notification_service.strategy;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import ua.dymohlo.notification_service.dto.request.NotificationRequest;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationStrategy implements NotificationStrategy {

    private final JavaMailSender mailSender;

    @Value("${notification.email.company-name:AudioCloud}")
    private String companyName;

    @Value("${notification.email.from-name:AudioCloud Team}")
    private String fromName;

    @Value("${notification.pdf.title:PAYMENT REPORT}")
    private String pdfTitle;

    @Value("${notification.pdf.title-font-size:20}")
    private int titleFontSize;

    @Value("${notification.pdf.details-font-size:10}")
    private int detailsFontSize;

    @Value("${notification.date.format:dd/MM/yyyy HH:mm}")
    private String dateFormat;

    // PDF Templates
    @Value("${notification.pdf.transaction-label:Transaction ID: }")
    private String transactionLabel;

    @Value("${notification.pdf.status-label:Status: }")
    private String statusLabel;

    @Value("${notification.pdf.message-label:Message: }")
    private String messageLabel;

    @Value("${notification.pdf.date-label:Date: }")
    private String dateLabel;

    @Value("${notification.pdf.details-label:Payment Details:}")
    private String detailsLabel;

    // Email Templates
    @Value("${notification.email.subject-template:Payment Report - %s}")
    private String emailSubjectTemplate;

    @Value("${notification.email.body-template:Dear Customer,\n\nPlease find attached your payment report.\n\nTransaction ID: %s\nStatus: %s\n\nBest regards,\n%s}")
    private String emailBodyTemplate;

    @Override
    public void sendNotification(NotificationRequest request) {
        log.info("Generating PDF and sending email to: {}", request.getRecipient());

        try {
            byte[] pdfBytes = generatePaymentReportPdf(request);
            sendEmailWithPdf(request, pdfBytes);
            log.info("Payment report sent successfully to: {}", request.getRecipient());

        } catch (Exception e) {
            log.error("Failed to send payment report to: {}", request.getRecipient(), e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    private byte[] generatePaymentReportPdf(NotificationRequest request) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph(pdfTitle).setBold().setFontSize(titleFontSize));
            document.add(new Paragraph(" "));

            document.add(new Paragraph(transactionLabel + request.getTransactionId()));
            document.add(new Paragraph(statusLabel + getStatusText(request.isSuccess())));
            document.add(new Paragraph(messageLabel + request.getMessage()));
            document.add(new Paragraph(dateLabel + getCurrentFormattedDate()));

            if (request.getPaymentData() != null && !request.getPaymentData().trim().isEmpty()) {
                document.add(new Paragraph(" "));
                document.add(new Paragraph(detailsLabel));
                document.add(new Paragraph(request.getPaymentData()).setFontSize(detailsFontSize));
            }

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF", e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void sendEmailWithPdf(NotificationRequest request, byte[] pdfBytes) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(request.getRecipient());
        helper.setSubject(createEmailSubject(request));
        helper.setText(createEmailBody(request));

        String fileName = createFileName(request.getTransactionId());
        helper.addAttachment(fileName, new ByteArrayResource(pdfBytes));

        mailSender.send(message);
    }

    private String createEmailSubject(NotificationRequest request) {
        return String.format(emailSubjectTemplate, request.getTransactionId());
    }

    private String createEmailBody(NotificationRequest request) {
        return String.format(
                emailBodyTemplate,
                request.getTransactionId(),
                getStatusText(request.isSuccess()),
                fromName
        );
    }

    private String createFileName(String transactionId) {
        return String.format("payment-report-%s.pdf", transactionId);
    }

    private String getStatusText(boolean success) {
        return success ? "SUCCESS" : "FAILED";
    }

    private String getCurrentFormattedDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(dateFormat));
    }

    @Override
    public String getNotificationType() {
        return "email";
    }
}