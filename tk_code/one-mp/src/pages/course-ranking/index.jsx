import Taro from '@tarojs/taro'
import { View, Picker } from '@tarojs/components'
import { AtTag, AtCard, AtActivityIndicator, AtButton } from 'taro-ui'

import './index.scss'

import emptyImg from '../../assets/images/empty.svg'

export default class Index extends Taro.Component {
  config = {
    navigationBarTitleText: 'Course Ranking'
  }

  constructor() {
    super(...arguments)

    this.state = {
      customExamTypes: [],
      examTypes: [],
      activeExamType: null,
      activeRankingType: 0,
      dateBegin: '',
      dateEnd: '',
      isLoading: false,
      classRankings: []
    }
  }

  componentDidMount() {
    this.loadCustomExamTypes()
    this.loadDefaultOptions()
    this.loadExamTypeOptions()
  }

  loadDefaultOptions() {
    Taro.getStorage({
      key: 'openid'
    }).then(openid => {
      Taro.request({
        url: BASE_URL + '/v1/classRanking/defaultOptions',
        header: {
          openid: openid.data
        }
      }).then(res => {
        this.setState({
          activeExamType: res.data.data.examTypeId,
          dateBegin: res.data.data.dateBegin,
          dateEnd: res.data.data.dateEnd
        })

        this.loadClassRankings(res.data.data.examTypeId, this.state.activeRankingType, res.data.data.dateBegin, res.data.data.dateEnd)
      })
    })
  }

  loadCustomExamTypes() {
    Taro.getStorage({
      key: 'openid'
    }).then(openid => {
      Taro.request({
        url: BASE_URL + '/v1/classRanking/customExamTypes',
        header: {
          openid: openid.data
        }
      }).then(res => {
        this.setState({
          customExamTypes: res.data.data
        })
      })
    })
  }

  loadExamTypeOptions() {
    Taro.getStorage({
      key: 'openid'
    }).then(openid => {
      Taro.request({
        url: BASE_URL + '/v1/classRanking/examTypeOptions',
        header: {
          openid: openid.data
        }
      }).then(res => {
        this.setState({
          examTypes: res.data.data
        })
      })
    })
  }

  loadClassRankings(examType, orderBy, dateBegin, dateEnd) {
    this.setState({
      isLoading: true
    })

    Taro.getStorage({
      key: 'openid'
    }).then(openid => {
      Taro.request({
        url: BASE_URL + '/v1/classRanking?examType=' + examType + '&orderBy=' + orderBy + '&date=' + dateBegin + '+-+' + dateEnd,
        header: {
          openid: openid.data
        }
      }).then(res => {
        this.setState({
          classRankings: res.data.data,
          isLoading: false
        })
      })
    })
  }

  onExamTypeClick(examType) {
    this.loadClassRankings(examType, this.state.activeRankingType, this.state.dateBegin, this.state.dateEnd)

    this.setState({
      activeExamType: examType
    })
  }

  onExamTypeAdd(e) {
    Taro.getStorage({
      key: 'openid'
    }).then(openid => {
      Taro.request({
        url: BASE_URL + '/v1/classRanking/addExamTypes?examTypeId=' + this.state.examTypes[e.detail.value].value,
        method: 'POST',
        header: {
          openid: openid.data
        }
      }).then(() => {
        if (this.state.activeExamType == 0 && this.state.examTypes[0].value === '0') {
          this.setState({
            activeExamType: this.state.examTypes[e.detail.value].value
          })

          this.loadClassRankings(this.state.examTypes[e.detail.value].value, this.state.activeRankingType, this.state.dateBegin, this.state.dateEnd)
        }
        this.loadCustomExamTypes()
        this.loadExamTypeOptions()
      })
    })
  }

  onExamTypeDel(examTypeId) {
    Taro.getStorage({
      key: 'openid'
    }).then(openid => {
      Taro.request({
        url: BASE_URL + '/v1/classRanking/delExamTypes?examTypeId=' + examTypeId,
        method: 'DELETE',
        header: {
          openid: openid.data
        }
      }).then(() => {
        if (this.state.activeExamType == examTypeId) {
          let activeExamType = null
          if (this.state.customExamTypes.length == 1) {
            activeExamType = 0
          } else {
            activeExamType = this.state.customExamTypes[0].value == examTypeId ? this.state.customExamTypes[1].value : this.state.customExamTypes[0].value
          }

          this.setState({
            activeExamType: activeExamType
          })

          this.loadClassRankings(activeExamType, this.state.activeRankingType, this.state.dateBegin, this.state.dateEnd)
        }

        this.loadCustomExamTypes()
        this.loadExamTypeOptions()
      })
    })
  }

  onRankingTypeClick(activeExamType, activeRankingType) {
    this.loadClassRankings(activeExamType, activeRankingType, this.state.dateBegin, this.state.dateEnd)

    this.setState({
      activeRankingType: activeRankingType
    })
  }

  onDateBeginChange(e) {
    this.loadClassRankings(this.state.activeExamType, this.state.activeRankingType, e.detail.value, this.state.dateEnd)

    this.setState({
      dateBegin: e.detail.value
    })
  }

  onDateEndChange(e) {
    this.loadClassRankings(this.state.activeExamType, this.state.activeRankingType, this.state.dateBegin, e.detail.value)

    this.setState({
      dateEnd: e.detail.value
    })
  }

  render() {
    return (
      <View>
        <View className='conditon-container'>
          <View className='panel__title'>考试类型</View>
          {
            this.state.customExamTypes.map(examType => {
              return <View key={examType.value} style='display: inline-block;'>
                <AtTag
                  onClick={() => this.onExamTypeClick(examType.value)}
                  active={this.state.activeExamType == examType.value}
                  type='primary'
                  circle
                  className='examTypeTag'>{examType.text}</AtTag>
                {
                  (this.state.examTypes.length > 0 && this.state.examTypes[0].value === examType.value) ? '' :
                    <View className='del-button-container'>
                      <View onClick={() => this.onExamTypeDel(examType.value)} className='del-button'>X</View>
                    </View>
                }
              </View>
            })
          }
          <View className='add-button-container'>
            <Picker range={examTypes} mode='selector' rangeKey='text' onChange={this.onExamTypeAdd}>
              <AtButton type='secondary' size='small' circle className='add-button'>+</AtButton>
            </Picker>
          </View>
          <View className='panel__title' style='margin-top: 14px;'>排序方式</View>
          <AtTag
            onClick={() => this.onRankingTypeClick(this.state.activeExamType, 0)}
            active={this.state.activeRankingType == 0}
            type='primary'
            circle
            className='examTypeTag'>销售额</AtTag>
          <AtTag
            onClick={() => this.onRankingTypeClick(this.state.activeExamType, 1)}
            active={this.state.activeRankingType == 1}
            type='primary'
            circle
            className='examTypeTag'>销量</AtTag>

          <View className='panel__title' style='margin-top: 14px;'>日期</View>
          <View className='date-container'>
            <Picker mode='date' onChange={this.onDateBeginChange}>
              <View className='picker' style='float: left;'>
                {this.state.dateBegin}
              </View>
            </Picker>
            <Text style='float: left; margin: 0px 14px;'> - </Text>
            <Picker mode='date' onChange={this.onDateEndChange}>
              <View className='picker' style='float: left;'>
                {this.state.dateEnd}
              </View>
            </Picker>
          </View>
        </View>

        <View style='margin: 40px auto;'>
          {
            this.state.isLoading ?
              <View className='loading-container'>
                <AtActivityIndicator mode='center' content='Loading . . .'></AtActivityIndicator>
              </View>
              : ''
          }
          {
            this.state.isLoading ? '' :
              this.state.classRankings.map((classRanking, index) => {
                return <View key={classRanking.rid}>
                  <AtCard
                    extra={'Id: ' + classRanking.rid}
                    title={++index + '. 销量：' + classRanking.count + ' | 销售额：' + classRanking.sumprice}>
                    {classRanking.title}
                  </AtCard>
                  <View style="height: 11px;"></View>
                </View>
              })
          }
          {
            !this.state.isLoading && this.state.classRankings.length == 0 ?
              <View className='empty-container'>
                <Image src={emptyImg} className='empty'></Image>
              </View>
              : ''
          }
        </View>
      </View >
    )
  }
}