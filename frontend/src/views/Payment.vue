<template>
  <div class="payment-page">
    <div class="payment-header">
      <h2>确认支付</h2>
      <p class="payment-subtitle">请确认您的预约信息并选择支付方式</p>
    </div>

    <!-- 主要内容区域：左右分栏布局 -->
    <div class="payment-content">
      <!-- 左侧：预约信息确认 -->
      <div class="left-panel">
        <div class="booking-info-card">
          <div class="card-header">
            <h3>预约信息</h3>
          </div>
          <div class="booking-details">
            <div v-for="slot in bookingSlots" :key="slot.id" class="booking-item">
              <div class="item-info">
                <div class="court-name">{{ slot.courtName }}</div>
                <div class="time-info">
                  <span class="date">{{ formatDate(slot.date) }}</span>
                  <span class="time">{{ slot.startTime }} - {{ slot.endTime }}</span>
                </div>
                <div class="duration">{{ slot.duration }}小时</div>
              </div>
              <div class="item-price">¥{{ slot.price }}</div>
            </div>
          </div>
          <div class="booking-summary">
            <div class="summary-row">
              <span class="label">预约数量：</span>
              <span class="value">{{ bookingSlots.length }}个时间段</span>
            </div>
            <div class="summary-row">
              <span class="label">总时长：</span>
              <span class="value">{{ totalDuration }}小时</span>
            </div>
            <div class="summary-row">
              <span class="label">预约状态：</span>
              <span class="value">待支付</span>
            </div>
            <div class="summary-row">
              <span class="label">订单创建时间：</span>
              <span class="value">{{ formatDateTime(new Date()) }}</span>
            </div>
            <div class="summary-row total">
              <span class="label">总金额：</span>
              <span class="value">¥{{ totalAmount }}</span>
            </div>
          </div>
          
          <!-- 预约须知 -->
          <div class="booking-notice">
            <div class="notice-header">
              <h4>预约须知</h4>
            </div>
            <div class="notice-content">
              <ul>
                <li>请提前15分钟到达场馆</li>
                <li>预约成功后不支持退款</li>
                <li>如需取消预约，请提前24小时联系客服</li>
                <li>场馆内请穿着运动鞋，禁止穿高跟鞋</li>
                <li>请保持场馆清洁，爱护场馆设施</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：支付方式和支付信息 -->
      <div class="right-panel">
        <!-- 支付方式选择 -->
        <div class="payment-method-card">
          <div class="card-header">
            <h3>支付方式</h3>
          </div>
          <div class="payment-methods">
            <div 
              v-for="method in paymentMethods" 
              :key="method.id"
              :class="['payment-method', { 'selected': selectedMethod === method.id }]"
              @click="selectPaymentMethod(method.id)"
            >
              <div class="method-icon">
                <i :class="method.icon"></i>
              </div>
              <div class="method-info">
                <div class="method-name">{{ method.name }}</div>
                <div class="method-desc">{{ method.description }}</div>
              </div>
              <div class="method-radio">
                <input 
                  type="radio" 
                  :value="method.id" 
                  v-model="selectedMethod"
                  :id="method.id"
                >
                <label :for="method.id"></label>
              </div>
            </div>
          </div>
        </div>

        <!-- 支付信息 -->
        <div class="payment-info-card" v-if="selectedMethod">
          <div class="card-header">
            <h3>支付信息</h3>
          </div>
          <div class="payment-form">
            <!-- 支付宝支付 -->
            <div v-if="selectedMethod === 'alipay'" class="alipay-info">
              <div class="qr-code-container">
                <div class="qr-code">
                  <div class="qr-placeholder">
                    <i class="icon-qrcode"></i>
                    <p>支付宝二维码</p>
                  </div>
                </div>
                <p class="qr-instructions">请使用支付宝扫描二维码完成支付</p>
              </div>
            </div>

            <!-- 微信支付 -->
            <div v-if="selectedMethod === 'wechat'" class="wechat-info">
              <div class="qr-code-container">
                <div class="qr-code" v-if="!wechatPayInfo.qrCodeImage">
                  <div class="qr-placeholder">
                    <i class="icon-qrcode"></i>
                    <p>微信二维码</p>
                    <div class="loading-text">正在生成二维码...</div>
                  </div>
                </div>
                <div class="qr-code" v-else>
                  <img :src="wechatPayInfo.qrCodeImage" alt="微信支付二维码" class="qr-image">
                </div>
                <p class="qr-instructions">请使用微信扫描二维码完成支付</p>
                
                <!-- 微信支付信息 -->
                <div class="wechat-pay-details" v-if="wechatPayInfo.outTradeNo">
                  <div class="pay-info-row">
                    <span class="label">订单号:</span>
                    <span class="value">{{ wechatPayInfo.outTradeNo }}</span>
                  </div>
                  <div class="pay-info-row">
                    <span class="label">支付金额:</span>
                    <span class="value amount">¥{{ wechatPayInfo.amount }}</span>
                  </div>
                  <div class="pay-info-row" v-if="wechatPayInfo.expireTime">
                    <span class="label">过期时间:</span>
                    <span class="value">{{ formatDateTime(wechatPayInfo.expireTime) }}</span>
                  </div>
                  <div class="pay-info-row" v-if="wechatPayInfo.isMock">
                    <span class="label mode-label">演示模式:</span>
                    <span class="value mode-value">此为模拟支付，用于功能演示</span>
                  </div>
                </div>

                <!-- 支付状态检查 -->
                <div class="payment-status-check" v-if="wechatPayInfo.outTradeNo">
                  <button @click="checkWechatPaymentStatus" class="check-status-btn" :disabled="checkingStatus">
                    {{ checkingStatus ? '检查中...' : '检查支付状态' }}
                  </button>
                </div>
              </div>
            </div>

            <!-- 银行卡支付 -->
            <div v-if="selectedMethod === 'card'" class="card-info">
              <div class="form-group">
                <label>卡号</label>
                <input 
                  type="text" 
                  v-model="cardInfo.number" 
                  placeholder="请输入银行卡号"
                  @input="formatCardNumber"
                >
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>有效期</label>
                  <input 
                    type="text" 
                    v-model="cardInfo.expiry" 
                    placeholder="MM/YY"
                    @input="formatExpiry"
                  >
                </div>
                <div class="form-group">
                  <label>CVV</label>
                  <input 
                    type="text" 
                    v-model="cardInfo.cvv" 
                    placeholder="CVV"
                    maxlength="3"
                  >
                </div>
              </div>
              <div class="form-group">
                <label>持卡人姓名</label>
                <input 
                  type="text" 
                  v-model="cardInfo.name" 
                  placeholder="请输入持卡人姓名"
                >
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 支付按钮 -->
    <div class="payment-actions">
      <button @click="goBack" class="back-btn">返回</button>
      <button 
        @click="processPayment" 
        :disabled="!canPay || processing"
        class="pay-btn"
      >
        <span v-if="processing">处理中...</span>
        <span v-else>确认支付 ¥{{ totalAmount }}</span>
      </button>
    </div>

    <!-- 支付状态弹窗 -->
    <div v-if="showPaymentStatus" class="payment-status-overlay">
      <div class="payment-status-modal">
        <div class="status-icon">
          <i :class="paymentResult.success ? 'icon-success' : 'icon-error'"></i>
        </div>
        <div class="status-title">
          {{ paymentResult.success ? '支付成功' : '支付失败' }}
        </div>
        <div class="status-message">
          {{ paymentResult.message }}
        </div>
        <div class="status-actions">
          <button 
            @click="handlePaymentResult" 
            class="status-btn"
          >
            {{ paymentResult.success ? '查看预约' : '重新支付' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { paymentAPI } from '../services/api'

export default {
  name: 'Payment',
  data() {
    return {
      bookingSlots: [],
      selectedMethod: '',
      processing: false,
      showPaymentStatus: false,
      checkingStatus: false,
      paymentResult: {
        success: false,
        message: ''
      },
      cardInfo: {
        number: '',
        expiry: '',
        cvv: '',
        name: ''
      },
      wechatPayInfo: {
        outTradeNo: '',
        codeUrl: '',
        qrCodeImage: '',
        expireTime: null,
        amount: 0,
        isMock: true
      },
      paymentMethods: [
        {
          id: 'alipay',
          name: '支付宝',
          description: '使用支付宝扫码支付',
          icon: 'icon-alipay'
        },
        {
          id: 'wechat',
          name: '微信支付',
          description: '使用微信扫码支付',
          icon: 'icon-wechat'
        },
        {
          id: 'card',
          name: '银行卡',
          description: '使用银行卡在线支付',
          icon: 'icon-card'
        }
      ]
    }
  },
  computed: {
    totalAmount() {
      return this.bookingSlots.reduce((total, slot) => total + slot.price, 0)
    },
    totalDuration() {
      return this.bookingSlots.reduce((total, slot) => total + slot.duration, 0)
    },
    canPay() {
      if (!this.selectedMethod) return false
      
      if (this.selectedMethod === 'card') {
        return this.cardInfo.number && 
               this.cardInfo.expiry && 
               this.cardInfo.cvv && 
               this.cardInfo.name
      }
      
      return true
    }
  },
  methods: {
    // 选择支付方式
    selectPaymentMethod(methodId) {
      this.selectedMethod = methodId
    },
    
    // 格式化卡号
    formatCardNumber() {
      let value = this.cardInfo.number.replace(/\s+/g, '').replace(/[^0-9]/gi, '')
      let formattedValue = value.match(/.{1,4}/g)?.join(' ') || value
      this.cardInfo.number = formattedValue
    },
    
    // 格式化有效期
    formatExpiry() {
      let value = this.cardInfo.expiry.replace(/\D/g, '')
      if (value.length >= 2) {
        value = value.slice(0, 2) + '/' + value.slice(2, 4)
      }
      this.cardInfo.expiry = value
    },
    
    // 格式化日期
    formatDate(date) {
      return new Date(date).toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    },

    // 格式化日期时间
    formatDateTime(date) {
      return new Date(date).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    
    // 处理支付
    async processPayment() {
      if (!this.canPay) return
      
      this.processing = true
      
      try {
        // 创建支付订单
        const paymentData = {
          amount: this.totalAmount,
          paymentMethod: this.selectedMethod,
          bookingSlots: this.bookingSlots.map(slot => ({
            courtId: slot.courtId,
            startTime: `${slot.date}T${slot.startTime}:00`,
            endTime: `${slot.date}T${slot.endTime}:00`,
            amount: slot.price
          }))
        }
        
        if (this.selectedMethod === 'card') {
          paymentData.cardInfo = this.cardInfo
        }
        
        const response = await paymentAPI.createPayment(paymentData)
        
        // 处理微信支付
        if (this.selectedMethod === 'wechat') {
          await this.processWeChatPayment(response.paymentId)
          return
        }
        
        // 处理其他支付方式
        await this.simulatePaymentProcess(response.paymentId)
        
        // 处理支付结果
        const paymentStatus = await paymentAPI.getPaymentStatus(response.paymentId)
        
        this.paymentResult = {
          success: paymentStatus.status === 'completed',
          message: paymentStatus.status === 'completed' ? 
            '支付成功！您的预约已确认，请准时到场。' : 
            '支付失败，请重试或选择其他支付方式。'
        }
        
      } catch (error) {
        console.error('支付失败:', error)
        this.paymentResult = {
          success: false,
          message: '支付过程中发生错误，请重试。'
        }
      } finally {
        this.processing = false
        if (this.selectedMethod !== 'wechat') {
          this.showPaymentStatus = true
        }
      }
    },

    // 处理微信支付
    async processWeChatPayment(paymentId) {
      try {
        const response = await paymentAPI.processPayment(paymentId, {
          paymentMethod: 'WECHAT'
        })
        
        if (response.success && response.wechatPayInfo) {
          // 更新微信支付信息
          this.wechatPayInfo = {
            outTradeNo: response.wechatPayInfo.outTradeNo,
            codeUrl: response.wechatPayInfo.codeUrl,
            qrCodeImage: response.wechatPayInfo.qrCodeImage,
            expireTime: response.wechatPayInfo.expireTime,
            amount: response.wechatPayInfo.amount,
            isMock: response.wechatPayInfo.isMock
          }
          
          console.log('微信支付订单创建成功:', this.wechatPayInfo)
          
          // 开始轮询支付状态
          this.startPaymentStatusPolling()
        } else {
          throw new Error(response.message || '微信支付订单创建失败')
        }
        
      } catch (error) {
        console.error('微信支付处理失败:', error)
        this.paymentResult = {
          success: false,
          message: '微信支付处理失败: ' + error.message
        }
        this.showPaymentStatus = true
      }
    },

    // 检查微信支付状态
    async checkWechatPaymentStatus() {
      if (!this.wechatPayInfo.outTradeNo) return
      
      this.checkingStatus = true
      
      try {
        const response = await fetch(`/api/payment/wechat/order/${this.wechatPayInfo.outTradeNo}/status`)
        const result = await response.json()
        
        if (result.success && result.data) {
          const orderData = result.data
          console.log('微信支付状态:', orderData.status)
          
          if (orderData.isPaid) {
            this.paymentResult = {
              success: true,
              message: '微信支付成功！您的预约已确认，请准时到场。'
            }
            this.showPaymentStatus = true
          } else if (orderData.status === 'EXPIRED') {
            this.paymentResult = {
              success: false,
              message: '支付已过期，请重新创建订单。'
            }
            this.showPaymentStatus = true
          } else if (orderData.status === 'FAILED') {
            this.paymentResult = {
              success: false,
              message: '支付失败，请重试或选择其他支付方式。'
            }
            this.showPaymentStatus = true
          }
        }
        
      } catch (error) {
        console.error('检查微信支付状态失败:', error)
      } finally {
        this.checkingStatus = false
      }
    },

    // 开始轮询支付状态
    startPaymentStatusPolling() {
      const pollInterval = setInterval(async () => {
        if (!this.wechatPayInfo.outTradeNo) {
          clearInterval(pollInterval)
          return
        }
        
        try {
          const response = await fetch(`/api/payment/wechat/order/${this.wechatPayInfo.outTradeNo}/status`)
          const result = await response.json()
          
          if (result.success && result.data) {
            const orderData = result.data
            
            if (orderData.isPaid) {
              clearInterval(pollInterval)
              this.paymentResult = {
                success: true,
                message: '微信支付成功！您的预约已确认，请准时到场。'
              }
              this.showPaymentStatus = true
            } else if (orderData.isExpired || orderData.status === 'FAILED') {
              clearInterval(pollInterval)
              this.paymentResult = {
                success: false,
                message: orderData.isExpired ? '支付已过期' : '支付失败'
              }
              this.showPaymentStatus = true
            }
          }
        } catch (error) {
          console.error('轮询支付状态失败:', error)
        }
      }, 3000) // 每3秒检查一次
      
      // 5分钟后停止轮询
      setTimeout(() => {
        clearInterval(pollInterval)
      }, 300000)
    },
    
    // 模拟支付处理过程
    async simulatePaymentProcess(paymentId) {
      // 模拟支付处理延时
      await new Promise(resolve => setTimeout(resolve, 2000))
      
      // 模拟支付处理
      await paymentAPI.processPayment(paymentId, this.selectedMethod)
    },
    
    // 处理支付结果
    handlePaymentResult() {
      this.showPaymentStatus = false
      
      if (this.paymentResult.success) {
        // 支付成功，跳转到预约列表或首页
        this.$router.push('/')
      } else {
        // 支付失败，重置状态
        this.selectedMethod = ''
        this.cardInfo = {
          number: '',
          expiry: '',
          cvv: '',
          name: ''
        }
      }
    },
    
    // 返回上一页
    goBack() {
      this.$router.go(-1)
    }
  },
  
  // 组件创建时获取预约数据
  created() {
    // 从路由参数或store中获取预约数据
    const bookingData = this.$route.params.bookingData || 
                       JSON.parse(localStorage.getItem('pendingBooking') || '[]')
    
    this.bookingSlots = bookingData
    
    // 如果没有预约数据，返回预约页面
    if (this.bookingSlots.length === 0) {
      this.$router.push('/booking')
    }
  }
}
</script>

<style scoped>
.payment-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.payment-header {
  text-align: center;
  margin-bottom: 30px;
}

.payment-header h2 {
  color: #333;
  margin-bottom: 10px;
}

.payment-subtitle {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.payment-content {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
}

.left-panel {
  flex: 1;
  min-width: 0; /* 防止内容溢出 */
  min-height: 600px; /* 设置最小高度 */
}

.right-panel {
  flex: 1;
  min-width: 0; /* 防止内容溢出 */
  min-height: 600px; /* 设置最小高度 */
}

.booking-info-card {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 25px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  height: fit-content; /* 让卡片根据内容调整高度 */
}

.payment-method-card,
.payment-info-card {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  height: fit-content; /* 让卡片根据内容调整高度 */
}

.card-header {
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.booking-details {
  margin-bottom: 20px;
}

.booking-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 15px;
  background-color: #fafafa;
  transition: all 0.3s ease;
}

.booking-item:hover {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
}

.item-info {
  flex: 1;
}

.court-name {
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
  font-size: 16px;
}

.time-info {
  color: #666;
  font-size: 14px;
  margin-bottom: 6px;
}

.time-info .date {
  margin-right: 12px;
  font-weight: 500;
}

.duration {
  color: #999;
  font-size: 13px;
}

.item-price {
  font-size: 18px;
  font-weight: bold;
  color: #e6a23c;
}

.booking-summary {
  border-top: 1px solid #f0f0f0;
  padding-top: 20px;
  margin-top: 20px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}

.summary-row .label {
  color: #666;
  font-weight: 500;
}

.summary-row .value {
  color: #333;
  font-weight: 500;
}

.summary-row.total {
  font-size: 18px;
  font-weight: bold;
  color: #e6a23c;
  border-top: 1px solid #f0f0f0;
  padding-top: 10px;
}

.booking-notice {
  margin-top: 25px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #3498db;
}

.notice-header {
  margin-bottom: 15px;
}

.notice-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #2c3e50;
}

.notice-content ul {
  margin: 0;
  padding-left: 20px;
  list-style-type: disc;
}

.notice-content li {
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.notice-content li:last-child {
  margin-bottom: 0;
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.payment-method {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 2px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.payment-method:hover {
  border-color: #409eff;
}

.payment-method.selected {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.method-icon {
  font-size: 24px;
  margin-right: 15px;
  color: #409eff;
}

.method-info {
  flex: 1;
}

.method-name {
  font-weight: bold;
  color: #333;
  margin-bottom: 3px;
}

.method-desc {
  color: #666;
  font-size: 14px;
}

.method-radio {
  margin-left: 15px;
}

.method-radio input[type="radio"] {
  margin: 0;
}

.payment-form {
  margin-top: 20px;
}

.qr-code-container {
  text-align: center;
}

.qr-code {
  display: inline-block;
  width: 200px;
  height: 200px;
  border: 1px solid #ddd;
  border-radius: 8px;
  margin-bottom: 15px;
}

.qr-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
}

.qr-placeholder i {
  font-size: 48px;
  margin-bottom: 10px;
}

.qr-instructions {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.qr-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.loading-text {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.wechat-pay-details {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.pay-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
}

.pay-info-row:last-child {
  margin-bottom: 0;
}

.pay-info-row .label {
  color: #666;
  font-weight: 500;
}

.pay-info-row .value {
  color: #333;
  font-weight: 500;
  text-align: right;
}

.pay-info-row .value.amount {
  color: #e6a23c;
  font-weight: bold;
  font-size: 16px;
}

.pay-info-row .mode-label {
  color: #f56c6c;
}

.pay-info-row .mode-value {
  color: #f56c6c;
  font-size: 12px;
}

.payment-status-check {
  margin-top: 15px;
  text-align: center;
}

.check-status-btn {
  padding: 8px 16px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.check-status-btn:hover:not(:disabled) {
  background-color: #3a8ee6;
}

.check-status-btn:disabled {
  background-color: #c0c4cc;
  cursor: not-allowed;
}

.form-group {
  margin-bottom: 15px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #333;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-group input:focus {
  outline: none;
  border-color: #409eff;
}

.payment-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
}

.back-btn {
  padding: 12px 24px;
  background-color: #f0f0f0;
  color: #666;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.back-btn:hover {
  background-color: #e0e0e0;
}

.pay-btn {
  padding: 12px 24px;
  background-color: #67c23a;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
}

.pay-btn:hover:not(:disabled) {
  background-color: #85ce61;
}

.pay-btn:disabled {
  background-color: #c0c4cc;
  cursor: not-allowed;
}

.payment-status-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.payment-status-modal {
  background: white;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  max-width: 400px;
  width: 90%;
}

.status-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.status-icon .icon-success {
  color: #67c23a;
}

.status-icon .icon-error {
  color: #f56c6c;
}

.status-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
}

.status-message {
  color: #666;
  margin-bottom: 30px;
  line-height: 1.5;
}

.status-btn {
  padding: 12px 24px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.status-btn:hover {
  background-color: #3a8ee6;
}

/* 图标样式 */
.icon-alipay::before { content: "💰"; }
.icon-wechat::before { content: "💬"; }
.icon-card::before { content: "💳"; }
.icon-qrcode::before { content: "📱"; }
.icon-success::before { content: "✅"; }
.icon-error::before { content: "❌"; }

@media (max-width: 768px) {
  .payment-page {
    padding: 15px;
  }
  
  .payment-content {
    flex-direction: column;
    gap: 20px;
  }
  
  .left-panel,
  .right-panel {
    flex: none;
    min-height: auto; /* 在移动设备上取消最小高度限制 */
  }
  
  .booking-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .item-price {
    margin-top: 10px;
  }
  
  .payment-actions {
    flex-direction: column;
    gap: 10px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style> 