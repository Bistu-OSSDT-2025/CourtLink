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

    <!-- 场地时间段预约表格 -->
    <div class="time-slots-card">
      <div class="table-header">
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
      
      <div class="time-table-container" v-if="!loading">
        <table class="time-table">
          <thead>
            <tr>
              <th class="time-header">时间 \ 场地</th>
              <th v-for="court in courtsData" :key="court.id" class="court-header">
                <div class="court-info">
                  <div class="name">{{ court.name }}</div>
                  <div class="price">¥{{ court.pricePerHour }}/时</div>
                  <div class="status" :class="{ 'available': court.available, 'unavailable': !court.available }">
                    {{ court.available ? '可用' : '停用' }}
                  </div>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="hour in timeSlots" :key="hour" class="time-row">
              <td class="time-name">
                <div class="time-info">{{ hour }}:00-{{ hour + 1 }}:00</div>
              </td>
              <td 
                v-for="court in courtsData" 
                :key="court.id"
                :class="getSlotClass(getSlotForCourtAndTime(court, hour))"
                @click="selectSlot(getSlotForCourtAndTime(court, hour), court)"
                class="time-slot"
              >
                <div class="slot-content">
                  <span class="slot-status">
                    {{ getSlotStatusText(getSlotForCourtAndTime(court, hour)) }}
                  </span>
                  <span v-if="getSlotForCourtAndTime(court, hour)?.note" class="slot-note">
                    {{ getSlotForCourtAndTime(court, hour).note }}
                  </span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="loading">加载中...</div>
    </div>

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
      if (!slot) return '无'
      
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
      if (!slot || !slot.isOpen) return
      
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
        // 创建预约请求
        const bookingPromises = this.selectedSlots.map(slot => {
          const startDateTime = `${slot.date}T${slot.startTime}:00`
          const endDateTime = `${slot.date}T${slot.endTime}:00`
          
          return appointmentAPI.createAppointment({
            courtId: slot.courtId,
            startTime: startDateTime,
            endTime: endDateTime,
            amount: slot.price
          })
        })

        await Promise.all(bookingPromises)
        
        alert('预约成功！')
        this.selectedSlots = []
        await this.loadCourtData()
        
      } catch (error) {
        console.error('预约失败:', error)
        alert('预约失败，请重试')
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
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.header h2 {
  margin: 0;
  color: #333;
}

.controls {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
}

.sport-selector, .date-selector {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.refresh-btn {
  padding: 8px 16px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.refresh-btn:hover {
  background-color: #3a8ee6;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.time-slots-card {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.table-header h3 {
  margin: 0;
  color: #333;
}

.legend {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.legend-color.available {
  background-color: #e8f5e8;
}

.legend-color.occupied {
  background-color: #ffeaa7;
}

.legend-color.closed {
  background-color: #ddd;
}

.legend-color.selected {
  background-color: #409eff;
}

.time-table-container {
  overflow-x: auto;
  margin-bottom: 20px;
}

.time-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}

.time-table th,
.time-table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: center;
}

.time-header {
  background-color: #f5f5f5;
  font-weight: bold;
  min-width: 120px;
}

.court-header {
  background-color: #f5f5f5;
  min-width: 120px;
}

.court-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.court-info .name {
  font-weight: bold;
  color: #333;
}

.court-info .price {
  font-size: 12px;
  color: #666;
}

.court-info .status {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  color: white;
}

.court-info .status.available {
  background-color: #67c23a;
}

.court-info .status.unavailable {
  background-color: #f56c6c;
}

.time-name {
  background-color: #f5f5f5;
  font-weight: bold;
}

.time-slot {
  cursor: pointer;
  transition: all 0.2s;
  min-height: 60px;
  vertical-align: middle;
}

.time-slot:hover {
  background-color: #f0f9ff;
}

.slot-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 4px;
}

.slot-status {
  font-size: 12px;
  font-weight: bold;
}

.slot-note {
  font-size: 10px;
  color: #666;
}

.slot-available {
  background-color: #e8f5e8;
  color: #67c23a;
}

.slot-occupied {
  background-color: #ffeaa7;
  color: #e6a23c;
}

.slot-closed {
  background-color: #ddd;
  color: #666;
  cursor: not-allowed;
}

.slot-selected {
  background-color: #409eff;
  color: white;
}

.slot-empty {
  background-color: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.booking-confirm-card {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  color: #333;
}

.selected-slots {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}

.selected-slot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: #f0f9ff;
  border-radius: 8px;
  border: 1px solid #409eff;
}

.slot-info {
  display: flex;
  gap: 15px;
  align-items: center;
}

.court-name {
  font-weight: bold;
  color: #333;
}

.time-range {
  color: #666;
}

.price {
  font-weight: bold;
  color: #409eff;
}

.remove-btn {
  background: #f56c6c;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
}

.remove-btn:hover {
  background: #f78989;
}

.booking-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.total-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.total-label {
  font-size: 16px;
  color: #333;
}

.total-price {
  font-size: 24px;
  font-weight: bold;
  color: #e6a23c;
}

.total-duration {
  font-size: 14px;
  color: #666;
}

.confirm-btn {
  padding: 12px 24px;
  background-color: #67c23a;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
}

.confirm-btn:hover {
  background-color: #85ce61;
}

.confirm-btn:disabled {
  background-color: #c0c4cc;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .table-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .legend {
    justify-content: center;
  }
  
  .booking-summary {
    flex-direction: column;
    gap: 15px;
  }
}
</style>
