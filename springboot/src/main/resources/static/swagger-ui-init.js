// Swagger UI 커스텀 CSS 주입 (shadcn 스타일)
(function() {
  function injectCustomCSS() {
    // 이미 추가되었는지 확인
    if (document.getElementById('swagger-ui-custom-css')) {
      return;
    }
    
    // 커스텀 CSS 링크 추가
    const link = document.createElement('link');
    link.id = 'swagger-ui-custom-css';
    link.rel = 'stylesheet';
    link.type = 'text/css';
    link.href = '/swagger-ui-custom.css';
    document.head.appendChild(link);
  }
  
  // 즉시 실행
  injectCustomCSS();
  
  // DOM 로드 완료 시 실행
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', injectCustomCSS);
  }
  
  // Swagger UI가 동적으로 로드되는 경우를 대비
  const observer = new MutationObserver(function(mutations) {
    injectCustomCSS();
  });
  
  observer.observe(document.body, {
    childList: true,
    subtree: true
  });
  
  // Swagger UI 초기화 후에도 주입
  window.addEventListener('load', injectCustomCSS);
})();

