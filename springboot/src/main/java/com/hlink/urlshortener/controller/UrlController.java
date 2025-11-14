package com.hlink.urlshortener.controller;

import com.hlink.urlshortener.dto.BulkUrlCreateRequest;
import com.hlink.urlshortener.dto.BulkUrlCreateResponse;
import com.hlink.urlshortener.dto.UrlCreateRequest;
import com.hlink.urlshortener.dto.UrlResponse;
import com.hlink.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "URL 관리", description = "URL 단축 및 관리 API")
@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://223.130.157.227:3000", "http://223.130.157.227"})
public class UrlController {

    private final UrlService urlService;

    @Operation(summary = "URL 단축 생성", description = "새로운 단축 URL을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "단축 URL 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    public ResponseEntity<UrlResponse> createShortUrl(@Valid @RequestBody UrlCreateRequest request) {
        UrlResponse response = urlService.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "대량 URL 단축 생성", description = "여러 URL을 한 번에 단축합니다.")
    @PostMapping("/bulk")
    public ResponseEntity<BulkUrlCreateResponse> bulkCreateShortUrls(@Valid @RequestBody BulkUrlCreateRequest request) {
        BulkUrlCreateResponse response = urlService.bulkCreateShortUrls(request.getUrls());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "단축 URL 정보 조회", description = "단축 코드로 URL 정보를 조회합니다.")
    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlResponse> getUrlInfo(
            @Parameter(description = "단축 코드", required = true) @PathVariable String shortCode) {
        Optional<UrlResponse> urlOpt = urlService.getUrlInfo(shortCode);
        
        if (urlOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(urlOpt.get());
    }

    @Operation(summary = "URL 목록 조회", description = "모든 단축 URL 목록을 조회합니다. 페이징 및 검색 지원.")
    @GetMapping
    public ResponseEntity<?> getAllUrls(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(required = false, defaultValue = "10") int size,
            @Parameter(description = "검색어 (원본 URL, 단축 코드)", example = "google") @RequestParam(required = false) String search) {
        // 검색어나 필터가 있으면 필터링된 페이징 결과 반환
        if ((search != null && !search.trim().isEmpty())) {
            com.hlink.urlshortener.dto.PageResponse<UrlResponse> pageResponse = 
                urlService.getAllUrlsWithPagingAndFilter(page, size, search, "all");
            return ResponseEntity.ok(pageResponse);
        }
        // 페이징 파라미터가 있으면 페이징된 결과 반환
        if (page >= 0 && size > 0) {
            com.hlink.urlshortener.dto.PageResponse<UrlResponse> pageResponse = urlService.getAllUrlsWithPaging(page, size);
            return ResponseEntity.ok(pageResponse);
        }
        // 페이징 파라미터가 없으면 전체 목록 반환 (하위 호환성)
        List<UrlResponse> urls = urlService.getAllUrls();
        return ResponseEntity.ok(urls);
    }
}

