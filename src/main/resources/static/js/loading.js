(function () {
    const overlayId = 'global-loading-overlay';

    function showLoading() {
        const overlay = document.getElementById(overlayId);
        if (overlay) {
            overlay.classList.add('is-active');
        }
    }

    function hideLoading() {
        const overlay = document.getElementById(overlayId);
        if (overlay) {
            overlay.classList.remove('is-active');
        }
    }

    function shouldIgnoreLink(link) {
        const href = link.getAttribute('href');

        return !href
            || href === '#'
            || href.startsWith('#')
            || link.target === '_blank'
            || link.hasAttribute('download')
            || link.dataset.bsToggle
            || link.href.includes('/download');
    }

    document.addEventListener('submit', function () {
        showLoading();
    });

    document.addEventListener('click', function (event) {
        const link = event.target.closest('a');
        if (!link || shouldIgnoreLink(link)) {
            return;
        }

        const currentOrigin = window.location.origin;
        if (link.href.startsWith(currentOrigin)) {
            showLoading();
        }
    });

    window.addEventListener('pageshow', hideLoading);
})();
