import Taro, { Component, sendHCEMessage } from '@tarojs/taro'
import { View, Text, Image } from '@tarojs/components'
import { AtCalendar, AtCard, AtActivityIndicator } from 'taro-ui'

import './index.scss'

import emptyImg from '../../assets/images/empty.svg'
export default class Index extends Component {

  config = {
    navigationBarTitleText: 'Schedule'
  }

  constructor() {
    super(...arguments)

    this.state = {
      isLoading: true,
      schedules: [],
      monthSchedules: []
    }
  }

  componentWillMount() { }

  componentDidMount() {
    let now = new Date()
    let month = now.getMonth() + 1
    let date = now.getDate()
    this.loadCheckData('' + now.getFullYear() + '-' + (month < 10 ? '0' + month : month))
    this.loadData('' + now.getFullYear() + '-' + (month < 10 ? '0' + month : month) + '-' + (date < 10 ? '0' + date : date))
  }

  componentWillUnmount() { }

  componentDidShow() { }

  componentDidHide() { }

  loadCheckData(date) {
    Taro.getStorage({
      key: 'openid'
    }).then(openid => {
      Taro.request({
        url: BASE_URL + '/v1/schedule/monthSchedule',
        header: {
          openid: openid.data,
          version: VERSION
        },
        data: {
          date: date
        }
      }).then(res => {
        this.setState({
          monthSchedules: res.data.data
        })
      })
    })
  }

  loadData(date) {
    this.setState({
      isLoading: true
    })

    Taro.getStorage({
      key: 'openid'
    }).then(openid => {
      Taro.request({
        url: BASE_URL + '/v1/schedule',
        header: {
          openid: openid.data,
          version: VERSION
        },
        data: {
          date: date
        }
      }).then(res => {
        this.setState({
          schedules: res.data.data,
          isLoading: false
        })
      })
    })
  }

  onSelectDate(date) {
    if (date.value) {
      this.loadData(date.value.start)
    }
  }

  onMonthChange(date) {
    this.loadCheckData(date)
  }

  setClipboardData(data) {
    Taro.setClipboardData({ data: data })
  }

  render() {
    const { isLoading, schedules } = this.state
    let loading = null
    if (isLoading) {
      loading = <View className='loading-container'>
        <AtActivityIndicator mode='center' content='Loading . . .'></AtActivityIndicator>
      </View>
    }

    let content = null
    if (!isLoading && schedules.length > 0) {
      content = schedules.map((schedule) => {
        return <View taroKey={schedule.name} onClick={() => this.setClipboardData(schedule.code)}>
          <AtCard
            note={schedule.time}
            extra={schedule.code}
            title={schedule.name}
          >
            {schedule.content}
          </AtCard>

          <View style="height: 11px;"></View>
        </View>
      })
    }

    let empty = null
    if (!isLoading && schedules.length == 0) {
      empty = <View className='empty-container'>
        <Image src={emptyImg} className='empty'></Image>
      </View>
    }

    return (
      <View>
        <AtCalendar
          onSelectDate={this.onSelectDate.bind(this)}
          onMonthChange={this.onMonthChange.bind(this)}
          isVertical={true} marks={this.state.monthSchedules}></AtCalendar>

        {loading}
        {content}
        {empty}
      </View>
    )
  }
}
