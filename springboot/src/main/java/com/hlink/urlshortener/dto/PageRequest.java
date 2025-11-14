package com.hlink.urlshortener.dto;

import lombok.Data;

@Data
public class PageRequest {
    private int page = 0; // 0-based page number
    private int size = 10; // page size
    
    public int getOffset() {
        return page * size;
    }
}

