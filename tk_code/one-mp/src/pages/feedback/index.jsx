import Taro from '@tarojs/taro'
import { View } from '@tarojs/components'
import { AtTextarea ,AtButton}  from 'taro-ui'
import DocsHeader from '../components/doc-header'

import './index.scss'

export default class Index extends Taro.Component{
    config = {
      navigationBarTitleText: 'Feedback'
    }

    constructor(){
        super (...arguments)

        this.state = {
            value: ''
        }
    }

    handleChange (event) {
        this.setState({
          value: event.target.value
        })
      }

      feedback(){
        Taro.getStorage({
          key: 'openid'
        }).then(openid =>{
          Taro.request({
            url: BASE_URL +  '/v1/feedback/add',
            method: 'POST',
            header: {
              openid: openid.data
            },
            data:  {
              content: this.state.value
            }
          }).then(res => {
            Taro.navigateBack({
              url: '/pages/index/index'
            })
          })
        })
      }

      render () {
        return (
          <View className='page page-index' >
              <DocsHeader title='意见、需求反馈'></DocsHeader>
              <View className='form-container'>
                <AtTextarea
                  value={this.state.value}
                  onChange={this.handleChange.bind(this)}
                  maxLength={500}
                  placeholder='在使用中有什么不便之处或者新的需求，可以这里反馈哦 . . .'/>
                <AtButton onClick={this.feedback.bind(this)} className='submit-button' disabled={this.state.value.length == 0}>提 交</AtButton>
              </View> 
          </View>
          
        )
      }
}
