package com.hlink.urlshortener.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class UserAgentParser {

    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "(?i)(mobile|android|iphone|ipad|ipod|blackberry|windows phone|opera mini)"
    );
    
    private static final Pattern TABLET_PATTERN = Pattern.compile(
            "(?i)(ipad|tablet|android(?!.*mobile))"
    );
    
    private static final Pattern OS_PATTERN = Pattern.compile(
            "(?i)(windows|mac os|linux|android|ios|iphone os|ipad os)"
    );
    
    private static final Pattern BROWSER_PATTERN = Pattern.compile(
            "(?i)(chrome|firefox|safari|edge|opera|msie|trident)"
    );
    
    private static final Pattern DEVICE_BRAND_PATTERN = Pattern.compile(
            "(?i)(iphone|ipad|ipod|samsung|huawei|xiaomi|oppo|vivo|oneplus|google|pixel|lg|sony|nokia|motorola)"
    );

    public String parseDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        
        if (TABLET_PATTERN.matcher(userAgent).find()) {
            return "Tablet";
        }
        
        if (MOBILE_PATTERN.matcher(userAgent).find()) {
            return "Mobile";
        }
        
        return "Desktop";
    }

    public String parseOS(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        
        Matcher matcher = OS_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            String os = matcher.group(1).toLowerCase();
            if (os.contains("windows")) return "Windows";
            if (os.contains("mac os")) return "macOS";
            if (os.contains("linux")) return "Linux";
            if (os.contains("android")) return "Android";
            if (os.contains("ios") || os.contains("iphone os") || os.contains("ipad os")) return "iOS";
        }
        
        return "Unknown";
    }

    public String parseBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        
        Matcher matcher = BROWSER_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            String browser = matcher.group(1).toLowerCase();
            if (browser.contains("chrome")) return "Chrome";
            if (browser.contains("firefox")) return "Firefox";
            if (browser.contains("safari") && !browser.contains("chrome")) return "Safari";
            if (browser.contains("edge")) return "Edge";
            if (browser.contains("opera")) return "Opera";
            if (browser.contains("msie") || browser.contains("trident")) return "IE";
        }
        
        return "Unknown";
    }

    public String parseDeviceBrand(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        
        Matcher matcher = DEVICE_BRAND_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            String brand = matcher.group(1).toLowerCase();
            if (brand.contains("iphone") || brand.contains("ipad") || brand.contains("ipod")) {
                return "Apple";
            }
            return capitalizeFirst(brand);
        }
        
        return "Unknown";
    }

    public boolean isMobile(String userAgent) {
        return "Mobile".equals(parseDeviceType(userAgent)) || "Tablet".equals(parseDeviceType(userAgent));
    }

    public boolean isIOS(String userAgent) {
        return "iOS".equals(parseOS(userAgent));
    }

    public boolean isAndroid(String userAgent) {
        return "Android".equals(parseOS(userAgent));
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}

