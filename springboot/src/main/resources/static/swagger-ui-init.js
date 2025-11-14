// Swagger UI 커스텀 CSS 주입 및 기본 Petstore 제거
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
  
  // 기본 Petstore URL 제거
  function removeDefaultPetstore() {
    // Swagger UI 초기화 스크립트에서 기본 URL 제거
    if (window.ui && window.ui.getSystem) {
      const system = window.ui.getSystem();
      if (system && system.specSelectors) {
        // 기본 URL 목록에서 Petstore 제거
        const urls = system.specSelectors.url();
        if (urls && urls.length > 0) {
          const filteredUrls = urls.filter(url => 
            !url.url || !url.url.includes('petstore')
          );
          if (filteredUrls.length < urls.length) {
            system.specActions.updateSpecUrl(filteredUrls[0]?.url || '/v3/api-docs');
          }
        }
      }
    }
    
    // DOM에서 Petstore 관련 요소 제거
    const petstoreElements = document.querySelectorAll('[data-name*="petstore"], [data-name*="Petstore"]');
    petstoreElements.forEach(el => el.remove());
  }
  
  // 즉시 실행
  injectCustomCSS();
  
  // DOM 로드 완료 시 실행
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
      injectCustomCSS();
      setTimeout(removeDefaultPetstore, 1000);
    });
  } else {
    setTimeout(removeDefaultPetstore, 1000);
  }
  
  // Swagger UI가 동적으로 로드되는 경우를 대비
  const observer = new MutationObserver(function(mutations) {
    injectCustomCSS();
    removeDefaultPetstore();
  });
  
  observer.observe(document.body, {
    childList: true,
    subtree: true
  });
  
  // Swagger UI 초기화 후에도 주입
  window.addEventListener('load', function() {
    injectCustomCSS();
    setTimeout(removeDefaultPetstore, 2000);
  });
})();

