<<<<<<< HEAD
<template>
  <div class="admin-dashboard">
    <!-- 顶部导航栏 -->
    <el-header class="admin-header">
      <div class="header-left">
        <h1>CourtLink 管理后台</h1>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="admin-dropdown">
            <el-avatar :size="32" :src="adminStore.adminProfile?.avatar">
              {{ adminStore.adminProfile?.fullName?.charAt(0) || 'A' }}
            </el-avatar>
            <span class="admin-name">{{ adminStore.adminProfile?.fullName || adminStore.adminProfile?.username }}</span>
            <el-icon><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人资料</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 主要内容区域 -->
    <el-container class="admin-main">
      <!-- 侧边栏 -->
      <el-aside width="250px" class="admin-sidebar">
        <el-menu
          :default-active="activeMenuItem"
          class="admin-menu"
          router
          @select="handleMenuSelect"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          
          <el-sub-menu index="courts">
            <template #title>
              <el-icon><OfficeBuilding /></el-icon>
              <span>场地管理</span>
            </template>
            <el-menu-item index="/admin/courts">场地列表</el-menu-item>
            <el-menu-item index="/admin/courts/schedule">场地时间</el-menu-item>
            <el-menu-item index="/admin/courts/statistics">场地统计</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="bookings">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span>预约管理</span>
            </template>
            <el-menu-item index="/admin/bookings">预约列表</el-menu-item>
            <el-menu-item index="/admin/bookings/calendar">预约日历</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="users">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/admin/users">用户列表</el-menu-item>
            <el-menu-item index="/admin/users/statistics">用户统计</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="admins" v-if="adminStore.isSuper">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>管理员</span>
            </template>
            <el-menu-item index="/admin/admins">管理员列表</el-menu-item>
            <el-menu-item index="/admin/admins/create">添加管理员</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/admin/settings">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 内容区域 -->
      <el-main class="admin-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAdminStore } from "../store/admin";
import { ElMessage, ElMessageBox } from "element-plus";
import { 
  ArrowDown, 
  DataBoard, 
  OfficeBuilding, 
  Calendar, 
  User, 
  UserFilled, 
  Setting 
} from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();
const adminStore = useAdminStore();

const activeMenuItem = computed(() => route.path);

const handleMenuSelect = (index) => {
  console.log("Menu selected:", index);
};

const handleCommand = async (command) => {
  switch (command) {
    case "profile":
      router.push("/admin/profile");
      break;
    case "logout":
      try {
        await ElMessageBox.confirm("确定要退出登录吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
        await adminStore.logout();
        ElMessage.success("退出登录成功");
        router.push("/admin/login");
      } catch (error) {
        // 用户取消了操作
      }
      break;
  }
};

onMounted(async () => {
  // 获取管理员信息
  try {
    await adminStore.fetchProfile();
  } catch (error) {
    ElMessage.error("获取管理员信息失败");
    router.push("/admin/login");
  }
});
</script>

<style scoped>
.admin-dashboard {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.admin-header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.header-left h1 {
  margin: 0;
  font-size: 20px;
  color: #303133;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
}

.admin-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s ease;
}

.admin-dropdown:hover {
  background-color: #f5f7fa;
}

.admin-name {
  margin: 0 8px;
  font-size: 14px;
  color: #303133;
}

.admin-main {
  flex: 1;
  overflow: hidden;
}

.admin-sidebar {
  background: #304156;
  overflow-y: auto;
}

.admin-menu {
  border: none;
  background: #304156;
}

:deep(.el-menu-item) {
  color: #bfcbd9;
  border-bottom: 1px solid #263445;
}

:deep(.el-menu-item:hover) {
  background-color: #263445 !important;
  color: #fff;
}

:deep(.el-menu-item.is-active) {
  background-color: #409eff !important;
  color: #fff;
}

:deep(.el-sub-menu__title) {
  color: #bfcbd9;
  border-bottom: 1px solid #263445;
}

:deep(.el-sub-menu__title:hover) {
  background-color: #263445 !important;
  color: #fff;
}

:deep(.el-sub-menu .el-menu-item) {
  background-color: #1f2d3d;
  min-height: 44px;
}

:deep(.el-sub-menu .el-menu-item:hover) {
  background-color: #001528 !important;
}

.admin-content {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

/* 滚动条样式 */
.admin-sidebar::-webkit-scrollbar {
  width: 6px;
}

.admin-sidebar::-webkit-scrollbar-track {
  background: #263445;
}

.admin-sidebar::-webkit-scrollbar-thumb {
  background: #409eff;
  border-radius: 3px;
}

.admin-content::-webkit-scrollbar {
  width: 6px;
}

.admin-content::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.admin-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}
=======
<template>
  <div class="admin-dashboard">
    <!-- 顶部导航栏 -->
    <el-header class="admin-header">
      <div class="header-left">
        <h1>CourtLink 管理后台</h1>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="admin-dropdown">
            <el-avatar :size="32" :src="adminStore.adminProfile?.avatar">
              {{ adminStore.adminProfile?.fullName?.charAt(0) || 'A' }}
            </el-avatar>
            <span class="admin-name">{{ adminStore.adminProfile?.fullName || adminStore.adminProfile?.username }}</span>
            <el-icon><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人资料</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 主要内容区域 -->
    <el-container class="admin-main">
      <!-- 侧边栏 -->
      <el-aside width="250px" class="admin-sidebar">
        <el-menu
          :default-active="activeMenuItem"
          class="admin-menu"
          router
          @select="handleMenuSelect"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          
          <el-sub-menu index="courts">
            <template #title>
              <el-icon><OfficeBuilding /></el-icon>
              <span>场地管理</span>
            </template>
            <el-menu-item index="/admin/courts">场地列表</el-menu-item>
            <el-menu-item index="/admin/courts/schedule">场地时间</el-menu-item>
            <el-menu-item index="/admin/courts/statistics">场地统计</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="bookings">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span>预约管理</span>
            </template>
            <el-menu-item index="/admin/bookings">预约列表</el-menu-item>
            <el-menu-item index="/admin/bookings/calendar">预约日历</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="users">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/admin/users">用户列表</el-menu-item>
            <el-menu-item index="/admin/users/statistics">用户统计</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="admins" v-if="adminStore.isSuper">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>管理员</span>
            </template>
            <el-menu-item index="/admin/admins">管理员列表</el-menu-item>
            <el-menu-item index="/admin/admins/create">添加管理员</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/admin/settings">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 内容区域 -->
      <el-main class="admin-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAdminStore } from "../store/admin";
import { ElMessage, ElMessageBox } from "element-plus";
import { 
  ArrowDown, 
  DataBoard, 
  OfficeBuilding, 
  Calendar, 
  User, 
  UserFilled, 
  Setting 
} from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();
const adminStore = useAdminStore();

const activeMenuItem = computed(() => route.path);

const handleMenuSelect = (index) => {
  console.log("Menu selected:", index);
};

const handleCommand = async (command) => {
  switch (command) {
    case "profile":
      router.push("/admin/profile");
      break;
    case "logout":
      try {
        await ElMessageBox.confirm("确定要退出登录吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
        await adminStore.logout();
        ElMessage.success("退出登录成功");
        router.push("/admin/login");
      } catch (error) {
        // 用户取消了操作
      }
      break;
  }
};

onMounted(async () => {
  // 获取管理员信息
  try {
    await adminStore.fetchProfile();
  } catch (error) {
    ElMessage.error("获取管理员信息失败");
    router.push("/admin/login");
  }
});
</script>

<style scoped>
.admin-dashboard {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.admin-header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.header-left h1 {
  margin: 0;
  font-size: 20px;
  color: #303133;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
}

.admin-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s ease;
}

.admin-dropdown:hover {
  background-color: #f5f7fa;
}

.admin-name {
  margin: 0 8px;
  font-size: 14px;
  color: #303133;
}

.admin-main {
  flex: 1;
  overflow: hidden;
}

.admin-sidebar {
  background: #304156;
  overflow-y: auto;
}

.admin-menu {
  border: none;
  background: #304156;
}

:deep(.el-menu-item) {
  color: #bfcbd9;
  border-bottom: 1px solid #263445;
}

:deep(.el-menu-item:hover) {
  background-color: #263445 !important;
  color: #fff;
}

:deep(.el-menu-item.is-active) {
  background-color: #409eff !important;
  color: #fff;
}

:deep(.el-sub-menu__title) {
  color: #bfcbd9;
  border-bottom: 1px solid #263445;
}

:deep(.el-sub-menu__title:hover) {
  background-color: #263445 !important;
  color: #fff;
}

:deep(.el-sub-menu .el-menu-item) {
  background-color: #1f2d3d;
  min-height: 44px;
}

:deep(.el-sub-menu .el-menu-item:hover) {
  background-color: #001528 !important;
}

.admin-content {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

/* 滚动条样式 */
.admin-sidebar::-webkit-scrollbar {
  width: 6px;
}

.admin-sidebar::-webkit-scrollbar-track {
  background: #263445;
}

.admin-sidebar::-webkit-scrollbar-thumb {
  background: #409eff;
  border-radius: 3px;
}

.admin-content::-webkit-scrollbar {
  width: 6px;
}

.admin-content::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.admin-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
</style> 