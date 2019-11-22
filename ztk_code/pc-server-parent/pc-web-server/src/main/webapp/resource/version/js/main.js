var Util = {
	UA: {}
};

Util.UA.isMobile = function() {
	if (navigator.userAgent.match(/(MicroMessenger|phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i)) {
		return true;
	}
	return false;
};

Util.UA.isWeChat = function() {
	return /micromessenger/i.test(navigator.userAgent);
};

Util.UA.isQQBroswer = function() {
	return /qq/i.test(navigator.userAgent);
};

Util.UA.isIOS = function() {
	return /(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent);
};

Util.UA.isAndroid = function() {
	return /(Android)/i.test(navigator.userAgent);
};

window.onload = function() {
	var btn = document.querySelector('#download');
	btn.addEventListener('click', function() {
		if (Util.UA.isIOS()) {
			window.location.href = 'https://itunes.apple.com/cn/app/hua-tu-zai-xian-gong-wu-yuan/id940376535';
		} else {
			window.location.href = 'http://app.qq.com/#id=detail&appid=1103583915';
		}
	}, false);
}