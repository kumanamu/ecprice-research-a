package com.ecprice_research.domain.coupang.service;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CoupangSignatureUtil {

    private static final String ALGORITHM = "HmacSHA256";

    /**
     * ✅ Postman 로직과 100% 동일하게 구현
     */
    public static String generate(String method, String uri, String secretKey, String accessKey) {
        String[] parts = uri.split("\\?");
        if (parts.length > 2) {
            throw new RuntimeException("incorrect uri format");
        }

        String path = parts[0];
        String query = "";
        if (parts.length == 2) {
            query = parts[1];
        }

        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyMMdd'T'HHmmss'Z'");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        String datetime = dateFormatGmt.format(new Date());

        String message = datetime + method + path + query;

        String signature;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    ALGORITHM
            );
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            signature = Hex.encodeHexString(rawHmac);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Unexpected error while creating hash: " + e.getMessage(), e);
        }

        System.out.println("=== [COUPANG SIGNATURE DEBUG] ===");
        System.out.println("URI: " + uri);
        System.out.println("Path: " + path);
        System.out.println("Query: " + query);
        System.out.println("DateTime: " + datetime);
        System.out.println("Message: " + message);
        System.out.println("Message Length: " + message.length());
        System.out.println("Message Bytes: " + bytesToHex(message.getBytes(StandardCharsets.UTF_8)));
        System.out.println("Signature: " + signature);
        System.out.println("=== [END] ===");

        return String.format(
                "CEA algorithm=%s, access-key=%s, signed-date=%s, signature=%s",
                "HmacSHA256",
                accessKey,
                datetime,
                signature
        );
    }

    /**
     * ✅ 테스트용: 고정 datetime으로 서명 생성
     */
    public static void testSignature() {
        String secretKey = "c96fb0715ee500b0f37aa067a83187a1af41573a";
        String accessKey = "f014f0dd-3dcb-48b9-b1ee-aa761712189c";

        try {
            // 1) 인코딩 테스트
            String keyword = "아이폰14";
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            System.out.println("=== [ENCODING TEST] ===");
            System.out.println("Original: " + keyword);
            System.out.println("Encoded: " + encodedKeyword);
            System.out.println("Expected: %EC%95%84%EC%9D%B4%ED%8F%B014");
            System.out.println("Match: " + encodedKeyword.equals("%EC%95%84%EC%9D%B4%ED%8F%B014"));
            System.out.println();

            // 2) 서명 테스트 (Postman과 동일한 값)
            String datetime = "251119T043522Z";
            String method = "GET";
            String path = "/v2/providers/affiliate_open_api/apis/openapi/v1/products/search";
            String query = "keyword=" + encodedKeyword;
            String message = datetime + method + path + query;

            System.out.println("=== [SIGNATURE TEST] ===");
            System.out.println("Message: " + message);
            System.out.println("Expected Signature: f23380f06caedb514090f3c030fe6f70a4dd14e7500291f76d2fa75e4ea91fd1");

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(keySpec);

            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            String signature = Hex.encodeHexString(rawHmac);

            System.out.println("Java Signature:     " + signature);
            System.out.println("Match: " + signature.equals("f23380f06caedb514090f3c030fe6f70a4dd14e7500291f76d2fa75e4ea91fd1"));

            if (signature.equals("f23380f06caedb514090f3c030fe6f70a4dd14e7500291f76d2fa75e4ea91fd1")) {
                System.out.println("✅ 서명 알고리즘 정상! 문제는 다른 곳에 있음 (헤더/요청방식)");
            } else {
                System.out.println("❌ 서명 알고리즘 문제! 인코딩이나 메시지 구성 재검토 필요");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}