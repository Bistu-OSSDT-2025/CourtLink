<template>
  <div class="payment-container">
    <!-- 支付头部 -->
    <div class="payment-header">
      <div class="header-left">
        <h2>
          <i class="el-icon-wallet"></i>
          支付订单
        </h2>
        <p class="order-number">订单号: {{ payment.paymentId }}</p>
      </div>
      <div class="countdown-timer" :class="{ 'danger': timeLeft <= 120 }">
        <div class="timer-icon">
          <i class="el-icon-time"></i>
        </div>
        <div class="timer-text">
          <span class="timer-label">剩余时间</span>
          <span class="timer-value">{{ formatTime(timeLeft) }}</span>
        </div>
      </div>
    </div>

    <div class="payment-content">
      <!-- 左侧：预定信息 -->
      <div class="booking-section">
        <div class="section-card">
          <div class="card-header">
            <h3>
              <i class="el-icon-calendar"></i>
              预定信息
            </h3>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="label">预定日期</span>
              <span class="value">{{ appointment.appointmentDate }}</span>
            </div>
            <div class="info-row">
              <span class="label">预定状态</span>
              <span class="value status-confirmed">
                <i class="el-icon-check"></i>
                已确认
              </span>
            </div>
            
            <!-- 单个场地预约 -->
            <div v-if="!appointment.selectedSlots || appointment.selectedSlots.length <= 1" class="single-booking">
              <div class="info-row">
                <span class="label">场地名称</span>
                <span class="value">{{ appointment.courtName }}</span>
              </div>
              <div class="info-row">
                <span class="label">预定时间</span>
                <span class="value">{{ appointment.startTime }} - {{ appointment.endTime }}</span>
              </div>
            </div>
            
            <!-- 多个场地预约 -->
            <div v-else class="multiple-bookings">
              <div class="info-row">
                <span class="label">预约详情</span>
                <span class="value">{{ appointment.selectedSlots.length }} 个时段</span>
              </div>
              <div class="booking-details">
                <div v-for="slot in appointment.selectedSlots" :key="slot.key" class="slot-item">
                  <div class="slot-info">
                    <div class="court-name">{{ slot.courtName }}</div>
                    <div class="time-range">{{ slot.timeRange }}</div>
                  </div>
                  <div class="slot-price">¥{{ slot.price }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 羽毛球场使用须知 -->
        <div class="section-card notice-card">
          <div class="card-header">
            <h3>
              <i class="el-icon-warning-outline"></i>
              羽毛球场使用须知
            </h3>
          </div>
          <div class="card-body">
            <div class="notice-content">
              <div class="notice-section">
                <h4>
                  <i class="el-icon-time"></i>
                  入场时间
                </h4>
                <ul class="notice-list">
                  <li>请提前10分钟到达场地，预约时间开始后15分钟内未到视为自动取消</li>
                  <li>场地使用时间严格按照预约时段，不得超时使用</li>
                  <li>如需延长使用时间，请提前申请并支付相应费用</li>
                </ul>
              </div>

              <div class="notice-section">
                <h4>
                  <i class="el-icon-user"></i>
                  入场要求
                </h4>
                <ul class="notice-list">
                  <li>必须穿着运动鞋，禁止穿硬底鞋、高跟鞋入场</li>
                  <li>请携带个人毛巾和水杯，保持场地整洁</li>
                  <li>入场前请出示预约凭证和有效身份证件</li>
                </ul>
              </div>

              <div class="notice-section">
                <h4>
                  <i class="el-icon-circle-close"></i>
                  禁止事项
                </h4>
                <ul class="notice-list">
                  <li>禁止在场内吸烟、饮酒、携带宠物</li>
                  <li>禁止携带食物进入场地，仅允许饮用水</li>
                  <li>禁止在场地内大声喧哗，影响其他用户</li>
                </ul>
              </div>

              <div class="notice-section">
                <h4>
                  <i class="el-icon-money"></i>
                  费用说明
                </h4>
                <ul class="notice-list">
                  <li>预约成功后不可退款，但可在24小时前免费改期一次</li>
                  <li>如因个人原因迟到或未到，费用不予退还</li>
                  <li>设备损坏需按价赔偿，具体标准请咨询前台</li>
                </ul>
              </div>

              <div class="notice-agreement">
                <div class="agreement-checkbox">
                  <input 
                    type="checkbox" 
                    id="agreeTerms" 
                    v-model="agreedToTerms"
                    class="checkbox-input"
                  />
                  <label for="agreeTerms" class="checkbox-label">
                    <i class="el-icon-check" v-if="agreedToTerms"></i>
                  </label>
                  <span class="agreement-text">
                    我已阅读并同意遵守以上使用须知和相关规定
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：支付信息 -->
      <div class="payment-section">
        <!-- 支付金额 -->
        <div class="section-card amount-card">
          <div class="amount-display">
            <span class="amount-label">支付金额</span>
            <span class="amount-value">¥{{ payment.amount }}</span>
          </div>
        </div>

        <!-- 支付方式选择 -->
        <div class="section-card payment-method-card">
          <div class="card-header">
            <h3>
              <i class="el-icon-credit-card"></i>
              选择支付方式
            </h3>
          </div>
          <div class="card-body">
            <div class="payment-methods">
              <div 
                v-for="method in paymentMethods" 
                :key="method.value"
                class="payment-method-item"
                :class="{ active: selectedPaymentMethod === method.value }"
                @click="selectPaymentMethod(method.value)"
              >
                <div class="method-icon">
                  <i :class="method.icon"></i>
                </div>
                <div class="method-info">
                  <div class="method-name">{{ method.label }}</div>
                  <div class="method-desc">{{ method.description }}</div>
                </div>
                <div class="method-check">
                  <i class="el-icon-check" v-if="selectedPaymentMethod === method.value"></i>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 支付按钮 -->
        <div class="payment-actions">
          <el-button 
            type="primary" 
            size="large" 
            @click="handlePayment"
            :disabled="timeLeft <= 0 || isProcessing || !agreedToTerms"
            :loading="isProcessing"
            class="pay-button"
            :class="{ 'disabled-payment': !agreedToTerms }"
          >
            <i class="el-icon-wallet"></i>
            {{ timeLeft <= 0 ? '支付已超时' : !agreedToTerms ? '请先同意使用须知' : `立即支付 ¥${payment.amount}` }}
          </el-button>
          
          <el-button 
            size="large" 
            @click="cancelOrder"
            :disabled="isProcessing"
            class="cancel-button"
          >
            <i class="el-icon-close"></i>
            取消订单
          </el-button>
        </div>
      </div>
    </div>

    <!-- 支付状态对话框 -->
    <el-dialog
      v-model="paymentDialog"
      :title="paymentDialogTitle"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      class="payment-dialog"
    >
      <div class="dialog-content">
        <div class="dialog-icon">
          <el-icon class="loading-icon" v-if="isProcessing" size="60">
            <Loading />
          </el-icon>
          <el-icon class="success-icon" v-else-if="paymentSuccess" size="60">
            <CircleCheck />
          </el-icon>
          <el-icon class="error-icon" v-else size="60">
            <CircleClose />
          </el-icon>
        </div>
        <div class="dialog-text">
          <h3>{{ paymentMessage }}</h3>
          <p v-if="isProcessing" class="processing-tip">请在{{ getPaymentMethodName(selectedPaymentMethod) }}中完成支付</p>
          <p v-else-if="paymentSuccess" class="success-tip">支付成功，即将跳转到预约详情页</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button 
            @click="paymentDialog = false" 
            v-if="!isProcessing"
            type="primary"
            size="large"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import api from '../services/api'

export default {
  name: 'Payment',
  components: {
    Loading,
    CircleCheck,
    CircleClose
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    const appointment = ref({})
    const payment = ref({})
    const timeLeft = ref(600) // 10分钟 = 600秒
    const isProcessing = ref(false)
    const paymentDialog = ref(false)
    const paymentSuccess = ref(false)
    const paymentMessage = ref('')
    const paymentDialogTitle = ref('')
    const selectedPaymentMethod = ref('WECHAT')
    const agreedToTerms = ref(false)
    
    // 支付方式配置
    const paymentMethods = ref([
      {
        value: 'WECHAT',
        label: '微信支付',
        description: '使用微信扫码支付',
        icon: 'el-icon-chat-dot-square'
      },
      {
        value: 'ALIPAY',
        label: '支付宝',
        description: '使用支付宝扫码支付',
        icon: 'el-icon-wallet'
      },
      {
        value: 'CARD',
        label: '银行卡',
        description: '使用银行卡在线支付',
        icon: 'el-icon-credit-card'
      }
    ])
    
    let timer = null
    let statusCheckTimer = null
    
    // 选择支付方式
    const selectPaymentMethod = (method) => {
      selectedPaymentMethod.value = method
      payment.value.paymentMethod = method
    }
    
    // 获取支付方式名称
    const getPaymentMethodName = (method) => {
      const methodInfo = paymentMethods.value.find(m => m.value === method)
      return methodInfo ? methodInfo.label : '支付平台'
    }
    
    // 格式化时间显示
    const formatTime = (seconds) => {
      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = seconds % 60
      return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
    }
    
    // 倒计时
    const startCountdown = () => {
      timer = setInterval(() => {
        timeLeft.value--
        if (timeLeft.value <= 0) {
          clearInterval(timer)
          handleTimeout()
        }
      }, 1000)
    }
    
    // 支付超时处理
    const handleTimeout = () => {
      ElMessage.error('支付超时，订单已自动取消')
      router.push('/booking')
    }
    
    // 处理支付
    const handlePayment = async () => {
      // 检查是否同意条款
      if (!agreedToTerms.value) {
        ElMessage.warning('请先阅读并同意羽毛球场使用须知')
        return
      }
      
      try {
        isProcessing.value = true
        paymentDialog.value = true
        paymentDialogTitle.value = '正在处理支付'
        paymentMessage.value = '正在启动支付...'
        
        // 更新支付方式
        const paymentData = {
          paymentId: payment.value.paymentId,
          paymentMethod: selectedPaymentMethod.value
        }
        
        const response = await api.processPayment(paymentData)
        
        if (response.success) {
          paymentSuccess.value = true
          paymentDialogTitle.value = '支付成功'
          paymentMessage.value = '支付成功！'
          
          // 开始检查支付状态
          startPaymentStatusCheck()
        } else {
          throw new Error(response.message || '支付处理失败')
        }
      } catch (error) {
        console.error('支付处理失败:', error)
        paymentSuccess.value = false
        paymentDialogTitle.value = '支付失败'
        paymentMessage.value = error.message || '支付处理失败，请重试'
        isProcessing.value = false
      }
    }
    
    // 检查支付状态
    const startPaymentStatusCheck = () => {
      statusCheckTimer = setInterval(async () => {
        try {
          const response = await api.getPaymentStatus(payment.value.paymentId)
          
          if (response.status === 'COMPLETED') {
            clearInterval(statusCheckTimer)
            isProcessing.value = false
            paymentSuccess.value = true
            paymentMessage.value = '支付成功！正在跳转...'
            
            setTimeout(() => {
              router.push('/booking')
            }, 2000)
          } else if (response.status === 'FAILED') {
            clearInterval(statusCheckTimer)
            isProcessing.value = false
            paymentSuccess.value = false
            paymentMessage.value = '支付失败，请重试'
          }
        } catch (error) {
          console.error('检查支付状态失败:', error)
        }
      }, 2000) // 每2秒检查一次
    }
    
    // 取消订单
    const cancelOrder = async () => {
      try {
        await ElMessageBox.confirm('确定要取消此订单吗？', '确认取消', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await api.cancelPayment(payment.value.paymentId)
        ElMessage.success('订单已取消')
        router.push('/booking')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消订单失败:', error)
          ElMessage.error('取消订单失败，请重试')
        }
      }
    }
    
    // 从路由参数和sessionStorage中获取预定和支付信息
    const loadPaymentData = () => {
      // 优先从sessionStorage获取智能预约的数据
      const sessionPaymentData = sessionStorage.getItem('paymentData')
      if (sessionPaymentData) {
        try {
          const data = JSON.parse(sessionPaymentData)
          
          // 设置预定信息
          const firstSlot = data.selectedSlots[0]
          appointment.value = {
            appointmentId: data.appointmentId,
            courtName: firstSlot ? firstSlot.courtName : '多个场地',
            appointmentDate: data.bookingDate,
            startTime: data.selectedSlots.length > 0 ? data.selectedSlots[0].timeRange.split('-')[0] : '',
            endTime: data.selectedSlots.length > 0 ? data.selectedSlots[data.selectedSlots.length - 1].timeRange.split('-')[1] : '',
            selectedSlots: data.selectedSlots
          }
          
          // 设置支付信息
          payment.value = {
            paymentId: `PAY_${Date.now()}_${data.appointmentId}`,
            amount: data.totalPrice,
            paymentMethod: 'WECHAT',
            expireTime: new Date(Date.now() + 10 * 60 * 1000) // 10分钟后过期
          }
          
          // 清除sessionStorage中的数据
          sessionStorage.removeItem('paymentData')
          
          // 开始倒计时
          timeLeft.value = 600 // 10分钟
          startCountdown()
          return
        } catch (error) {
          console.error('解析智能预约数据失败:', error)
        }
      }
      
      // 首先检查sessionStorage中是否有普通预约的数据
      const sessionAppointmentData = sessionStorage.getItem('appointmentData')
      const sessionPaymentData2 = sessionStorage.getItem('paymentData')
      
      if (sessionAppointmentData && sessionPaymentData2) {
        try {
          const appointmentData = JSON.parse(sessionAppointmentData)
          const paymentData = JSON.parse(sessionPaymentData2)
          
          appointment.value = appointmentData
          payment.value = paymentData
          
          // 清除sessionStorage中的数据
          sessionStorage.removeItem('appointmentData')
          sessionStorage.removeItem('paymentData')
          
          // 计算剩余时间
          const expireTime = new Date(payment.value.expireTime)
          const now = new Date()
          const remainingTime = Math.max(0, Math.floor((expireTime - now) / 1000))
          timeLeft.value = remainingTime
          
          if (remainingTime > 0) {
            startCountdown()
          } else {
            handleTimeout()
          }
          return
        } catch (error) {
          console.error('解析预约数据失败:', error)
        }
      }
      
      // 从路由参数中获取传统预约的数据
      const { appointmentData, paymentData } = route.params
      
      if (appointmentData && paymentData) {
        appointment.value = typeof appointmentData === 'string' ? 
          JSON.parse(appointmentData) : appointmentData
        payment.value = typeof paymentData === 'string' ? 
          JSON.parse(paymentData) : paymentData
        
        // 计算剩余时间
        const expireTime = new Date(payment.value.expireTime)
        const now = new Date()
        const remainingTime = Math.max(0, Math.floor((expireTime - now) / 1000))
        timeLeft.value = remainingTime
        
        if (remainingTime > 0) {
          startCountdown()
        } else {
          handleTimeout()
        }
      } else {
        // 尝试从query参数获取基本信息
        const { appointmentId, totalPrice, date } = route.query
        if (appointmentId && totalPrice) {
          appointment.value = {
            appointmentId: appointmentId,
            courtName: '预约场地',
            appointmentDate: date || new Date().toISOString().split('T')[0],
            startTime: '待确认',
            endTime: '待确认'
          }
          
          payment.value = {
            paymentId: `PAY_${Date.now()}_${appointmentId}`,
            amount: parseFloat(totalPrice),
            paymentMethod: 'WECHAT',
            expireTime: new Date(Date.now() + 10 * 60 * 1000)
          }
          
          timeLeft.value = 600
          startCountdown()
        } else {
          console.error('支付信息不完整，无法跳转支付界面')
          ElMessage.error('支付信息不完整，请重新预定')
          router.push('/booking')
        }
      }
    }
    
    onMounted(() => {
      loadPaymentData()
    })
    
    onUnmounted(() => {
      if (timer) {
        clearInterval(timer)
      }
      if (statusCheckTimer) {
        clearInterval(statusCheckTimer)
      }
    })
    
    return {
      appointment,
      payment,
      timeLeft,
      isProcessing,
      paymentDialog,
      paymentSuccess,
      paymentMessage,
      paymentDialogTitle,
      selectedPaymentMethod,
      paymentMethods,
      agreedToTerms,
      formatTime,
      selectPaymentMethod,
      getPaymentMethodName,
      handlePayment,
      cancelOrder
    }
  }
}
</script>

<style scoped>
.payment-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

.payment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 25px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.header-left h2 {
  color: #2c3e50;
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h2 i {
  color: #667eea;
  font-size: 32px;
}

.order-number {
  color: #7f8c8d;
  margin: 0;
  font-size: 14px;
}

.countdown-timer {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 25px;
  background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
  border-radius: 12px;
  color: white;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(46, 204, 113, 0.3);
}

.countdown-timer.danger {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  box-shadow: 0 4px 15px rgba(231, 76, 60, 0.3);
}

.timer-icon {
  font-size: 24px;
}

.timer-text {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.timer-label {
  font-size: 12px;
  opacity: 0.9;
}

.timer-value {
  font-size: 18px;
  font-weight: 700;
  margin-top: 2px;
}

.payment-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.section-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  overflow: hidden;
  margin-bottom: 20px;
}

.card-header {
  padding: 20px 25px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.card-header h3 {
  color: #2c3e50;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header h3 i {
  color: #667eea;
  font-size: 20px;
}

.card-body {
  padding: 25px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  color: #7f8c8d;
  font-weight: 500;
  font-size: 14px;
}

.info-row .value {
  color: #2c3e50;
  font-weight: 600;
  font-size: 16px;
}

.status-confirmed {
  color: #27ae60;
  display: flex;
  align-items: center;
  gap: 5px;
}

.booking-details {
  margin-top: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.slot-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.slot-item:last-child {
  border-bottom: none;
}

.slot-info {
  flex: 1;
}

.court-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 5px;
}

.time-range {
  color: #7f8c8d;
  font-size: 14px;
}

.slot-price {
  color: #e67e22;
  font-weight: 700;
  font-size: 16px;
}

.amount-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.amount-display {
  padding: 30px;
  text-align: center;
}

.amount-label {
  display: block;
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 10px;
}

.amount-value {
  font-size: 36px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.payment-method-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.payment-method-item:hover {
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.15);
}

.payment-method-item.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.method-icon {
  font-size: 32px;
  margin-right: 15px;
  color: #667eea;
}

.payment-method-item.active .method-icon {
  color: white;
}

.method-info {
  flex: 1;
}

.method-name {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 5px;
}

.method-desc {
  font-size: 14px;
  opacity: 0.7;
}

.method-check {
  font-size: 20px;
  color: #27ae60;
}

.payment-method-item.active .method-check {
  color: white;
}

.notice-card {
  margin-top: 0;
}

.notice-content {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 5px;
}

.notice-content::-webkit-scrollbar {
  width: 6px;
}

.notice-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.notice-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.notice-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.notice-section {
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.notice-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.notice-section h4 {
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.notice-section h4 i {
  color: #667eea;
  font-size: 18px;
}

.notice-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.notice-list li {
  position: relative;
  padding: 8px 0 8px 20px;
  color: #5a6c7d;
  font-size: 14px;
  line-height: 1.5;
}

.notice-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  top: 8px;
  color: #667eea;
  font-weight: bold;
  font-size: 16px;
}

.notice-agreement {
  margin-top: 25px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.agreement-checkbox {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  cursor: pointer;
}

.checkbox-input {
  display: none;
}

.checkbox-label {
  width: 20px;
  height: 20px;
  border: 2px solid #ddd;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
  margin-top: 2px;
}

.checkbox-input:checked + .checkbox-label {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: white;
}

.checkbox-label:hover {
  border-color: #667eea;
}

.checkbox-label i {
  font-size: 12px;
  font-weight: bold;
}

.agreement-text {
  color: #2c3e50;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
}

.payment-actions {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 20px;
}

.pay-button, .cancel-button {
  height: 60px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.pay-button {
  background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(46, 204, 113, 0.3);
}

.pay-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(46, 204, 113, 0.4);
}

.pay-button.disabled-payment {
  background: linear-gradient(135deg, #bdc3c7 0%, #95a5a6 100%);
  color: #7f8c8d;
  cursor: not-allowed;
}

.pay-button.disabled-payment:hover {
  transform: none;
  box-shadow: none;
}

.cancel-button {
  background: white;
  border: 2px solid #e9ecef;
  color: #7f8c8d;
}

.cancel-button:hover {
  border-color: #e74c3c;
  color: #e74c3c;
  transform: translateY(-2px);
}

.payment-dialog .dialog-content {
  text-align: center;
  padding: 30px;
}

.dialog-icon {
  margin-bottom: 20px;
}

.loading-icon {
  color: #667eea;
  animation: spin 2s linear infinite;
}

.success-icon {
  color: #27ae60;
}

.error-icon {
  color: #e74c3c;
}

.dialog-text h3 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 20px;
}

.processing-tip, .success-tip {
  color: #7f8c8d;
  font-size: 14px;
  margin: 0;
}

.dialog-footer {
  text-align: center;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .payment-content {
    grid-template-columns: 1fr;
  }
  
  .payment-header {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .header-left h2 {
    justify-content: center;
  }
  
  .timer-text {
    align-items: center;
  }
  
  .countdown-timer {
    justify-content: center;
  }
  
  .amount-display {
    padding: 20px;
  }
  
  .amount-value {
    font-size: 28px;
  }
  
  .notice-content {
    max-height: 300px;
  }
  
  .notice-section h4 {
    font-size: 14px;
  }
  
  .notice-list li {
    font-size: 13px;
    padding: 6px 0 6px 18px;
  }
  
  .agreement-text {
    font-size: 13px;
  }
}
</style> 