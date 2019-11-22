function runApp1() {
  var app1Dom = document.querySelector('#page1-app')
  if (!app1Dom) {
    return
  }
  new Vue({
    data: {
      run_bar_anim: false,
      backend_data: {
        privileges: [],
        level: null
      }
    },
    mounted: function() {
      // canvas 宽度监听
      window.onresize = function() {
        var i_width = $('#indicatorContainer').width()
        var i_height = $('#indicatorContainer').height()
        console.log(i_width, i_height)
        $('canvas').css({
          width: i_width + 'px',
          height: i_height + 'px'
        })
      }

      var self = this
      var _match = window.location.search.match(/token=([^&]*)/)
      var token = _match ? _match[1] : 'fake_token'
      $.ajax({
        // url: 'http://123.103.79.69:8022/v3/mycount/myLevel.php?username=htwx_4224005&action=1',
        url: '/c/v3/my/level',
        type: 'get',
        dataType: 'jsonp',
        jsonp: 'callback',
        jsonpCallback: 'sqf',
        headers: {
          token: token
        }
      })
        .success(function(res) {})
        .fail(function() {
          // 失败
          console.log('error')
        })
        .then(function(res) {
          self.backend_data = res.data
          self.$refs.bar_wrap.style.width = self.backend_data.level * 10 + '%'
          self.run_bar_anim = true
          // 转'0.5%'为 1
          self.backend_data.percent = self.backend_data.percent.replace('%', '') - 0
          self.createCircle(self.backend_data.percent)
        })
    },
    methods: {
      createCircle: function(percentage) {
        // 获取
        var box_width = $('.indicatorContainer-wrap').width()
        console.log(box_width)
        //
        $('#indicatorContainer').radialIndicator({
          barColor: '#e9304e',
          barWidth: 5,
          initValue: 0,
          roundCorner: false, // 改为false解决了一个bug
          percentage: true,
          displayNumber: false,
          radius: box_width / 2 - 5 // 半径
        })
        var radialObj = $('#indicatorContainer').data('radialIndicator')
        //now you can use instance to call different method on the radial progress.
        //like
        if (percentage && percentage > 0) {
          radialObj.animate(percentage)
        }
      }
    }
  }).$mount(app1Dom)
}

function runApp2() {
  var appDom = document.querySelector('#page2-app')
  if (!appDom) {
    return
  }
  new Vue({
    data: {
      backend_data: []
      // backend_data: /**/[{"level":"1","analysis":"0","saleRemark":"无折扣"},{"level":"2","analysis":"1","saleRemark":"无折扣"},{"level":"3","analysis":"2","saleRemark":"无折扣"},{"level":"4","analysis":"3","saleRemark":"95折"},{"level":"5","analysis":"5","saleRemark":"9折"},{"level":"6","analysis":"7","saleRemark":"85折"},{"level":"7","analysis":"8","saleRemark":"8折"},{"level":"8","analysis":"10","saleRemark":"75折"},{"level":"9","analysis":"15","saleRemark":"65折"},{"level":"10","analysis":"20","saleRemark":"5折"}]
    },
    created: function() {
      var self = this
      var _match = window.location.search.match(/token=([^&]*)/)
      var token = _match ? _match[1] : 'fake_token'
      $.ajax({
        // url: 'http://123.103.79.69:8022/v3/mycount/myLevel.php?username=htwx_4224005&action=2',
        url: '/c/v3/level/_settings',
        type: 'get',
        dataType: 'jsonp',
        jsonp: 'callback',
        jsonpCallback: 'sqf',
        headers: {
          token: token
        }
      })
        .success(function(res) {})
        .fail(function() {
          // 失败
          console.log('error')
        })
        .then(function(res) {
          console.log('14:01')
          console.log(res.data)
          self.backend_data = res.data
        })
    }
  }).$mount(appDom)
}

window.onload = function() {
  runApp1()
  runApp2()
}
