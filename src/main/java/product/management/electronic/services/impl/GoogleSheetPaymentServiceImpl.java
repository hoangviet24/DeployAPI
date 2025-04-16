package product.management.electronic.services.impl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import product.management.electronic.enums.PaymentStatus;
import product.management.electronic.repository.OrderRepository;
import product.management.electronic.services.GoogleSheetPaymentService;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSheetPaymentServiceImpl implements GoogleSheetPaymentService {

    private final OrderRepository orderRepository;
    private final OrderServiceImpl orderService;
    @Value("${google.sheet.scope}")
    private String googleSheetScope;
    @Value("${google.sheet.app-name}")
    private String applicationName;
    @Value("${google.sheet.spreadsheet-id}")
    private String spreadsheetId;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    @Value("${google.sheet.range}")
    private String range;
    @Scheduled(fixedRate = 30_000)
    @Override
    public void checkPaymentsFromGoogleSheet() {
        try {
            List<List<Object>> transactions = readTransactions();

            for (List<Object> row : transactions) {
                if (row.size() < 8) continue;

                String paymentContent = row.get(5).toString().trim();
                double amount;
                try {
                    amount = Double.parseDouble(row.get(7).toString());
                } catch (NumberFormatException e) {
                    continue;
                }
                paymentContent = processPaymentContent(paymentContent);
                UUID orderId;
                try {
                    orderId = UUID.fromString(paymentContent);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                orderRepository.findById(orderId).ifPresent(order -> {
                    double calculatedAmount = orderService.calculateTotalAmount(order);
                    if (order.getPaymentStatus() == PaymentStatus.UNPAID &&
                            Math.abs(calculatedAmount - amount) < 0.001) {
                        order.setPaymentStatus(PaymentStatus.PAID);
                        orderRepository.save(order);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error while checking Google Sheet payments: {}", e.getMessage(), e);
        }
    }

    private List<List<Object>> readTransactions() throws IOException, GeneralSecurityException {
        try (FileInputStream serviceAccountStream = new FileInputStream("credentials.json")) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream)
                    .createScoped(List.of(googleSheetScope));
            Sheets sheetsService = new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    new HttpCredentialsAdapter(credentials)
            )
                    .setApplicationName(applicationName)
                    .build();
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            return response.getValues() == null ? new ArrayList<>() : response.getValues();
        }
    }

    private String processPaymentContent(String paymentContent) {
        int dashIndex = paymentContent.indexOf('-');
        if (dashIndex != -1) {
            paymentContent = paymentContent.substring(0, Math.min(dashIndex, 32));
        } else {
            paymentContent = paymentContent.length() > 32 ? paymentContent.substring(0, 32) : paymentContent;
        }
        if (paymentContent.length() < 32) {
            return paymentContent;
        }
        try {
            StringBuilder formattedUUID = new StringBuilder(paymentContent);
            formattedUUID.insert(8, '-');
            formattedUUID.insert(13, '-');
            formattedUUID.insert(18, '-');
            formattedUUID.insert(23, '-');
            return formattedUUID.toString();
        } catch (Exception e) {
            return paymentContent;
        }
    }
}