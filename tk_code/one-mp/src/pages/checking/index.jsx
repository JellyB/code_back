import Taro from '@tarojs/taro'
import { View, Image, Text } from '@tarojs/components'

import checkingImg from '../../assets/images/checking.svg'

import './index.scss'

export default class Index extends Taro.Component {
  config = {
    navigationBarTitleText: 'Checking'
  }

  constructor() {
    super(...arguments)

    this.state = {
      tips: '用户校验中 · · ·'
    }
  }

  componentDidMount() {
    let that = this
    Taro.getStorage({
      key: 'openid',
      fail: () => {
        wx.login({
          success(res) {
            Taro.request({
              url: BASE_URL + '/v1/user/getOpenid?code=' + res.code,
            }).then(res => {
              Taro.setStorage({
                key: 'openid',
                data: res.data.data.openid
              })

              that.checkUser(res.data.data.openid)
            })
          }
        })
      }
    }).then(openid => {
      this.checkUser(openid.data)
    });
  }

  checkUser(openid) {
    Taro.request({
      url: BASE_URL + '/v1/user/check',
      header: {
        openid: openid,
        version: VERSION
      }
    }).then(res => {
      if (res.data.data.status == 0) {
        Taro.redirectTo({
          url: `/pages/register/index`
        })
      } else if (res.data.data.status == 1) {
        this.setState({
          tips: '用户审核中 · · ·'
        })

        this.checkUserTimer()
      } else if (res.data.data.status == 2) {
        let menuIndexes = res.data.data.menuIndexes

        if (menuIndexes.length == 0) {
          this.setState({
            tips: '用户暂无权限 · · ·'
          })

          this.checkUserTimer()
        } else if (menuIndexes.length == 1 && menuIndexes.indexOf(0) == 0) {
          Taro.redirectTo({
            url: `/pages/dashboard/index`
          })
        } else {
          Taro.setStorage({
            key: 'menuIndexes',
            data: menuIndexes
          })

          Taro.redirectTo({
            url: `/pages/index/index`
          })
        }
      } else if (res.data.data.status == 3) {
        this.setState({
          tips: '用户已禁用 · · ·'
        })
      }
    })
  }

  checkUserTimer() {
    setTimeout(() => {
      Taro.getStorage({
        key: 'openid'
      }).then(openid => {
        this.checkUser(openid.data)
      });
    }, 1400)
  }

  render() {
    return (
      <View className='loading-container'>
        <Image src={checkingImg} className='loading-image'></Image>
        <Text>{this.state.tips}</Text>
      </View>
    )
  }
}