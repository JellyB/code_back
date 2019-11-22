import Taro, { Component } from '@tarojs/taro'
import { View } from '@tarojs/components'
import DocsHeader from '../components/doc-header'
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
      reports: []
    }
  }

  componentWillMount() { }

  componentDidMount() {
    this.loadData()
  }

  componentWillUnmount() { }

  componentDidShow() { }

  componentDidHide() { }

  loadData() {
    Taro.getStorage({
      key: 'openid'
    }).then(openid => {
      Taro.request({
        url: BASE_URL + '/v1/report',
        header: {
          openid: openid.data
        }
      }).then(res => {
        this.setState({
          reports: res.data.data,
          isLoading: false
        })
      })
    })
  }

  toDetail(category) {
    Taro.navigateTo({
      url: `/pages/dashboard/detail/index?category=` + category
    })
  }

  render() {
    let loading = null
    if (this.state.isLoading) {
      loading = <View className='loading-container'>
        <AtActivityIndicator mode='center' content='Loading . . .'></AtActivityIndicator>
      </View>
    }

    const content = this.state.reports.map(report => {
      return <View key={report.name}>
        <View className='panel__title'>{report.name}
          <Text onClick={() => this.toDetail(report.id)} className='moreButton'>更多 >></Text>
        </View>

        <View class='container'>
          <View style='height: 30px;'>
            <View className='cell ml100'>今日</View>
            <View className='cell'>昨日</View>
            <View className='cell'>本周</View>
            <View style='text-align: center;'>本月</View>
          </View>
          <View style='height: 30px;'>
            <View className='cell ml100'>{report.rows[0].today}</View>
            <View className='cell'>{report.rows[0].yesterday}</View>
            <View className='cell'>{report.rows[0].week}</View>
            <View className='cell'>{report.rows[0].month}</View>
          </View>
          {
            report.name === '总业绩' ? '' :
              <View style='height: 30px;'>
                <View className='cell w100'>占比</View>
                <View className='cell'>{report.rows[3].today}</View>
                <View className='cell'>{report.rows[3].yesterday}</View>
                <View className='cell'>{report.rows[3].week}</View>
                <View className='cell' style='text-align: center;'>{report.rows[3].month}</View>
              </View>
          }
          <View style='height: 30px;'>
            <View className='cell w100'>同比</View>
            <View className='cell' style={report.rows[1].today.indexOf('-') == -1 ? 'color: red;' : 'color: green;'}>{report.rows[1].today}</View>
            <View className='cell' style={report.rows[1].yesterday.indexOf('-') == -1 ? 'color: red;' : 'color: green;'}>{report.rows[1].yesterday}</View>
            <View className='cell' style={report.rows[1].week.indexOf('-') == -1 ? 'color: red;' : 'color: green;'}>{report.rows[1].week}</View>
            <View className='cell' style={report.rows[1].month.indexOf('-') == -1 ? 'text-align: center; color: red;' : 'text-align: center; color: green;'}>{report.rows[1].month}</View>
          </View>
          <View style='height: 30px;'>
            <View className='cell w100'>环比</View>
            <View className='cell' style={report.rows[2].today.indexOf('-') == -1 ? 'color: red;' : 'color: green;'}>{report.rows[2].today}</View>
            <View className='cell' style={report.rows[2].yesterday.indexOf('-') == -1 ? 'color: red;' : 'color: green;'}>{report.rows[2].yesterday}</View>
            <View className='cell' style={report.rows[2].week.indexOf('-') == -1 ? 'color: red;' : 'color: green;'}>{report.rows[2].week}</View>
            <View className='cell' style={report.rows[2].month.indexOf('-') == -1 ? 'text-align: center; color: red;' : 'text-align: center; color: green;'}>{report.rows[2].month}</View>
          </View>
        </View>

        <View style="height: 17px;"></View>
      </View>
    })

    return (
      <View>
        <DocsHeader title='华图在线课程业绩'></DocsHeader>
        <View className='tips'>
          <AtNoticebar>
            <View>1. 业绩单位：万元</View>
            <View>2. 今日业绩：每小时第20分钟更新一次</View>
          </AtNoticebar>
        </View>
        {loading}
        {content}
      </View>
    )
  }
}
