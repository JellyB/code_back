<!--拖拽滑动效果使用 -->
<div class="m-material" style="width:auto" >
    <div class="mata-slide" id="m-material"></div>
</div>


<!-- 确认窗口 -->
<div class="asktips-bg" style="display: none;"></div>
<div class="m-asktips" style="display: none;">
    <p class="tips-txt">是否提交本次练习</p>
    <div class="tips-btn">
        <a class="tips-give" href="javascript:;">取消</a>
        <a class="tips-goon" href="javascript:;">提交</a>
    </div>
</div>

<!-- 状态提示框 --> 
<#--<#if last =1>
    <div class="m-poptips m-poptips-1" style="display: none;">
      <i></i>
    <p>正在交卷中...</p>
    </div>
</#if>-->
</div>
<!-- 状态提示框2 -->
<div class="m-poptips m-poptips-2" style="display: none;">
    <p>网络不佳，请重试</p>
    <a class="u-btn" id="resubmit" href="javascript:;">重新提交</a>
</div>
<!-- 状态提示框3 -->
<div class="m-poptips m-poptips-outtime" style="display: none;">
    <p>网络连接超时，请重试</p>
    <a class="u-btn" id="resubmit_outtime" href="javascript:;">重新提交</a>
</div>
<div id="swipebox"></div>
</body>
</html>

<script src="http://ns.huatu.com/pc/wechat/js/app.js"></script>
<script>
var _hmt = _hmt || [];
(function() {
  var hm = document.createElement("script");
  hm.src = "//hm.baidu.com/hm.js?1d39a05aeff8b455a179675a2ab26c58";
  var s = document.getElementsByTagName("script")[0]; 
  s.parentNode.insertBefore(hm, s);
})();
</script>
