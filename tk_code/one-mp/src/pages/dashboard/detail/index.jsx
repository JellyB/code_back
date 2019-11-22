import Taro, { Component } from '@tarojs/taro'
import { View } from '@tarojs/components'
import DocsHeader from '../../components/doc-header'
import { AtActivityIndicator, AtNoticebar } from 'taro-ui'

import './index.scss'

export default class Index extends Component {

  config = {
    navigationBarTitleText: 'Dashboard'
  }

  constructor() {
    super(...arguments)

    this.state = {
      isLoading: true,
      detailV1s: []
    }
  }

  componentWillMount() { }

  componentDidMount() {
    this.loadData(this.$router.params.category)
  }

  componentWillUnmount() { }

  componentDidShow() { }

  componentDidHide() { }

  loadData(category) {
    Taro.getStorage({
      key: 'openid'
    }).then(openid => {
      Taro.request({
        url: BASE_URL + '/v1/report/detailV1?category=' + category,
        header: {
          openid: openid.data
        }
      }).then(res => {
        this.setState({
          detailV1s: res.data.data,
          isLoading: false
        })
      })
    })
  }

  render() {
    let loading = null
    if (this.state.isLoading) {
      loading = <View className='loading-container'>
        <AtActivityIndicator mode='center' content='Loading . . .'></AtActivityIndicator>
      </View>
    }

    const { detailV1s } = this.state

    return (
      <View>
        <View class='container'>
          <View style='height: 30px;'>
            <View className='cell ml240'>上周</View>
            <View className='cell'>上月</View>
          </View>
          {
            detailV1s.length > 0 ?
              <View>
                <View style='height: 30px;'>
                  <View className='cell ml240'>{detailV1s[0].week}</View>
                  <View className='cell'>{detailV1s[0].month}</View>
                </View>
                <View style='height: 30px;'>
                  <View className='cell-label'>同比</View>
                  <View className='cell' style={detailV1s[1].week.indexOf('-') == -1 ? 'color: red;' : 'color: green;'}>{detailV1s[1].week}</View>
                  <View className='cell' style={detailV1s[1].month.indexOf('-') == -1 ? 'color: red;' : 'color: green;'}>{detailV1s[1].month}</View>
                </View>
                <View style='height: 30px;'>
                  <View className='cell-label'>环比</View>
                  <View className='cell' style={detailV1s[2].week.indexOf('-') == -1 ? 'color: red;' : 'color: green;'}>{detailV1s[2].week}</View>
                  <View className='cell' style={detailV1s[2].month.indexOf('-') == -1 ? 'color: red;' : 'color: green;'}>{detailV1s[2].month}</View>
                </View>
              </View>
              : ''
          }
        </View>
        {loading}
      </View>
    )
  }
}
