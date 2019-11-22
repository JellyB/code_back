/**
 * Created by ht on 2017/10/13.
 */
// ajax从后端获取的数据
var fakeData = {
    "code": 1000000,
    "data": {
        "TRAIN_MISTAKE": {
            "time": "0",
            "limit": "1",
            "over": false
        },
        "ATTENDANCE": {
            "limit": "1",
            "time": "0",
            "over": false
        },
        "TRAIN_INTELLIGENCE": {
            "limit": "1",
            "over": false,
            "time": "0"
        },
        "EVALUATE": {
            "over": false,
            "time": "0",
            "limit": "1"
        },
        "TRAIN_DAILY": {
            "time": "0",
            "over": true,
            "limit": "1"
        },
        "WATCH_FREE": {
            "limit": "1",
            "over": false,
            "time": "0"
        },
        "SHARE": {
            "over": false,
            "time": "0",
            "limit": "1"
        },
        "COURSE_PRACTICE_RIGHT": {
            "over": false,
            "time": "0",
            "limit": "10"
        },
        "TRAIN_SPECIAL": {
            "limit": "1",
            "over": false,
            "time": "0"
        },
        "WATCH_PAY": {
            "over": false,
            "limit": "1",
            "time": "0"
        },
        "EVALUATE_AFTER": {
            "time": "0",
            "over": false,
            "limit": "1"
        },
        "ARENA_WIN": {
            "limit": "1",
            "time": "0",
            "over": false
        }
    },
    "message": "查询成功"
};
// 有序的原型对表
var table = ['TRAIN_DAILY', 'ARENA_WIN', 'TRAIN_INTELLIGENCE', 'TRAIN_MISTAKE', 'TRAIN_SPECIAL', 'WATCH_FREE', 'WATCH_PAY', 'EVALUATE_AFTER', 'EVALUATE', 'COURSE_PRACTICE_RIGHT', 'SHARE'];

window.onload = function() {
    new Vue({
        el: '#t-every',
        data: {
            list: null
        },
        created: function() {
            var self = this;
            var _match = window.location.search.match(/token=([^&]*)/);
            var token = _match ? _match[1] : 'fake_token';
            $.ajax({
                type: 'get',
                // url: 'http://123.103.79.69:8022/v3/mycount/myLevel.php?username=htwx_4224005&action=2',
                url: '/c/v3/my/reward/view',
                dataType: 'jsonp',
                headers:{"token": token},
                success: function (data) {
                    self.list = data.data;
                }
            });
        }
    })
}