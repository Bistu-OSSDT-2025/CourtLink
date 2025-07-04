<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h2>仪表盘</h2>
      <p>欢迎回来，{{ adminStore.adminProfile?.fullName || adminStore.adminProfile?.username }}！</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="dashboard-stats">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon user-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ stats.totalUsers }}</h3>
              <p>总用户数</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon court-icon">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ stats.totalCourts }}</h3>
              <p>场地总数</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon booking-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ stats.todayBookings }}</h3>
              <p>今日预约</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon revenue-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <h3>¥{{ stats.todayRevenue }}</h3>
              <p>今日收入</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快速操作 -->
    <el-row :gutter="20" class="dashboard-actions">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快速操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/admin/courts')">
              <el-icon><OfficeBuilding /></el-icon>
              管理场地
            </el-button>
            <el-button type="success" @click="$router.push('/admin/bookings')">
              <el-icon><Calendar /></el-icon>
              查看预约
            </el-button>
            <el-button type="info" @click="$router.push('/admin/users')">
              <el-icon><User /></el-icon>
              用户管理
            </el-button>
            <el-button v-if="adminStore.isSuper" type="warning" @click="$router.push('/admin/admins')">
              <el-icon><UserFilled /></el-icon>
              管理员设置
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统状态</span>
            </div>
          </template>
          <div class="system-status">
            <div class="status-item">
              <span class="status-label">系统运行状态：</span>
              <el-tag type="success">正常</el-tag>
            </div>
            <div class="status-item">
              <span class="status-label">数据库连接：</span>
              <el-tag type="success">正常</el-tag>
            </div>
            <div class="status-item">
              <span class="status-label">在线用户：</span>
              <span>{{ stats.onlineUsers }}</span>
            </div>
            <div class="status-item">
              <span class="status-label">服务器负载：</span>
              <el-progress :percentage="35" :show-text="false" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近活动 -->
    <el-card class="recent-activities">
      <template #header>
        <div class="card-header">
          <span>最近活动</span>
          <el-button text @click="refreshActivities">刷新</el-button>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="activity in recentActivities"
          :key="activity.id"
          :timestamp="activity.time"
          :type="activity.type"
        >
          {{ activity.content }}
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useAdminStore } from "../../store/admin";
import { 
  User, 
  OfficeBuilding, 
  Calendar, 
  Money,
  UserFilled 
} from "@element-plus/icons-vue";

const adminStore = useAdminStore();

const stats = reactive({
  totalUsers: 156,
  totalCourts: 12,
  todayBookings: 23,
  todayRevenue: 2680,
  onlineUsers: 34
});

const recentActivities = ref([
  {
    id: 1,
    content: "用户 张三 预约了场地A，时间：14:00-16:00",
    time: "2025-07-03 10:30",
    type: "primary"
  },
  {
    id: 2,
    content: "管理员 李四 更新了场地B的价格",
    time: "2025-07-03 09:15",
    type: "success"
  },
  {
    id: 3,
    content: "新用户 王五 注册成功",
    time: "2025-07-03 08:45",
    type: "info"
  },
  {
    id: 4,
    content: "场地C的预约被取消",
    time: "2025-07-02 16:20",
    type: "warning"
  }
]);

const refreshActivities = () => {
  // 模拟刷新活动数据
  console.log("刷新活动数据");
};

onMounted(() => {
  // 这里可以调用API获取实际的统计数据
  console.log("Dashboard mounted");
});
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.dashboard-header {
  margin-bottom: 30px;
}

.dashboard-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}

.dashboard-header p {
  margin: 0;
  color: #606266;
  font-size: 16px;
}

.dashboard-stats {
  margin-bottom: 30px;
}

.stat-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 20px;
  color: white;
}

.user-icon {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.court-icon {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

.booking-icon {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}

.revenue-icon {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}

.stat-info h3 {
  margin: 0 0 5px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.dashboard-actions {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.quick-actions .el-button {
  flex: 1;
  min-width: 120px;
}

.system-status {
  space-y: 12px;
}

.status-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.status-label {
  margin-right: 12px;
  color: #606266;
  font-size: 14px;
  min-width: 100px;
}

.recent-activities {
  margin-top: 20px;
}

:deep(.el-timeline-item__timestamp) {
  color: #909399;
  font-size: 12px;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style> 