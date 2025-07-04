<template>
  <div class="home">
    <!-- 导航栏 -->
    <div class="navbar">
      <div class="nav-content">
        <h1 class="logo">🏓 CourtLink 场地预约系统</h1>
        <div class="nav-info">
          <span class="user-welcome">欢迎，{{ userInfo.username }}</span>
          <el-button @click="logout" type="danger" plain>退出登录</el-button>
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <div class="welcome-section">
        <h2>🏆 欢迎使用专业场地预约系统</h2>
        <p>选择以下功能开始您的预约之旅</p>
      </div>

      <!-- 功能模块 -->
      <div class="function-cards">
        <el-card class="function-card badminton-card" shadow="hover" @click="goToBooking">
          <div class="card-content">
            <div class="card-icon">🏸</div>
            <h3>羽毛球场预约</h3>
            <p>预约您心仪的羽毛球场地</p>
            <el-button type="primary" class="card-button">立即预约</el-button>
          </div>
        </el-card>

        <el-card class="function-card pingpong-card" shadow="hover" @click="goToSmartBooking">
          <div class="card-content">
            <div class="card-icon">🏓</div>
            <h3>乒乓球场预约</h3>
            <p>预约专业乒乓球台，享受精彩对战</p>
            <el-button type="success" class="card-button">立即预约</el-button>
          </div>
        </el-card>

        <el-card class="function-card logout-card" shadow="hover" @click="logout">
          <div class="card-content">
            <div class="card-icon">🚪</div>
            <h3>退出登录</h3>
            <p>安全退出系统</p>
            <el-button type="danger" class="card-button">退出登录</el-button>
          </div>
        </el-card>
      </div>

      <!-- 快速统计信息 -->
      <div class="stats-section">
        <el-card class="stats-card">
          <div class="stats-header">
            <h3>📊 今日场地统计</h3>
          </div>
          <div class="stats-content">
            <div class="stat-item">
              <div class="stat-icon">🏟️</div>
              <div class="stat-value">{{ stats.totalCourts }}</div>
              <div class="stat-label">可用场地</div>
            </div>
            <div class="stat-item">
              <div class="stat-icon">⏰</div>
              <div class="stat-value">{{ stats.todaySlots }}</div>
              <div class="stat-label">今日时段</div>
            </div>
            <div class="stat-item">
              <div class="stat-icon">✅</div>
              <div class="stat-value">{{ stats.availableSlots }}</div>
              <div class="stat-label">空闲时段</div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'
import { appointmentAPI } from '../services/api'

const router = useRouter()
const userStore = useUserStore()

const userInfo = ref({
  username: userStore.username || 'Guest'
})

const stats = ref({
  totalCourts: 0,
  todaySlots: 0,
  availableSlots: 0
})

// 方法
const goToBooking = () => {
  router.push('/booking')
}

const goToSmartBooking = () => {
  // 暂时跳转到同样的预约页面，后续可以为乒乓球场创建专门的预约页面
  router.push('/booking')
}

const logout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

const loadStats = async () => {
  try {
    const today = new Date().toISOString().split('T')[0]
    const response = await appointmentAPI.getCourtsForBooking(today)
    
    if (Array.isArray(response)) {
      stats.value.totalCourts = response.length
      let totalSlots = 0
      let availableSlots = 0
      
      response.forEach(court => {
        if (court.timeSlots) {
          totalSlots += court.timeSlots.length
          availableSlots += court.timeSlots.filter(slot => slot.available && slot.isOpen).length
        }
      })
      
      stats.value.todaySlots = totalSlots
      stats.value.availableSlots = availableSlots
    }
  } catch (error) {
    console.log('加载统计信息失败:', error)
    // 如果API调用失败，设置默认值
    stats.value = {
      totalCourts: 0,
      todaySlots: 0,
      availableSlots: 0
    }
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
  animation: backgroundShift 20s ease-in-out infinite;
}

@keyframes backgroundShift {
  0%, 100% { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
  50% { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
}

.navbar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(15px);
  padding: 0 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
}

.logo {
  color: #2c3e50;
  margin: 0;
  font-size: 26px;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.nav-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-welcome {
  color: #2c3e50;
  font-weight: 500;
  font-size: 16px;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.welcome-section {
  text-align: center;
  margin-bottom: 50px;
  color: white;
  animation: fadeInUp 1s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.welcome-section h2 {
  font-size: 36px;
  margin-bottom: 15px;
  font-weight: 300;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.welcome-section p {
  font-size: 18px;
  opacity: 0.9;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
}

.function-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  margin-bottom: 50px;
}

.function-card {
  border-radius: 20px;
  border: none;
  cursor: pointer;
  transition: all 0.4s ease;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(15px);
  overflow: hidden;
  position: relative;
  animation: slideInUp 0.8s ease-out;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.function-card:hover {
  transform: translateY(-15px) scale(1.02);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
}

.function-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  transition: height 0.3s ease;
}

.function-card:hover::before {
  height: 8px;
}

.badminton-card::before {
  background: linear-gradient(90deg, #11998e, #38ef7d);
}

.pingpong-card::before {
  background: linear-gradient(90deg, #ff9a9e, #fecfef);
}

.logout-card::before {
  background: linear-gradient(90deg, #fc466b, #3f5efb);
}

.card-content {
  padding: 40px 20px;
  text-align: center;
}

.card-icon {
  font-size: 80px;
  margin-bottom: 20px;
  display: block;
  line-height: 1;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.card-content h3 {
  font-size: 24px;
  margin-bottom: 15px;
  color: #2c3e50;
  font-weight: 600;
}

.card-content p {
  color: #666;
  margin-bottom: 25px;
  font-size: 16px;
  line-height: 1.5;
}

.card-button {
  width: 80%;
  padding: 12px 0;
  font-size: 16px;
  border-radius: 25px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.card-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.stats-section {
  margin-top: 50px;
  animation: fadeIn 1s ease-out 0.5s both;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.stats-card {
  border-radius: 20px;
  border: none;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(15px);
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.stats-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  text-align: center;
  margin: 0;
}

.stats-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.stats-content {
  display: flex;
  justify-content: space-around;
  padding: 30px 20px;
}

.stat-item {
  text-align: center;
  transition: transform 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-5px);
}

.stat-icon {
  font-size: 32px;
  margin-bottom: 10px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 10px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
}

.stat-label {
  color: #666;
  font-size: 16px;
  font-weight: 500;
}

@media (max-width: 768px) {
  .function-cards {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .stats-content {
    flex-direction: column;
    gap: 20px;
  }
  
  .nav-content {
    flex-direction: column;
    height: auto;
    padding: 15px 0;
    gap: 15px;
  }
  
  .logo {
    font-size: 22px;
  }
  
  .welcome-section h2 {
    font-size: 28px;
  }
  
  .card-icon {
    font-size: 60px;
  }
}
</style>
