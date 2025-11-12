package com.hlink.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlStatsResponse {
    private List<Map<String, Object>> clicksByHour;
    private List<Map<String, Object>> clicksByCountry;
    private List<Map<String, Object>> clicksByDate;
    private List<Map<String, Object>> clicksByDeviceType;
    private List<Map<String, Object>> clicksByBrowser;
    private List<Map<String, Object>> clicksByOS;
    private List<Map<String, Object>> clicksByReferer;
    private List<Map<String, Object>> clicksByCity;
}

