<template>
  <div class="home">
    <div class="header">
      <h1>场地预订</h1>
      <div class="navigation">
        <router-link to="/booking" class="nav-link booking-link">
          🏸 智能预约系统
        </router-link>
        <button @click="logout" class="logout-btn">退出登录</button>
      </div>
    </div>
    
    <!-- 场地列表 -->
    <div class="courts-container" v-if="courts.length">
      <div v-for="court in courts" :key="court.id" class="court-card">
        <h3>{{ court.name }}</h3>
        <p>{{ court.description }}</p>
        <p>价格: ¥{{ court.pricePerHour }}/小时</p>
        <button @click="selectCourt(court)" :disabled="!court.available">
          {{ court.available ? '预订' : '已预订' }}
        </button>
      </div>
    </div>
    <p v-else>加载场地信息中...</p>

    <!-- 预订表单 -->
    <div v-if="selectedCourt" class="booking-form">
      <h2>预订 {{ selectedCourt.name }}</h2>
      <form @submit.prevent="submitBooking">
        <div class="form-group">
          <label>预订日期：</label>
          <input type="date" v-model="bookingDate" :min="today" required>
        </div>
        
        <div class="form-group">
          <label>开始时间：</label>
          <select v-model="startTime" required>
            <option v-for="time in availableStartTimes" :key="time" :value="time">
              {{ time }}:00
            </option>
          </select>
        </div>
        
        <div class="form-group">
          <label>时长：</label>
          <select v-model="duration" required>
            <option value="1">1小时</option>
            <option value="2">2小时</option>
          </select>
        </div>

        <div class="form-group">
          <label>总价：</label>
          <span>¥{{ totalPrice }}</span>
        </div>

        <button type="submit" :disabled="isSubmitting">确认预订</button>
      </form>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { courtAPI, appointmentAPI } from '../services/api';

export default {
  name: 'Home',
  setup() {
    const router = useRouter();
    const courts = ref([]);
    const selectedCourt = ref(null);
    const bookingDate = ref('');
    const startTime = ref('');
    const duration = ref('1');
    const isSubmitting = ref(false);
    const today = new Date().toISOString().split('T')[0];

    // 可选的开始时间（整点）
    const availableStartTimes = computed(() => {
      const times = [];
      // 上午时段：9:00-11:00
      for (let i = 9; i <= 10; i++) {
        times.push(i.toString().padStart(2, '0'));
      }
      // 下午时段：14:00-20:00
      for (let i = 14; i <= 19; i++) {
        times.push(i.toString().padStart(2, '0'));
      }
      return times;
    });

    // 计算总价
    const totalPrice = computed(() => {
      if (!selectedCourt.value) return 0;
      return selectedCourt.value.pricePerHour * parseInt(duration.value);
    });

    // 验证结束时间是否在允许的时间段内
    const validateEndTime = (startHour, duration) => {
      const endHour = parseInt(startHour) + parseInt(duration);
      
      // 检查上午时段
      if (startHour >= 9 && startHour <= 10) {
        return endHour <= 11;
      }
      
      // 检查下午时段
      if (startHour >= 14 && startHour <= 19) {
        return endHour <= 20;
      }
      
      return false;
    };

    // 获取场地列表
    const fetchCourts = async () => {
      try {
        const response = await courtAPI.getAllCourts();
        courts.value = response;
      } catch (error) {
        console.error('获取场地列表失败:', error);
      }
    };

    // 选择场地
    const selectCourt = (court) => {
      selectedCourt.value = court;
      bookingDate.value = today;
      startTime.value = availableStartTimes.value[0];
    };

    // 提交预订
    const submitBooking = async () => {
      if (isSubmitting.value) return;
      
      // 验证结束时间
      if (!validateEndTime(startTime.value, duration.value)) {
        alert('选择的时间段无效，请确保预订时间在允许的范围内（9:00-11:00或14:00-20:00）');
        return;
      }
      
      isSubmitting.value = true;
      try {
        const startDateTime = `${bookingDate.value}T${startTime.value}:00:00`;
        const endDateTime = new Date(startDateTime);
        endDateTime.setHours(endDateTime.getHours() + parseInt(duration.value));

        const appointmentData = {
          courtId: selectedCourt.value.id,
          startTime: startDateTime,
          endTime: endDateTime.toISOString(),
          amount: totalPrice.value
        };

        await appointmentAPI.createAppointment(appointmentData);
        alert('预订成功！');
        
        // 重置表单
        selectedCourt.value = null;
        bookingDate.value = '';
        startTime.value = '';
        duration.value = '1';
        
        // 刷新场地列表
        await fetchCourts();
      } catch (error) {
        alert(error.response?.data?.message || '预订失败，请重试');
      } finally {
        isSubmitting.value = false;
      }
    };

    const logout = () => {
      localStorage.removeItem('token')
      router.push('/login')
    }

    onMounted(fetchCourts);

    return {
      courts,
      selectedCourt,
      bookingDate,
      startTime,
      duration,
      isSubmitting,
      today,
      availableStartTimes,
      totalPrice,
      selectCourt,
      submitBooking,
      logout
    };
  }
};
</script>

<style scoped>
.home {
  padding: 20px;
  max-width: 1200px;
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

.header h1 {
  margin: 0;
  color: #333;
  font-size: 2.5rem;
}

.navigation {
  display: flex;
  gap: 15px;
  align-items: center;
}

.nav-link {
  text-decoration: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.booking-link {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.booking-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.logout-btn {
  padding: 10px 20px;
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background-color: #f78989;
  transform: translateY(-1px);
}

.courts-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.court-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.booking-form {
  margin-top: 30px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

input, select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

button {
  background-color: #4CAF50;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

button:hover:not(:disabled) {
  background-color: #45a049;
}
</style>
