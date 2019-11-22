import Taro from '@tarojs/taro'
import { View } from '@tarojs/components'
import { AtInput, AtButton, AtNoticebar } from 'taro-ui'
import DocsHeader from '../components/doc-header'

import './index.scss'

export default class Index extends Taro.Component {
  config = {
    navigationBarTitleText: 'Register'
  }

  constructor() {
    super(...arguments)

    this.state = {
      username: '',
      mobile: ''
    }
  }

  handleUsernameChange(value) {
    this.setState({
      username: value
    })

    return value
  }

  handleMobileChange(value) {
    this.setState({
      mobile: value
    })

    return value
  }

  register() {
    Taro.getStorage({
      key: 'openid'
    }).then(openid => {
      Taro.request({
        url: BASE_URL + '/v1/user/register',
        method: 'POST',
        header: {
          openid: openid.data
        },
        data: {
          username: this.state.username,
          mobile: this.state.mobile
        }
      }).then(res => {
        Taro.redirectTo({
          url: `/pages/checking/index`
        })
      })
    })
  }

  render() {
    const { list } = this.state

    return (
      <View className='page page-index'>
        <DocsHeader title='用户注册'></DocsHeader>
        <View className='tips'>
          <AtNoticebar>
            <View>1. 请老师填写真实信息，后台将根据注册信息与排课等系统进行关联</View>
            <View>2. 如果注册后长时间没有审核，可以在公司交流群提醒开发人员进行审核</View>
          </AtNoticebar>
        </View>
        <View className='form-container'>
          <AtInput
            title="姓名"
            placeholder='请输入姓名'
            onChange={this.handleUsernameChange.bind(this)}
          ></AtInput>
          <AtInput
            title="手机号"
            placeholder='请输入手机号'
            onChange={this.handleMobileChange.bind(this)}
          ></AtInput>
          <AtButton
            onClick={this.register.bind(this)}
            disabled={this.state.username.length == 0 || this.state.mobile.length != 11}
            className='submit-button'>提交</AtButton>
        </View>
      </View>
    )
  }
}