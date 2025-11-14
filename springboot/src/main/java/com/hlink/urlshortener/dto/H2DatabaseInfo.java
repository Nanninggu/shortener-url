package com.hlink.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class H2DatabaseInfo {
    private String databaseType;
    private String jdbcUrl;
    private String consoleUrl;
    private String tcpUrl;
    private String username;
    private String password;
    private Integer tcpPort;
    private String databasePath;
    private Boolean consoleEnabled;
    private Boolean externalAccessEnabled;
}

