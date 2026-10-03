(function () {
  var params = new URLSearchParams(window.location.search);
  if (params.has('embed') || window.self !== window.top) {
    document.documentElement.classList.add('embed');
  }
})();
