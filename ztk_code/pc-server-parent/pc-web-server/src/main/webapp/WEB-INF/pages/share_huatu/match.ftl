<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>模考大赛</title>
	<meta name="apple-itunes-app" content="app-id=940376535"/>
	<meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, minimal-ui"> <!-- 尺寸大小，禁止缩放 -->
	<!-- <meta name="apple-mobile-web-app-capable" content="yes">  safar浏览器全屏显示 -->
	<!-- <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent"> -->
	<meta name="format-detection" content="telephone=no, address=no">   <!-- 数字号码不被显示为拨号链接 -->
	<link rel="stylesheet" href="http://tiku.huatu.com/cdn/share/match/css/match.css">
</head>
<body>
	<div id="wrap" class="clearfix" v-cloak>
		<div class="mask" @click="showMask=false" v-show="showMask">
			<div class="mask-alert mask-alert-ios pr clearfix">
				<img src="http://tiku.huatu.com/cdn/share/match/img/arrow.png" alt="" class="arrow">
				<span class="words">请在safari中打开</span>
			</div>
		</div>
		<div class="title1 clearfix g-padding">
			<div class="title1-title">
				${name}<br>
                    <#--<span class="yellow">模考大赛</span>-->
			</div>
			<div class="time">
				${timeInfo}
				<#--9月17日（周日）9:00-11:00-->
			</div>

			<div class="shizhan"></div>
			<img src="http://tiku.huatu.com/cdn/share/match/img/img001.png" alt="" class="shizhan">
			<button class="baoming pr" @click="baoming">
				点此报名
				<div class="qr" v-show="showQR">
					<img src="http://tiku.huatu.com/cdn/share/match/img/qrcode_htwx.gif" alt="">
				</div>
			</button>
	</div>
	<div class="g-padding">
		<div>
			<img src="http://tiku.huatu.com/cdn/share/match/img/img002.png" alt="" class="img002">
		</div>

		<div class="jiexi-time">
			<span>
				<#--9月17日（周日）19：00-21：30-->
				${courseInfo}
			</span><br>
			<span>学员可至华图在线APP购买</span>
		</div>
		<#--<div>-->
			<#--<img src="http://tiku.huatu.com/cdn/share/match/img/img003.png" alt="" class="img003">-->
		<#--</div>-->
	</div>
</div>

<script src="http://tiku.huatu.com/cdn/share/match/js/vue.min.js"></script>
<script src="http://tiku.huatu.com/cdn/share/match/js/main.js"></script>
<script type="text/javascript" src="http://tiku.huatu.com/cdn/share/js/weixin.common.js?v=1.1"></script>
<script src="http://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
<script src="https://code.jquery.com/jquery-1.11.1.min.js"></script>
<script>
	initWeiXin("${title!''}","${description!''}","${imgUrl!''}","${url!''}");
</script>
</body>
</html>