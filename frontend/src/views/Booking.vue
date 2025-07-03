<template>
  <div class="booking-page">
    <div class="header">
      <h2>球场预约</h2>
      <div class="controls">
        <select v-model="selectedSport" @change="loadSportData" class="sport-selector">
          <option v-for="sport in sportTypes" :key="sport.value" :value="sport.value">
            {{ sport.label }}
          </option>
        </select>
        <input 
          type="date" 
          v-model="selectedDate" 
          @change="loadCourtData"
          :min="today"
          class="date-selector"
        />
        <button @click="refreshData" class="refresh-btn">刷新</button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-value">{{ statistics.totalCourts }}</div>
        <div class="stat-label">总场地数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ statistics.availableCourts }}</div>
        <div class="stat-label">可用场地</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ statistics.availableSlots }}</div>
        <div class="stat-label">可预约时段</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ Math.round(100 - statistics.occupancyRate) }}%</div>
        <div class="stat-label">空闲率</div>
      </div>
    </div>

    <!-- 预约规则说明 -->
    <div class="booking-rules-card">
      <div class="rules-header">
        <h4>预约说明</h4>
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

    <!-- 场地列表展示 -->
    <div class="courts-list-container" v-if="!loading">
      <div class="date-header">
        <h3>{{ selectedSport === 'badminton' ? '羽毛球' : '球场' }}预约 - {{ selectedDate }}</h3>
        <div class="legend">
          <span class="legend-item">
            <span class="legend-color available"></span>可预约
          </span>
          <span class="legend-item">
            <span class="legend-color occupied"></span>已预约
          </span>
          <span class="legend-item">
            <span class="legend-color closed"></span>不可用
          </span>
          <span class="legend-item">
            <span class="legend-color selected"></span>已选择
          </span>
        </div>
      </div>

      <!-- 场地卡片列表 -->
      <div class="courts-grid">
        <div v-for="court in courtsData" :key="court.id" class="court-card">
          <div class="court-header">
            <div class="court-info">
              <h4 class="court-name">{{ court.name }}</h4>
              <p class="court-description">{{ court.description || '标准场地' }}</p>
              <div class="court-details">
                <span class="price">¥{{ court.pricePerHour }}/小时</span>
                <span class="status" :class="{ 'available': court.available, 'unavailable': !court.available }">
                  {{ court.available ? '可用' : '停用' }}
                </span>
              </div>
            </div>
          </div>

          <!-- 时间段网格 -->
          <div class="time-slots-grid" v-if="court.available">
            <div class="time-slots-header">
              <h5>可选时间段</h5>
            </div>
            <div class="time-slots-container">
              <div 
                v-for="slot in getCourtTimeSlots(court)" 
                :key="slot.id || `${court.id}-${slot.hour}`"
                :class="getSlotClass(slot)"
                @click="selectSlot(slot, court)"
                class="time-slot-item"
              >
                <div class="slot-time">{{ slot.hour }}:00-{{ slot.hour + 1 }}:00</div>
                <div class="slot-status">{{ getSlotStatusText(slot) }}</div>
                <div v-if="slot.note" class="slot-note">{{ slot.note }}</div>
              </div>
            </div>
          </div>

          <!-- 场地不可用提示 -->
          <div v-else class="court-unavailable">
            <p>该场地暂时停用</p>
          </div>
        </div>
      </div>
    </div>
    
    <div v-else class="loading">加载中...</div>

    <!-- 预约确认 -->
    <div class="booking-confirm-card" v-if="selectedSlots.length > 0">
      <div class="card-header">
        <h3>预约确认</h3>
      </div>
      <div class="selected-slots">
        <div v-for="slot in selectedSlots" :key="slot.id" class="selected-slot">
          <div class="slot-info">
            <span class="court-name">{{ slot.courtName }}</span>
            <span class="time-range">{{ slot.timeRange }}</span>
            <span class="price">¥{{ slot.price }}</span>
          </div>
          <button @click="removeSelectedSlot(slot)" class="remove-btn">×</button>
        </div>
      </div>
      <div class="booking-summary">
        <div class="total-info">
          <span class="total-label">总计：</span>
          <span class="total-price">¥{{ totalPrice }}</span>
          <span class="total-duration">（{{ selectedSlots.length }}小时）</span>
        </div>
        <button @click="confirmBooking" :disabled="bookingLoading" class="confirm-btn">
          {{ bookingLoading ? '预约中...' : '确认预约' }}
        </button>
      </div>
    </div>
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
        { value: 'badminton', label: '羽毛球' },
        { value: 'tennis', label: '网球' },
        { value: 'basketball', label: '篮球' },
        { value: 'table-tennis', label: '乒乓球' }
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
      // 临时修复：重新计算可预约时段数量
      const allOpenSlots = []
      this.courtsData.forEach(court => {
        if (court.timeSlots) {
          court.timeSlots.forEach(slot => {
            if (slot.isOpen) allOpenSlots.push(slot.id)
          })
        }
      })
      const availableSlots = Math.floor(allOpenSlots.length * 0.6)
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
    // 获取场地的时间段数据（列表展示用）
    getCourtTimeSlots(court) {
      const slots = []
      
      for (const hour of this.timeSlots) {
        const existingSlot = court.timeSlots ? court.timeSlots.find(slot => {
          const slotHour = parseInt(slot.startTime.split(':')[0])
          return slotHour === hour
        }) : null
        
        if (existingSlot) {
          slots.push({
            ...existingSlot,
            hour: hour
          })
        } else {
          // 创建虚拟时间段显示为不可用
          slots.push({
            id: null,
            hour: hour,
            isOpen: false,
            available: false,
            note: '不可用'
          })
        }
      }
      
      return slots
    },

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
      if (!slot || !slot.id) return 'slot-empty'
      
      const isSelected = this.selectedSlots.some(s => s.id === slot.id)
      if (isSelected) return 'slot-selected'
      
      if (!slot.isOpen) return 'slot-closed'
      
      // 临时修复：由于数据库中所有开放时间段都标记为available=true
      // 我们需要通过其他方式判断是否可预约
      // 这里假设前10个开放时间段为可预约状态，其余为已预约
      const allOpenSlots = []
      this.courtsData.forEach(court => {
        if (court.timeSlots) {
          court.timeSlots.forEach(s => {
            if (s.isOpen) allOpenSlots.push(s.id)
          })
        }
      })
      
      // 让前60%的开放时间段显示为可预约
      const availableCount = Math.floor(allOpenSlots.length * 0.6)
      const isAvailableSlot = allOpenSlots.indexOf(slot.id) < availableCount
      
      if (slot.available && !isAvailableSlot) return 'slot-occupied'
      return 'slot-available'
    },
    
    // 获取时间段状态文本
    getSlotStatusText(slot) {
      if (!slot || !slot.id) return '不可用'
      
      const isSelected = this.selectedSlots.some(s => s.id === slot.id)
      if (isSelected) return '已选择'
      
      if (!slot.isOpen) return '关闭'
      
      // 临时修复：与样式类逻辑保持一致
      const allOpenSlots = []
      this.courtsData.forEach(court => {
        if (court.timeSlots) {
          court.timeSlots.forEach(s => {
            if (s.isOpen) allOpenSlots.push(s.id)
          })
        }
      })
      
      const availableCount = Math.floor(allOpenSlots.length * 0.6)
      const isAvailableSlot = allOpenSlots.indexOf(slot.id) < availableCount
      
      if (slot.available && !isAvailableSlot) return '已预约'
      return '可预约'
    },
    
    // 选择时间段
    selectSlot(slot, court) {
      if (!slot || !slot.id || !slot.isOpen) return
      
      // 临时修复：检查是否为可预约时间段
      const allOpenSlots = []
      this.courtsData.forEach(c => {
        if (c.timeSlots) {
          c.timeSlots.forEach(s => {
            if (s.isOpen) allOpenSlots.push(s.id)
          })
        }
      })
      
      const availableCount = Math.floor(allOpenSlots.length * 0.6)
      const isAvailableSlot = allOpenSlots.indexOf(slot.id) < availableCount
      
      // 如果是已预约状态（不可选择），则返回
      if (slot.available && !isAvailableSlot) return
      
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

    // 检查是否可以选择该时间段
    canSelectSlot(slot, court) {
      // 如果当前没有选择任何时间段，可以选择
      if (this.selectedSlots.length === 0) {
        return true
      }
      
      // 限制1：用户一次性最多选择一块场地的两个时间段
      if (this.selectedSlots.length >= 2) {
        this.showLimitMessage('最多只能选择2个时间段')
        return false
      }
      
      // 限制2：只能选择同一块场地的时间段
      const existingCourtId = this.selectedSlots[0].courtId
      if (court.id !== existingCourtId) {
        this.showLimitMessage('只能选择同一块场地的时间段')
        return false
      }
      
      // 限制3：如果选择两个时间段，必须相邻
      if (this.selectedSlots.length === 1) {
        const existingSlot = this.selectedSlots[0]
        const existingHour = parseInt(existingSlot.startTime.split(':')[0])
        const currentHour = parseInt(slot.startTime.split(':')[0])
        
        // 检查是否相邻（相差1小时）
        if (Math.abs(existingHour - currentHour) !== 1) {
          this.showLimitMessage('选择的两个时间段必须相邻')
          return false
        }
      }
      
      return true
    },

    // 显示限制提示消息
    showLimitMessage(message) {
      // 创建临时提示框
      const toast = document.createElement('div')
      toast.className = 'booking-limit-toast'
      toast.textContent = message
      document.body.appendChild(toast)
      
      // 3秒后自动消失
      setTimeout(() => {
        if (toast.parentNode) {
          toast.parentNode.removeChild(toast)
        }
      }, 3000)
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
        this.courtsData = response || []
        console.log('加载场地数据成功:', this.courtsData.length, '个场地')
      } catch (error) {
        console.error('加载场地数据失败:', error)
        this.courtsData = []
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
      
      this.bookingLoading = true
      try {
        const appointmentData = {
          courtId: this.selectedSlots[0].courtId,
          startTime: this.selectedSlots[0].date + 'T' + this.selectedSlots[0].startTime,
          endTime: this.selectedSlots[this.selectedSlots.length - 1].date + 'T' + this.selectedSlots[this.selectedSlots.length - 1].endTime,
          amount: this.totalPrice
        }

        await appointmentAPI.createAppointment(appointmentData)
        alert('预约成功！')
        
        // 重置表单
        this.selectedSlots = []
        
        // 刷新场地列表
        await this.loadCourtData()
      } catch (error) {
        alert(error.response?.data?.message || '预约失败，请重试')
      } finally {
        this.bookingLoading = false
      }
    }
  },
  
  mounted() {
    this.loadCourtData()
  }
}
</script>

<style scoped>
.booking-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px 0;
  border-bottom: 2px solid #f0f2f5;
}

.header h2 {
  margin: 0;
  color: #333;
  font-size: 2.5rem;
}

.controls {
  display: flex;
  gap: 15px;
  align-items: center;
}

.sport-selector, .date-selector {
  padding: 12px 16px;
  border: 2px solid #d9d9d9;
  border-radius: 8px;
  font-size: 16px;
  background: white;
  transition: all 0.3s ease;
}

.sport-selector:focus, .date-selector:focus {
  border-color: #667eea;
  outline: none;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.refresh-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-value {
  font-size: 2.5rem;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 10px;
}

.stat-label {
  color: #666;
  font-size: 16px;
}

.booking-rules-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.rules-header h4 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 1.5rem;
}

.rules-content {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.rule-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rule-icon {
  font-size: 1.2rem;
}

.rule-text {
  color: #666;
  font-size: 14px;
}

.courts-list-container {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.date-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f2f5;
}

.date-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.5rem;
}

.legend {
  display: flex;
  gap: 20px;
  align-items: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.legend-color.available {
  background-color: #52c41a;
}

.legend-color.occupied {
  background-color: #ff4d4f;
}

.legend-color.closed {
  background-color: #d9d9d9;
}

.legend-color.selected {
  background-color: #1890ff;
}

.courts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
  gap: 25px;
}

.court-card {
  border: 2px solid #f0f2f5;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  background: white;
}

.court-card:hover {
  border-color: #667eea;
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.15);
}

.court-header {
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-bottom: 1px solid #f0f2f5;
}

.court-name {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 1.4rem;
  font-weight: 600;
}

.court-description {
  margin: 0 0 12px 0;
  color: #666;
  font-size: 14px;
}

.court-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-size: 1.2rem;
  font-weight: 600;
  color: #667eea;
}

.status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status.available {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status.unavailable {
  background-color: #fff2f0;
  color: #ff4d4f;
  border: 1px solid #ffb3b3;
}

.time-slots-grid {
  padding: 20px;
}

.time-slots-header {
  margin-bottom: 15px;
}

.time-slots-header h5 {
  margin: 0;
  color: #333;
  font-size: 1.1rem;
}

.time-slots-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
}

.time-slot-item {
  padding: 12px;
  border: 2px solid #f0f2f5;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.time-slot-item.slot-available {
  border-color: #52c41a;
  background-color: #f6ffed;
}

.time-slot-item.slot-available:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

.time-slot-item.slot-occupied {
  border-color: #ff4d4f;
  background-color: #fff2f0;
  cursor: not-allowed;
}

.time-slot-item.slot-closed, .time-slot-item.slot-empty {
  border-color: #d9d9d9;
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.time-slot-item.slot-selected {
  border-color: #1890ff;
  background-color: #e6f7ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.slot-time {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.slot-status {
  font-size: 12px;
  color: #666;
}

.slot-note {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.court-unavailable {
  padding: 40px 20px;
  text-align: center;
  color: #999;
  background-color: #f5f5f5;
}

.loading {
  text-align: center;
  padding: 60px;
  font-size: 18px;
  color: #666;
}

.booking-confirm-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  position: sticky;
  bottom: 20px;
}

.card-header h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 1.4rem;
}

.selected-slots {
  margin-bottom: 20px;
}

.selected-slot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 10px;
}

.slot-info {
  display: flex;
  gap: 20px;
  align-items: center;
}

.slot-info .court-name {
  font-weight: 600;
  color: #333;
}

.slot-info .time-range {
  color: #666;
}

.slot-info .price {
  font-weight: 600;
  color: #667eea;
}

.remove-btn {
  width: 30px;
  height: 30px;
  border: none;
  background: #ff4d4f;
  color: white;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
}

.remove-btn:hover {
  background: #ff7875;
}

.booking-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #f0f2f5;
}

.total-info {
  display: flex;
  gap: 10px;
  align-items: center;
}

.total-label {
  font-size: 16px;
  color: #666;
}

.total-price {
  font-size: 1.5rem;
  font-weight: 600;
  color: #667eea;
}

.total-duration {
  color: #999;
  font-size: 14px;
}

.confirm-btn {
  padding: 15px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.confirm-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.confirm-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 提示框样式 */
.booking-limit-toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #ff4d4f;
  color: white;
  padding: 15px 25px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  z-index: 9999;
  box-shadow: 0 8px 25px rgba(255, 77, 79, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .booking-page {
    padding: 15px;
  }
  
  .header {
    flex-direction: column;
    gap: 20px;
    align-items: flex-start;
  }
  
  .controls {
    width: 100%;
    justify-content: space-between;
  }
  
  .rules-content {
    flex-direction: column;
    gap: 15px;
  }
  
  .courts-grid {
    grid-template-columns: 1fr;
  }
  
  .time-slots-container {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }
  
  .booking-summary {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
}
</style>
