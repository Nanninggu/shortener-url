package com.hlink.urlshortener.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.hlink.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/qrcode")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class QrCodeController {

    private final UrlService urlService;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @GetMapping("/{shortCode}")
    public ResponseEntity<byte[]> generateQrCode(@PathVariable String shortCode, HttpServletRequest request) {
        Optional<com.hlink.urlshortener.dto.UrlResponse> urlOpt = urlService.getUrlInfo(shortCode);
        
        if (urlOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 요청의 Host 정보를 사용하여 동적으로 URL 생성
        String shortUrl = buildShortUrl(shortCode, request);
        
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(shortUrl, BarcodeFormat.QR_CODE, 300, 300);
            
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();
            
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(pngData);
        } catch (WriterException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private String buildShortUrl(String shortCode, HttpServletRequest request) {
        if (request != null) {
            String scheme = request.getScheme(); // http or https
            String serverName = request.getServerName(); // localhost or IP
            int serverPort = request.getServerPort();
            
            // X-Forwarded-Host 헤더 확인 (프록시/로드밸런서 뒤에 있는 경우)
            String forwardedHost = request.getHeader("X-Forwarded-Host");
            if (forwardedHost != null && !forwardedHost.isEmpty()) {
                serverName = forwardedHost.split(":")[0];
                if (forwardedHost.contains(":")) {
                    try {
                        serverPort = Integer.parseInt(forwardedHost.split(":")[1]);
                    } catch (NumberFormatException e) {
                        // 포트 파싱 실패 시 기본값 유지
                    }
                }
            }
            
            // localhost인 경우 실제 IP 주소로 변경 (스마트폰 접근 가능하도록)
            if (serverName.equals("localhost") || serverName.equals("127.0.0.1")) {
                String actualIp = getLocalNetworkIp();
                if (actualIp != null) {
                    log.info("Replacing localhost with actual IP {} for QR code", actualIp);
                    serverName = actualIp;
                } else {
                    // IP를 찾을 수 없으면 base-url 사용
                    log.warn("Could not determine local network IP, using base-url from config");
                    if (baseUrl != null && !baseUrl.isEmpty() && !baseUrl.contains("localhost")) {
                        return baseUrl + "/" + shortCode;
                    }
                }
            }
            
            // 포트가 기본 포트(80, 443)가 아니면 포함
            String port = "";
            if ((scheme.equals("http") && serverPort != 80) || 
                (scheme.equals("https") && serverPort != 443)) {
                port = ":" + serverPort;
            }
            
            return scheme + "://" + serverName + port + "/" + shortCode;
        }
        
        // 요청이 없으면 기본값 사용
        return baseUrl + "/" + shortCode;
    }
    
    /**
     * 로컬 네트워크의 실제 IP 주소를 가져옵니다 (localhost 대신 사용)
     * 스마트폰에서 QR 코드를 스캔할 때 접근 가능하도록 합니다.
     */
    private String getLocalNetworkIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                // 루프백 인터페이스는 제외
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    
                    // IPv4 주소만 사용 (IPv6는 제외)
                    if (!address.isLoopbackAddress() && 
                        address.getAddress().length == 4) {
                        String ip = address.getHostAddress();
                        
                        // 사설 IP 대역만 사용 (공인 IP는 제외)
                        if (ip.startsWith("192.168.") || 
                            ip.startsWith("10.") || 
                            ip.startsWith("172.16.") || ip.startsWith("172.17.") ||
                            ip.startsWith("172.18.") || ip.startsWith("172.19.") ||
                            ip.startsWith("172.20.") || ip.startsWith("172.21.") ||
                            ip.startsWith("172.22.") || ip.startsWith("172.23.") ||
                            ip.startsWith("172.24.") || ip.startsWith("172.25.") ||
                            ip.startsWith("172.26.") || ip.startsWith("172.27.") ||
                            ip.startsWith("172.28.") || ip.startsWith("172.29.") ||
                            ip.startsWith("172.30.") || ip.startsWith("172.31.")) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error getting local network IP: {}", e.getMessage());
        }
        return null;
    }
}

