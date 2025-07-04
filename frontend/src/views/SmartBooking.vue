<template>
  <div class="smart-booking">
    <!-- 顶部导航 -->
    <div class="header">
      <div class="nav-section">
        <el-button @click="goBack" type="text" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </el-button>
        <h2>智能预约</h2>
      </div>
      <div class="date-selector">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          @change="loadCourtData"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :disabled-date="disabledDate"
        />
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalCourts }}</div>
          <div class="stat-label">总场地</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ statistics.availableSlots }}</div>
          <div class="stat-label">可预约时段</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ selectedSlots.length }}</div>
          <div class="stat-label">已选时段</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">¥{{ totalPrice }}</div>
          <div class="stat-label">预计费用</div>
        </div>
      </el-card>
    </div>

    <!-- 场地时间段预约表格 -->
    <el-card class="booking-table-card">
      <div class="table-header">
        <h3>场地预约 - {{ selectedDate }}</h3>
        <div class="legend">
          <span class="legend-item">
            <span class="legend-color available"></span>
            可预约
          </span>
          <span class="legend-item">
            <span class="legend-color occupied"></span>
            已预约
          </span>
          <span class="legend-item">
            <span class="legend-color closed"></span>
            未开放
          </span>
          <span class="legend-item">
            <span class="legend-color selected"></span>
            已选择
          </span>
        </div>
      </div>
      
      <div class="time-table-container" v-loading="loading">
        <table class="time-table">
          <thead>
            <tr>
              <th class="time-header">时间 \ 场地</th>
              <th v-for="court in courtsData" :key="court.id" class="court-header">
                <div class="court-info">
                  <div class="name">{{ court.name }}</div>
                  <div class="price">¥{{ court.pricePerHour }}/时</div>
                  <div class="description">{{ court.description }}</div>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="hour in timeSlots" :key="hour" class="time-row">
              <td class="time-name">
                <div class="time-info">
                  {{ hour }}:00-{{ hour + 1 }}:00
                </div>
              </td>
              <td 
                v-for="court in courtsData" 
                :key="court.id"
                :class="getSlotClass(getSlotForCourtAndTime(court, hour))"
                @click="selectSlot(court, hour)"
                class="time-slot"
              >
                <div class="slot-content">
                  <span class="slot-status">
                    {{ getSlotStatusText(getSlotForCourtAndTime(court, hour)) }}
                  </span>
                  <span v-if="getSlotForCourtAndTime(court, hour)?.price" class="slot-price">
                    ¥{{ getSlotForCourtAndTime(court, hour).price }}
                  </span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </el-card>

    <!-- 预约确认 -->
    <el-card class="booking-confirm-card" v-if="selectedSlots.length > 0">
      <div class="confirm-header">
        <h3>预约确认</h3>
        <el-button @click="clearSelection" type="text" class="clear-btn">清空选择</el-button>
      </div>
      
      <div class="selected-slots">
        <div class="slot-item" v-for="slot in selectedSlots" :key="slot.key">
          <div class="slot-info">
            <span class="court-name">{{ slot.courtName }}</span>
            <span class="time-range">{{ slot.timeRange }}</span>
            <span class="price">¥{{ slot.price }}</span>
          </div>
          <el-button @click="removeSlot(slot)" type="text" class="remove-btn">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </div>
      
      <div class="booking-summary">
        <div class="summary-item">
          <span class="label">预约日期：</span>
          <span class="value">{{ selectedDate }}</span>
        </div>
        <div class="summary-item">
          <span class="label">时段数量：</span>
          <span class="value">{{ selectedSlots.length }} 个</span>
        </div>
        <div class="summary-item">
          <span class="label">总费用：</span>
          <span class="value total-price">¥{{ totalPrice }}</span>
        </div>
      </div>
      
      <div class="booking-actions">
        <el-button @click="clearSelection" size="large">取消</el-button>
        <el-button @click="confirmBooking" type="primary" size="large" :loading="submitting">
          确认预约
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Close } from '@element-plus/icons-vue'
import { appointmentAPI } from '../services/api'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const courtsData = ref([])
const selectedDate = ref(new Date().toISOString().split('T')[0])
const timeSlots = ref([8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22]) // 8:00-23:00
const loading = ref(false)
const submitting = ref(false)
const selectedSlots = ref([])

const statistics = ref({
  totalCourts: 0,
  availableSlots: 0,
  occupiedSlots: 0,
  closedSlots: 0
})

// 计算属性
const totalPrice = computed(() => {
  return selectedSlots.value.reduce((total, slot) => total + slot.price, 0)
})

// 方法
const goBack = () => {
  router.push('/')
}

const disabledDate = (date) => {
  // 禁用过去的日期
  return date < new Date(new Date().setHours(0, 0, 0, 0))
}

const loadCourtData = async () => {
  loading.value = true
  try {
    const response = await appointmentAPI.getCourtsForBooking(selectedDate.value)
    courtsData.value = response || []
    
    // 计算统计信息
    let totalSlots = 0
    let availableSlots = 0
    let occupiedSlots = 0
    let closedSlots = 0
    
    courtsData.value.forEach(court => {
      if (court.timeSlots) {
        totalSlots += court.timeSlots.length
        court.timeSlots.forEach(slot => {
          if (slot.isOpen && slot.available) {
            availableSlots++
          } else if (slot.isOpen && !slot.available) {
            occupiedSlots++
          } else {
            closedSlots++
          }
        })
      }
    })
    
    statistics.value = {
      totalCourts: courtsData.value.length,
      availableSlots,
      occupiedSlots,
      closedSlots
    }
    
    // 清空之前的选择
    selectedSlots.value = []
    
  } catch (error) {
    ElMessage.error('加载场地数据失败: ' + (error.message || '未知错误'))
    console.error(error)
  } finally {
    loading.value = false
  }
}

const getSlotForCourtAndTime = (court, hour) => {
  if (!court.timeSlots) return null
  return court.timeSlots.find(slot => {
    const slotHour = parseInt(slot.startTime.split(':')[0])
    return slotHour === hour
  })
}

const getSlotClass = (slot) => {
  if (!slot) return 'slot-empty'
  
  const slotKey = `${slot.courtId}-${slot.id}`
  const isSelected = selectedSlots.value.some(s => s.key === slotKey)
  
  if (isSelected) return 'slot-selected'
  if (!slot.isOpen) return 'slot-closed'
  if (!slot.available) return 'slot-occupied'
  return 'slot-available'
}

const getSlotStatusText = (slot) => {
  if (!slot) return '-'
  
  const slotKey = `${slot.courtId}-${slot.id}`
  const isSelected = selectedSlots.value.some(s => s.key === slotKey)
  
  if (isSelected) return '已选'
  if (!slot.isOpen) return '未开放'
  if (!slot.available) return '已预约'
  return '可预约'
}

const selectSlot = (court, hour) => {
  const slot = getSlotForCourtAndTime(court, hour)
  if (!slot || !slot.isOpen || !slot.available) return
  
  const slotKey = `${court.id}-${slot.id}`
  const existingIndex = selectedSlots.value.findIndex(s => s.key === slotKey)
  
  if (existingIndex !== -1) {
    // 取消选择
    selectedSlots.value.splice(existingIndex, 1)
  } else {
    // 选择时段
    selectedSlots.value.push({
      key: slotKey,
      courtId: court.id,
      courtName: court.name,
      timeSlotId: slot.id,
      timeRange: `${hour}:00-${hour + 1}:00`,
      price: court.pricePerHour,
      startTime: slot.startTime,
      endTime: slot.endTime
    })
  }
}

const removeSlot = (slotToRemove) => {
  const index = selectedSlots.value.findIndex(s => s.key === slotToRemove.key)
  if (index !== -1) {
    selectedSlots.value.splice(index, 1)
  }
}

const clearSelection = () => {
  selectedSlots.value = []
}

const confirmBooking = async () => {
  if (selectedSlots.value.length === 0) {
    ElMessage.warning('请选择预约时段')
    return
  }
  
  try {
    await ElMessageBox.confirm('确认预约所选时段？', '预约确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    submitting.value = true
    
    // 按场地分组预约
    const bookingsByCourtId = {}
    selectedSlots.value.forEach(slot => {
      if (!bookingsByCourtId[slot.courtId]) {
        bookingsByCourtId[slot.courtId] = []
      }
      bookingsByCourtId[slot.courtId].push(slot)
    })
    
    const appointments = []
    
    for (const courtId in bookingsByCourtId) {
      const slots = bookingsByCourtId[courtId]
      const courtSlots = slots.sort((a, b) => a.startTime.localeCompare(b.startTime))
      
      // 计算开始和结束时间
      const startTime = courtSlots[0].startTime.substring(0, 5) // HH:MM
      const endTime = courtSlots[courtSlots.length - 1].endTime.substring(0, 5) // HH:MM
      
      const appointmentData = {
        courtId: parseInt(courtId),
        appointmentDate: selectedDate.value,
        startTime: startTime,
        endTime: endTime,
        timeSlotIds: courtSlots.map(slot => slot.timeSlotId),
        note: `智能预约 - ${courtSlots.length}个时段`
      }
      
      const result = await appointmentAPI.createAppointment(appointmentData)
      appointments.push(result)
    }
    
    ElMessage.success('预约成功！')
    
    // 跳转到支付页面
    const appointmentId = appointments[0]?.id || 'demo'
    const paymentData = {
      appointmentId: appointmentId,
      appointments: appointments,
      totalPrice: totalPrice.value,
      selectedSlots: selectedSlots.value,
      bookingDate: selectedDate.value
    }
    
    // 将预约数据存储到 sessionStorage，在支付页面使用
    sessionStorage.setItem('paymentData', JSON.stringify(paymentData))
    
    router.push({
      path: '/payment',
      query: {
        appointmentId: appointmentId,
        totalPrice: totalPrice.value,
        date: selectedDate.value
      }
    })
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('预约失败:', error)
      ElMessage.error('预约失败: ' + (error.message || '未知错误'))
    }
  } finally {
    submitting.value = false
  }
}

// 生命周期
onMounted(() => {
  loadCourtData()
})
</script>

<style scoped>
.smart-booking {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.nav-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  color: #666;
  font-size: 16px;
}

.header h2 {
  color: #2c3e50;
  margin: 0;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 10px;
  border: none;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.stat-content {
  text-align: center;
  padding: 20px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 10px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.booking-table-card {
  border-radius: 10px;
  border: none;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 20px;
}

.table-header h3 {
  color: #2c3e50;
  margin: 0;
}

.legend {
  display: flex;
  gap: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #666;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-color.available {
  background: #67c23a;
}

.legend-color.occupied {
  background: #f56c6c;
}

.legend-color.closed {
  background: #909399;
}

.legend-color.selected {
  background: #409eff;
}

.time-table-container {
  overflow-x: auto;
  padding: 0 20px 20px;
}

.time-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}

.time-header,
.court-header {
  background: #f8f9fa;
  padding: 15px 10px;
  border: 1px solid #e9ecef;
  font-weight: bold;
  color: #2c3e50;
}

.time-header {
  width: 150px;
  text-align: center;
}

.court-header {
  min-width: 120px;
  text-align: center;
}

.court-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.court-info .name {
  font-size: 16px;
  font-weight: bold;
}

.court-info .price {
  font-size: 14px;
  color: #e6a23c;
}

.court-info .description {
  font-size: 12px;
  color: #666;
}

.time-name {
  background: #f8f9fa;
  padding: 15px 10px;
  border: 1px solid #e9ecef;
  font-weight: bold;
  text-align: center;
  color: #2c3e50;
}

.time-slot {
  padding: 10px;
  border: 1px solid #e9ecef;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 60px;
}

.slot-content {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.slot-status {
  font-size: 14px;
  font-weight: bold;
}

.slot-price {
  font-size: 12px;
  color: #e6a23c;
}

.slot-available {
  background: #f0f9ff;
  color: #67c23a;
}

.slot-available:hover {
  background: #e6f7ff;
  transform: scale(1.05);
}

.slot-occupied {
  background: #fef0f0;
  color: #f56c6c;
  cursor: not-allowed;
}

.slot-closed {
  background: #f5f5f5;
  color: #909399;
  cursor: not-allowed;
}

.slot-selected {
  background: #e6f7ff;
  color: #409eff;
  border: 2px solid #409eff;
}

.slot-empty {
  background: #f8f9fa;
  color: #ccc;
  cursor: not-allowed;
}

.booking-confirm-card {
  border-radius: 10px;
  border: none;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.confirm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 20px;
}

.confirm-header h3 {
  color: #2c3e50;
  margin: 0;
}

.clear-btn {
  color: #f56c6c;
}

.selected-slots {
  padding: 0 20px;
  margin-bottom: 20px;
}

.slot-item {
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

.court-name {
  font-weight: bold;
  color: #2c3e50;
}

.time-range {
  color: #666;
}

.price {
  color: #e6a23c;
  font-weight: bold;
}

.remove-btn {
  color: #f56c6c;
}

.booking-summary {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  margin: 0 20px 20px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #666;
}

.value {
  font-weight: bold;
  color: #2c3e50;
}

.total-price {
  color: #e6a23c;
  font-size: 18px;
}

.booking-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  padding: 20px;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 15px;
  }
  
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .legend {
    flex-direction: column;
    gap: 10px;
  }
  
  .slot-info {
    flex-direction: column;
    gap: 10px;
  }
  
  .booking-actions {
    flex-direction: column;
  }
}
</style> 