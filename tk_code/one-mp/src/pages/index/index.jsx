import Taro from '@tarojs/taro'
import { View, Image, Text } from '@tarojs/components'
import { AtFab } from 'taro-ui'

import logoImg from '../../assets/images/logo_taro.png'
import reportImg from '../../assets/images/report.png'
import scheduleImg from '../../assets/images/schedule.png'
import rankingImg from '../../assets/images/ranking.png'

import './index.scss'

const MENUS = [{
  id: 'Dashboard',
  title: '数据看板',
  content: '销售额，同比，环比报表',
  icon: reportImg
},
{
  id: 'Schedule',
  title: '课程表',
  content: '课程信息，上课码',
  icon: scheduleImg
},
{
  id: 'Course-Ranking',
  title: '课程销售排名',
  content: '课程销售排名',
  icon: rankingImg
}]

export default class Index extends Taro.Component {
  config = {
    navigationBarTitleText: 'HuaTu One'
  }

  constructor() {
    super(...arguments)

    this.state = {
      menus: []
    }
  }

  componentDidMount() {
    Taro.getStorage({
      key: 'menuIndexes'
    }).then(res => {
      let menuIndexes = res.data

      if (menuIndexes.length == 1 && menuIndexes.indexOf(0) == 0) {
        Taro.navigateTo({
          url: `/pages/dashboard/index`
        })
      } else {
        let menus = []
        menuIndexes.forEach(i => {
          menus.push(MENUS[i])
        })

        if (menus.length > 0) {
          this.setState({
            menus: menus
          })
        }
      }
    })
  }

  onShareAppMessage() {
    return {
      title: 'HuaTu One',
      path: '/pages/checking/index',
      imageUrl: 'https://tiku.huatu.com/cdn/images/ic/20190829/share1535013100318.png'
    }
  }

  gotoPanel = e => {
    const { id } = e.currentTarget.dataset
    Taro.navigateTo({
      url: `/pages/${id.toLowerCase()}/index`
    })
  }

  feedBack() {
    Taro.navigateTo({
      url: `/pages/feedback/index`
    })
  }

  render() {
    const { list } = this.state
    return (
      <View className='page page-index'>
        <View className='logo'>
          <Image src={logoImg} className='img' mode='widthFix' />
        </View>
        <View className='page-title'>HuaTu One</View>
        <View className='module-list'>
          {this.state.menus.map((item, index) => (
            <View
              className='module-list__item'
              key={index}
              data-id={item.id}
              data-name={item.title}
              data-list={item.subpages}
              onClick={this.gotoPanel}
            >
              <View className='module-list__icon'>
                <Image src={item.icon} className='img' mode='widthFix' />
              </View>
              <View className='module-list__info'>
                <View className='title'>{item.title}</View>
                <View className='content'>{item.content}</View>
              </View>
              <View className='module-list__arrow'>
                <Text className='at-icon at-icon-chevron-right' />
              </View>
            </View>
          ))}
        </View>

        <View className='fab-icon'>
          <AtFab onClick={this.feedBack}>
            <Text className='at-fab__icon at-icon at-icon-mail'></Text>
          </AtFab>
        </View>
      </View>
    )
  }
}