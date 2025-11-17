package com.ecprice_research.domain.coupang.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CoupangSignatureUtil {

    private static final String ALGORITHM = "HmacSHA256";

    /**
     * GMT 기준 datetime 생성 (yyMMdd'T'HHmmss'Z')
     */
    public static String generateDateTime() {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyMMdd'T'HHmmss'Z'");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormatGmt.format(new Date());
    }

    /**
     * 특정 datetime으로 HMAC Signature 생성
     */
    public static String generateWithDateTime(
            String method,
            String uri,
            String secretKey,
            String accessKey,
            String datetime
    ) {
        try {
            // URI를 path와 query로 분리
            String[] parts = uri.split("\\?");
            if (parts.length > 2) {
                throw new RuntimeException("Incorrect URI format");
            }

            String path = parts[0];
            String query = parts.length == 2 ? parts[1] : "";

            // 메시지 = datetime + method + path + query
            String message = datetime + method + path + query;

            log.debug("=== COUPANG HMAC DEBUG ===");
            log.debug("DateTime (GMT): {}", datetime);
            log.debug("Method: {}", method);
            log.debug("Path: {}", path);
            log.debug("Query: {}", query);
            log.debug("Message: {}", message);

            // HMAC-SHA256 생성
            SecretKeySpec signingKey = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    ALGORITHM
            );
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

            // Hex 인코딩
            String signature = bytesToHex(rawHmac);

            log.debug("Signature (Hex): {}", signature);
            log.debug("========================");

            // Authorization 헤더 생성
            String authorization = String.format(
                    "CEA algorithm=%s, access-key=%s, signed-date=%s, signature=%s",
                    ALGORITHM,
                    accessKey,
                    datetime,
                    signature
            );

            log.debug("Authorization: {}", authorization);

            return authorization;

        } catch (GeneralSecurityException e) {
            log.error("HMAC Signature Error", e);
            throw new RuntimeException("HMAC Signature Error: " + e.getMessage(), e);
        }
    }

    /**
     * byte array를 Hex string으로 변환
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}