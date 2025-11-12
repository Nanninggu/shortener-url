package com.hlink.urlshortener.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicQrCodeService {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 동적 QR 코드 생성
     * @param shortCode 단축 코드
     * @param customData 커스텀 데이터 (JSON 문자열)
     * @param size QR 코드 크기
     * @return QR 코드 이미지 바이트 배열
     */
    public byte[] generateDynamicQrCode(String shortCode, String customData, int size) {
        try {
            String qrContent = buildQrContent(shortCode, customData);
            
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, size, size);
            
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            
            return pngOutputStream.toByteArray();
        } catch (WriterException | IOException e) {
            log.error("Error generating dynamic QR code: {}", e.getMessage());
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    /**
     * QR 코드 내용 구성 (동적 데이터 포함)
     */
    private String buildQrContent(String shortCode, String customData) {
        String shortUrl = baseUrl + "/" + shortCode;
        
        if (customData == null || customData.isEmpty()) {
            return shortUrl;
        }

        try {
            Map<String, Object> data = parseCustomData(customData);
            
            // 동적 데이터가 있으면 JSON 형태로 포함
            if (!data.isEmpty()) {
                Map<String, Object> qrPayload = new HashMap<>();
                qrPayload.put("url", shortUrl);
                qrPayload.put("data", data);
                qrPayload.put("timestamp", System.currentTimeMillis());
                
                return objectMapper.writeValueAsString(qrPayload);
            }
            
            return shortUrl;
        } catch (Exception e) {
            log.warn("Failed to parse custom data, using simple URL: {}", e.getMessage());
            return shortUrl;
        }
    }

    private Map<String, Object> parseCustomData(String customData) {
        try {
            if (customData.startsWith("{") || customData.startsWith("[")) {
                return objectMapper.readValue(customData, new TypeReference<Map<String, Object>>() {});
            }
            return new HashMap<>();
        } catch (Exception e) {
            log.error("Error parsing custom data: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}

