package com.hlink.urlshortener.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Component
@Order(1)
public class SwaggerUiFilter implements Filter {

    private static final String SWAGGER_UI_INIT_SCRIPT = """
        <script>
        // Swagger UI 커스텀 CSS 주입 및 기본 Petstore 제거
        (function() {
          // 즉시 실행 - Swagger UI 초기화 전에 실행
          (function() {
            // window.SwaggerUIBundle이 로드되기 전에 기본 URL 설정을 오버라이드
            const originalFetch = window.fetch;
            window.fetch = function(...args) {
              const url = args[0];
              if (typeof url === 'string' && url.includes('petstore')) {
                args[0] = '/v3/api-docs';
              }
              return originalFetch.apply(this, args);
            };
          })();
          
          function injectCustomCSS() {
            if (document.getElementById('swagger-ui-custom-css')) {
              return;
            }
            const link = document.createElement('link');
            link.id = 'swagger-ui-custom-css';
            link.rel = 'stylesheet';
            link.type = 'text/css';
            link.href = '/swagger-ui-custom.css';
            document.head.appendChild(link);
          }
          
          function removeDefaultPetstore() {
            // Swagger UI 초기화 전에 기본 URL 제거
            try {
              if (window.ui && window.ui.getSystem) {
                const system = window.ui.getSystem();
                if (system && system.specSelectors) {
                  const urls = system.specSelectors.url();
                  // urls가 배열인지 확인
                  if (urls && Array.isArray(urls) && urls.length > 0) {
                    const filteredUrls = urls.filter(url => 
                      url && (!url.url || !url.url.includes('petstore'))
                    );
                    if (filteredUrls.length < urls.length && system.specActions) {
                      system.specActions.updateSpecUrl(filteredUrls[0]?.url || '/v3/api-docs');
                    }
                  } else if (urls && typeof urls === 'string' && urls.includes('petstore')) {
                    // urls가 문자열인 경우
                    if (system.specActions) {
                      system.specActions.updateSpecUrl('/v3/api-docs');
                    }
                  }
                }
              }
            } catch (e) {
              // 에러 무시 (Swagger UI가 아직 초기화되지 않았을 수 있음)
              console.debug('Swagger UI not ready yet:', e);
            }
            
            // DOM에서 Petstore 관련 요소 제거
            const petstoreElements = document.querySelectorAll('[data-name*="petstore"], [data-name*="Petstore"], [data-url*="petstore"], [href*="petstore"]');
            petstoreElements.forEach(el => {
              if (el.tagName === 'OPTION' || el.tagName === 'A') {
                el.remove();
              }
            });
            
            // URL 입력 필드에서 Petstore URL 제거 및 강제 설정
            const urlInput = document.querySelector('.download-url-wrapper input');
            if (urlInput) {
              if (urlInput.value && urlInput.value.includes('petstore')) {
                urlInput.value = '/v3/api-docs';
                // 이벤트 트리거
                urlInput.dispatchEvent(new Event('change', { bubbles: true }));
                urlInput.dispatchEvent(new Event('input', { bubbles: true }));
              }
              // 값이 비어있거나 petstore가 아닌 경우에도 강제 설정
              if (!urlInput.value || urlInput.value.trim() === '') {
                urlInput.value = '/v3/api-docs';
                urlInput.dispatchEvent(new Event('change', { bubbles: true }));
              }
            }
            
            // select 요소에서 Petstore 옵션 제거
            const selectElements = document.querySelectorAll('select');
            selectElements.forEach(select => {
              const options = select.querySelectorAll('option');
              options.forEach(option => {
                if (option.value && option.value.includes('petstore')) {
                  option.remove();
                }
              });
            });
          }
          
          // 즉시 실행
          injectCustomCSS();
          
          // DOM 로드 완료 시 실행
          if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', function() {
              injectCustomCSS();
              removeDefaultPetstore();
              setTimeout(removeDefaultPetstore, 100);
              setTimeout(removeDefaultPetstore, 500);
              setTimeout(removeDefaultPetstore, 1000);
              setTimeout(removeDefaultPetstore, 2000);
              setTimeout(removeDefaultPetstore, 3000);
            });
          } else {
            removeDefaultPetstore();
            setTimeout(removeDefaultPetstore, 100);
            setTimeout(removeDefaultPetstore, 500);
            setTimeout(removeDefaultPetstore, 1000);
            setTimeout(removeDefaultPetstore, 2000);
            setTimeout(removeDefaultPetstore, 3000);
          }
          
          // Swagger UI가 동적으로 로드되는 경우를 대비
          function setupObserver() {
            if (document.body) {
              try {
                const observer = new MutationObserver(function(mutations) {
                  injectCustomCSS();
                  removeDefaultPetstore();
                });
                
                observer.observe(document.body, {
                  childList: true,
                  subtree: true,
                  attributes: true,
                  attributeFilter: ['value', 'href']
                });
              } catch (e) {
                console.debug('MutationObserver setup failed:', e);
              }
            }
          }
          
          // document.body가 준비될 때까지 대기
          if (document.body) {
            setupObserver();
          } else {
            if (document.readyState === 'loading') {
              document.addEventListener('DOMContentLoaded', setupObserver);
            } else {
              setTimeout(setupObserver, 100);
            }
          }
          
          // Swagger UI 초기화 후에도 주입
          window.addEventListener('load', function() {
            injectCustomCSS();
            removeDefaultPetstore();
            setTimeout(removeDefaultPetstore, 100);
            setTimeout(removeDefaultPetstore, 500);
            setTimeout(removeDefaultPetstore, 1000);
            setTimeout(removeDefaultPetstore, 2000);
            setTimeout(removeDefaultPetstore, 4000);
          });
          
          // Swagger UI 완전히 로드된 후에도 지속적으로 확인
          setInterval(function() {
            removeDefaultPetstore();
          }, 2000);
        })();
        </script>
        """;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        
        // Swagger UI HTML 페이지인지 확인 (다양한 경로 패턴 지원)
        if (path != null && (path.contains("/swagger-ui/index.html") 
                || path.equals("/swagger-ui/") 
                || path.equals("/swagger-ui")
                || path.endsWith("/swagger-ui/index.html"))) {
            // 응답을 가로채서 스크립트 주입
            ResponseWrapper responseWrapper = new ResponseWrapper(httpResponse);
            chain.doFilter(request, responseWrapper);
            
            String content = responseWrapper.getContent();
            if (content != null && !content.isEmpty()) {
                // </head> 태그 앞에 스크립트 주입 (더 빠른 실행을 위해)
                if (!content.contains("swagger-ui-custom-css")) {
                    content = content.replace("</head>", SWAGGER_UI_INIT_SCRIPT + "</head>");
                }
                
                byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
                httpResponse.setContentLength(contentBytes.length);
                httpResponse.getOutputStream().write(contentBytes);
                httpResponse.getOutputStream().flush();
            } else {
                chain.doFilter(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    // 응답 래퍼 클래스
    private static class ResponseWrapper extends jakarta.servlet.http.HttpServletResponseWrapper {
        private final ByteArrayServletOutputStream outputStream;
        private PrintWriter writer;
        private boolean usingOutputStream = false;
        private boolean usingWriter = false;

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
            this.outputStream = new ByteArrayServletOutputStream();
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (usingWriter) {
                throw new IllegalStateException("getWriter() has already been called");
            }
            usingOutputStream = true;
            return outputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (usingOutputStream) {
                throw new IllegalStateException("getOutputStream() has already been called");
            }
            usingWriter = true;
            if (writer == null) {
                writer = new PrintWriter(new java.io.OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            }
            return writer;
        }

        public String getContent() throws IOException {
            if (writer != null) {
                writer.flush();
            }
            return outputStream.getContent();
        }
    }

    private static class ByteArrayServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        @Override
        public void write(int b) throws IOException {
            buffer.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            buffer.write(b, off, len);
        }

        public String getContent() {
            return buffer.toString(StandardCharsets.UTF_8);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener listener) {
            // Not needed for this implementation
        }
    }
}

