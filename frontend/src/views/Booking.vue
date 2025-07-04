<template>
  <div class="booking-page">
    <!-- 美化头部 -->
    <div class="header">
      <div class="header-title">
        <div class="title-icon">🏟️</div>
        <h2>球场预约</h2>
      </div>
      <div class="controls">
        <div class="control-group">
          <label>运动类型</label>
          <select v-model="selectedSport" @change="loadSportData" class="sport-selector">
            <option v-for="sport in sportTypes" :key="sport.value" :value="sport.value">
              {{ sport.label }}
            </option>
          </select>
        </div>
        <div class="control-group">
          <label>预约日期</label>
          <input 
            type="date" 
            v-model="selectedDate" 
            @change="loadCourtData"
            :min="today"
            class="date-selector"
          />
        </div>
        <button @click="refreshData" class="refresh-btn">
          <span class="refresh-icon">🔄</span>
          刷新
        </button>
      </div>
    </div>

    <!-- 美化统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card total-courts">
        <div class="stat-icon">🏟️</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalCourts }}</div>
          <div class="stat-label">总场地数</div>
        </div>
      </div>
      <div class="stat-card available-courts">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.availableCourts }}</div>
          <div class="stat-label">可用场地</div>
        </div>
      </div>
      <div class="stat-card available-slots">
        <div class="stat-icon">⏰</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.availableSlots }}</div>
          <div class="stat-label">可预约时段</div>
        </div>
      </div>
      <div class="stat-card free-rate">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <div class="stat-value">{{ Math.round(100 - statistics.occupancyRate) }}%</div>
          <div class="stat-label">空闲率</div>
        </div>
      </div>
    </div>

    <!-- 美化预约规则说明 -->
    <div class="booking-rules-card">
      <div class="rules-header">
        <h4>📋 预约说明</h4>
      </div>
      <div class="rules-content">
        <div class="rule-item">
          <span class="rule-icon">📍</span>
          <span class="rule-text">一次最多选择同一块场地的2个时间段</span>
        </div>
        <div class="rule-item">
          <span class="rule-icon">⏰</span>
          <span class="rule-text">选择2个时间段时，必须选择相邻的时间段</span>
        </div>
        <div class="rule-item">
          <span class="rule-icon">💡</span>
          <span class="rule-text">点击时间段进行选择，再次点击可取消选择</span>
        </div>
      </div>
    </div>

    <!-- 美化场地列表 -->
    <div class="court-list-card">
      <div class="card-header">
        <h3>🎯 可用场地</h3>
        <div class="status-legend">
          <div class="legend-item">
            <div class="legend-dot available"></div>
            <span>可预约</span>
          </div>
          <div class="legend-item">
            <div class="legend-dot selected"></div>
            <span>已选择</span>
          </div>
          <div class="legend-item">
            <div class="legend-dot occupied"></div>
            <span>已占用</span>
          </div>
          <div class="legend-item">
            <div class="legend-dot unavailable"></div>
            <span>不可用</span>
          </div>
        </div>
      </div>
      
      <div class="courts-container">
        <div v-for="court in courtsData" :key="court.id" class="court-card">
          <div class="court-info">
            <div class="court-header">
              <h4 class="court-name">{{ court.name }}</h4>
              <div class="court-status">
                <el-tag :type="court.available ? 'success' : 'info'" size="small">
                  {{ court.available ? '✅ 可用' : '❌ 停用' }}
                </el-tag>
              </div>
            </div>
            <div class="court-details">
              <p class="court-description">{{ court.description }}</p>
              <div class="court-price">
                <span class="price-label">价格：</span>
                <span class="price-value">¥{{ court.pricePerHour }}/小时</span>
              </div>
            </div>
          </div>
          
          <div class="time-slots-section">
            <h5 class="slots-title">可预约时段</h5>
            <div class="time-slots-grid">
              <div
                v-for="hour in timeSlots"
                :key="hour"
                :class="getSlotClass(getSlotForCourtAndTime(court, hour))"
                class="time-slot-button"
                @click="selectSlot(getSlotForCourtAndTime(court, hour), court)"
              >
                <div class="slot-time">{{ hour }}:00</div>
                <div class="slot-status-text">{{ getSlotStatusText(getSlotForCourtAndTime(court, hour)) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 美化预约确认 -->
    <transition name="slide-up">
      <div class="booking-confirm-card" v-if="selectedSlots.length > 0">
        <div class="confirm-header">
          <h3>🎯 预约确认</h3>
          <div class="selected-count">已选择 {{ selectedSlots.length }} 个时段</div>
        </div>
        
        <div class="selected-slots">
          <transition-group name="list" tag="div">
            <div v-for="slot in selectedSlots" :key="slot.id" class="selected-slot">
              <div class="slot-info">
                <div class="slot-court">
                  <span class="court-icon">🏟️</span>
                  <span class="court-name">{{ slot.courtName }}</span>
                </div>
                <div class="slot-time">
                  <span class="time-icon">⏰</span>
                  <span class="time-range">{{ slot.timeRange }}</span>
                </div>
                <div class="slot-price">
                  <span class="price-icon">💰</span>
                  <span class="price">¥{{ slot.price }}</span>
                </div>
              </div>
              <button @click="removeSelectedSlot(slot)" class="remove-btn">
                <span>×</span>
              </button>
            </div>
          </transition-group>
        </div>
        
        <div class="booking-summary">
          <div class="total-info">
            <div class="total-label">总计费用</div>
            <div class="total-details">
              <span class="total-price">¥{{ totalPrice }}</span>
              <span class="total-duration">（{{ selectedSlots.length }}小时）</span>
            </div>
          </div>
          <button @click="confirmBooking" :disabled="bookingLoading" class="confirm-btn">
            <span v-if="bookingLoading" class="loading-spinner">⏳</span>
            <span v-else class="confirm-icon">🚀</span>
            {{ bookingLoading ? '预约中...' : '确认预约' }}
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { appointmentAPI } from '../services/api'

export default {
  name: 'Booking',
  data() {
    return {
      // 运动类型选项
      sportTypes: [
        { value: 'badminton', label: '🏸 羽毛球' },
        { value: 'tennis', label: '🎾 网球' },
        { value: 'basketball', label: '🏀 篮球' },
        { value: 'table-tennis', label: '🏓 乒乓球' }
      ],
      
      // 响应式数据
      selectedSport: 'badminton', // 默认羽毛球
      selectedDate: new Date().toISOString().split('T')[0],
      courtsData: [],
      loading: false,
      bookingLoading: false,
      selectedSlots: [],
      
      // 时间段设置
      timeSlots: [8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22],
      
      // 今天日期
      today: new Date().toISOString().split('T')[0]
    }
  },
  computed: {
    // 统计数据
    statistics() {
      const totalCourts = this.courtsData.length
      const availableCourts = this.courtsData.filter(court => court.available).length
      const totalSlots = this.courtsData.reduce((total, court) => {
        return total + (court.timeSlots ? court.timeSlots.length : 0)
      }, 0)
      // 计算可预约时段数量
      const availableSlots = this.courtsData.reduce((total, court) => {
        return total + (court.timeSlots ? court.timeSlots.filter(slot => slot.available).length : 0)
      }, 0)
      const occupancyRate = totalSlots > 0 ? ((totalSlots - availableSlots) / totalSlots) * 100 : 0

      return {
        totalCourts,
        availableCourts,
        totalSlots,
        availableSlots,
        occupancyRate
      }
    },
    
    // 总价计算
    totalPrice() {
      return this.selectedSlots.reduce((total, slot) => total + slot.price, 0)
    }
  },
  methods: {
    // 获取指定场地和时间的时间段
    getSlotForCourtAndTime(court, hour) {
      if (!court.timeSlots) return null
      return court.timeSlots.find(slot => {
        const slotHour = parseInt(slot.startTime.split(':')[0])
        return slotHour === hour
      })
    },
    
    // 获取时间段样式类
    getSlotClass(slot) {
      if (!slot) return 'slot-empty'
      
      const isSelected = this.isSlotSelected(slot)
      if (isSelected) return 'slot-selected'
      
      if (!slot.available) return 'slot-unavailable'
      if (!this.canSelectSlot(slot)) return 'slot-disabled'
      
      return 'slot-available'
    },

    // 获取时间段状态文本
    getSlotStatusText(slot) {
      if (!slot) return '暂无'
      
      const isSelected = this.isSlotSelected(slot)
      if (isSelected) return '已选择'
      
      if (!slot.available) return '已占用'
      if (!this.canSelectSlot(slot)) return '不可选'
      
      return '可预约'
    },

    // 检查时间段是否被选中
    isSlotSelected(slot) {
      if (!slot) return false
      return this.selectedSlots.some(s => s.id === slot.id)
    },

    // 检查时间段是否可选
    canSelectSlot(slot) {
      if (!slot || !slot.available) return false
      
      // 如果已经选择了2个时间段，且当前时间段未被选中，则不可再选
      if (this.selectedSlots.length >= 2 && !this.isSlotSelected(slot)) {
        return false
      }
      
      // 如果已选择了1个时间段，检查是否相邻
      if (this.selectedSlots.length === 1) {
        const selectedSlot = this.selectedSlots[0]
        const selectedHour = parseInt(selectedSlot.startTime.split(':')[0])
        const currentHour = parseInt(slot.startTime.split(':')[0])
        return Math.abs(selectedHour - currentHour) === 1
      }
      
      return true
    },
    
    // 选择时间段
    selectSlot(slot, court) {
      if (!slot || !slot.available) return
      
      const isSelected = this.selectedSlots.some(s => s.id === slot.id)
      if (isSelected) {
        // 取消选择
        this.selectedSlots = this.selectedSlots.filter(s => s.id !== slot.id)
      } else {
        // 检查预约限制
        if (!this.canSelectSlot(slot, court)) {
          return
        }
        
        // 选择时间段
        const slotInfo = {
          id: slot.id,
          courtId: court.id,
          courtName: court.name,
          timeRange: `${slot.startTime} - ${slot.endTime}`,
          price: court.pricePerHour,
          date: this.selectedDate,
          startTime: slot.startTime,
          endTime: slot.endTime
        }
        this.selectedSlots.push(slotInfo)
      }
    },
    
    // 移除选择的时间段
    removeSelectedSlot(slot) {
      this.selectedSlots = this.selectedSlots.filter(s => s.id !== slot.id)
    },
    
    // 加载运动数据
    loadSportData() {
      this.selectedSlots = []
      this.loadCourtData()
    },
    
    // 加载场地数据
    async loadCourtData() {
      if (!this.selectedDate) return
      
      this.loading = true
      try {
        const response = await appointmentAPI.getCourtsForBooking(this.selectedDate)
        // 管理员API直接返回数组，不是包装在data字段中
        this.courtsData = Array.isArray(response) ? response : response.data || []
      } catch (error) {
        console.error('加载场地数据失败:', error)
        alert('加载场地数据失败，请检查登录状态')
        
        // 如果是认证错误，跳转到登录页
        if (error.response?.status === 401) {
          this.$router.push('/login')
        }
      } finally {
        this.loading = false
      }
    },
    
    // 刷新数据
    refreshData() {
      this.selectedSlots = []
      this.loadCourtData()
    },
    
    // 确认预约
    async confirmBooking() {
      if (this.selectedSlots.length === 0) {
        alert('请选择要预约的时间段')
        return
      }

      if (!confirm(`确认预约 ${this.selectedSlots.length} 个时间段，总计 ¥${this.totalPrice}？`)) {
        return
      }

      this.bookingLoading = true

      try {
        // 获取第一个和最后一个时间段的时间
        const sortedSlots = [...this.selectedSlots].sort((a, b) => 
          a.startTime.localeCompare(b.startTime)
        )
        
        const startTime = sortedSlots[0].startTime
        const endTime = sortedSlots[sortedSlots.length - 1].endTime

        // 准备预约请求数据
        const appointmentRequest = {
          courtId: this.selectedSlots[0].courtId,
          appointmentDate: this.selectedDate,
          startTime: startTime,
          endTime: endTime,
          timeSlotIds: this.selectedSlots.map(slot => slot.id),
          note: ''
        }

        // 调用后端API创建预约
        const response = await appointmentAPI.createAppointment(appointmentRequest)
        
        if (response.success) {
          // 预约创建成功，获取预约和支付数据
          const appointmentData = response.appointment
          const paymentData = response.payment
          
          // 将数据存储到sessionStorage，这样更可靠
          sessionStorage.setItem('appointmentData', JSON.stringify(appointmentData))
          sessionStorage.setItem('paymentData', JSON.stringify(paymentData))
          
          // 跳转到支付页面
          this.$router.push({ name: 'Payment' })
        } else {
          alert(response.message || '创建预约失败')
        }
        
      } catch (error) {
        console.error('创建预约失败:', error)
        let errorMessage = '创建预约失败，请重试'
        
        if (error.response?.data?.message) {
          errorMessage = error.response.data.message
        } else if (error.message) {
          errorMessage = error.message
        }
        
        alert(errorMessage)
      } finally {
        this.bookingLoading = false
      }
    }
  },
  
  // 组件挂载时加载数据
  mounted() {
    this.loadCourtData()
  }
}
</script>

<style scoped>
.booking-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  background-attachment: fixed;
  padding: 20px;
  animation: backgroundShift 30s ease-in-out infinite;
}

@keyframes backgroundShift {
  0%, 100% { 
    background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%); 
  }
  33% { 
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 50%, #4facfe 100%); 
  }
  66% { 
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 50%, #667eea 100%); 
  }
}

/* 美化头部 */
.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 25px;
  margin-bottom: 30px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 15px;
}

.title-icon {
  font-size: 32px;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.header h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 28px;
  font-weight: 700;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.controls {
  display: flex;
  gap: 20px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.control-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.control-group label {
  font-size: 14px;
  font-weight: 600;
  color: #555;
}

.sport-selector, .date-selector {
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 14px;
  background: white;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.sport-selector:focus, .date-selector:focus {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  outline: none;
}

.refresh-btn {
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.refresh-icon {
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 美化统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 25px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 20px;
  overflow: hidden;
  position: relative;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2);
}

.stat-card.total-courts::before {
  background: linear-gradient(90deg, #667eea, #764ba2);
}

.stat-card.available-courts::before {
  background: linear-gradient(90deg, #43e97b, #38f9d7);
}

.stat-card.available-slots::before {
  background: linear-gradient(90deg, #fa709a, #fee140);
}

.stat-card.free-rate::before {
  background: linear-gradient(90deg, #a8edea, #fed6e3);
}

.stat-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  font-size: 48px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 5px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
}

.stat-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

/* 美化预约规则说明 */
.booking-rules-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 25px;
  margin-bottom: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.rules-header h4 {
  margin: 0 0 20px 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

.rules-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.rule-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 12px;
  border-left: 4px solid #667eea;
  transition: all 0.3s ease;
}

.rule-item:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.rule-icon {
  font-size: 20px;
  min-width: 24px;
  text-align: center;
}

.rule-text {
  color: #424242;
  font-size: 14px;
  line-height: 1.5;
  font-weight: 500;
}

/* 美化场地列表 */
.court-list-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 30px;
  margin: 30px 0;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  flex-wrap: wrap;
  gap: 15px;
}

.card-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
}

.status-legend {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #666;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.legend-dot.available {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}

.legend-dot.selected {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.legend-dot.occupied {
  background: linear-gradient(135deg, #fa709a, #fee140);
}

.legend-dot.unavailable {
  background: linear-gradient(135deg, #c0c4cc, #909399);
}

.courts-container {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.court-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 250, 252, 0.9) 100%);
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.court-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.court-info {
  margin-bottom: 20px;
}

.court-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.court-name {
  margin: 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

.court-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.court-description {
  margin: 0;
  color: #666;
  font-size: 14px;
  flex: 1;
}

.court-price {
  display: flex;
  align-items: center;
  gap: 5px;
}

.price-label {
  color: #666;
  font-size: 14px;
}

.price-value {
  color: #e6a23c;
  font-size: 16px;
  font-weight: 600;
}

.time-slots-section {
  border-top: 1px solid rgba(102, 126, 234, 0.1);
  padding-top: 20px;
}

.slots-title {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 14px;
  font-weight: 600;
}

.time-slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.time-slot-button {
  padding: 12px;
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  font-size: 12px;
  font-weight: 500;
}

.slot-time {
  font-weight: 600;
  margin-bottom: 4px;
}

.slot-status-text {
  font-size: 10px;
  opacity: 0.8;
}

.slot-available {
  background: linear-gradient(135deg, rgba(67, 233, 123, 0.2), rgba(56, 249, 215, 0.2));
  color: #00b894;
  border-color: rgba(67, 233, 123, 0.3);
}

.slot-available:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(67, 233, 123, 0.3);
  border-color: #43e97b;
}

.slot-selected {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.slot-unavailable {
  background: linear-gradient(135deg, rgba(250, 112, 154, 0.2), rgba(254, 225, 64, 0.2));
  color: #e17055;
  cursor: not-allowed;
  border-color: rgba(250, 112, 154, 0.3);
}

.slot-disabled {
  background: rgba(192, 196, 204, 0.3);
  color: #909399;
  cursor: not-allowed;
  border-color: rgba(192, 196, 204, 0.3);
}

.slot-empty {
  background: rgba(245, 245, 245, 0.8);
  color: #c0c4cc;
  cursor: not-allowed;
  border-color: rgba(192, 196, 204, 0.2);
}

/* 美化预约确认 */
.booking-confirm-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  margin-top: 30px;
  border: 2px solid rgba(102, 126, 234, 0.2);
}

.confirm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  flex-wrap: wrap;
  gap: 15px;
}

.confirm-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
}

.selected-count {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.selected-slots {
  margin-bottom: 25px;
}

.selected-slot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border-radius: 16px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.selected-slot:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
}

.slot-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.slot-court, .slot-time, .slot-price {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.court-name {
  font-weight: 600;
  color: #2c3e50;
}

.time-range {
  color: #666;
}

.price {
  font-weight: 600;
  color: #e6a23c;
}

.remove-btn {
  background: linear-gradient(135deg, #ff6b6b, #ee5a6f);
  color: white;
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}

.remove-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(255, 107, 107, 0.4);
}

.booking-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 16px;
  border: 1px solid rgba(102, 126, 234, 0.1);
  flex-wrap: wrap;
  gap: 20px;
}

.total-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.total-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.total-details {
  display: flex;
  align-items: center;
  gap: 10px;
}

.total-price {
  font-size: 28px;
  font-weight: bold;
  color: #e6a23c;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
}

.total-duration {
  font-size: 14px;
  color: #666;
}

.confirm-btn {
  padding: 15px 30px;
  background: linear-gradient(135deg, #43e97b, #38f9d7);
  color: white;
  border: none;
  border-radius: 16px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 8px 24px rgba(67, 233, 123, 0.3);
  display: flex;
  align-items: center;
  gap: 10px;
}

.confirm-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(67, 233, 123, 0.4);
}

.confirm-btn:disabled {
  background: linear-gradient(135deg, #c0c4cc, #909399);
  cursor: not-allowed;
  box-shadow: none;
}

.loading-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 动画效果 */
.slide-up-enter-active, .slide-up-leave-active {
  transition: all 0.5s ease;
}

.slide-up-enter-from, .slide-up-leave-to {
  opacity: 0;
  transform: translateY(30px);
}

.list-enter-active, .list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from, .list-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

.list-move {
  transition: transform 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .booking-page {
    padding: 15px;
  }
  
  .header {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
  }
  
  .controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .card-header {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
  }
  
  .status-legend {
    justify-content: center;
  }
  
  .court-details {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
  }
  
  .time-slots-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }
  
  .booking-summary {
    flex-direction: column;
    text-align: center;
  }
  
  .selected-slot {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
}

@media (max-width: 480px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .time-slots-grid {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  }
  
  .stat-card {
    flex-direction: column;
    text-align: center;
  }
  
  .rules-content {
    gap: 10px;
  }
  
  .rule-item {
    padding: 12px 15px;
  }
}
</style>
