<template>
  <div class="home">
    <h1>欢迎使用 CourtLink 法院调解预约系统</h1>
    <el-row class="menu-container">
      <el-col :span="8" v-for="(item, index) in menuItems" :key="index">
        <el-card class="menu-card" shadow="hover">
          <h2>{{ item.title }}</h2>
          <p>{{ item.description }}</p>
          <el-button type="primary" @click="navigate(item.route)">
            {{ item.buttonText }}
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const menuItems = ref([
  {
    title: "调解预约",
    description: "预约调解服务和法院设施",
    buttonText: "开始预约",
    route: "/booking",
  },
  {
    title: "我的预约",
    description: "查看和管理您的预约记录",
    buttonText: "查看详情",
    route: "/my-bookings",
  },
  {
    title: "场所信息",
    description: "了解各个场所的详细信息",
    buttonText: "了解更多",
    route: "/venues",
  },
]);

const navigate = (route) => {
  router.push(route);
};
</script>

<style scoped>
.home {
  padding: 20px;
}

.menu-container {
  margin-top: 40px;
  gap: 20px;
}

.menu-card {
  margin: 10px;
  text-align: center;
}

h1 {
  color: #409EFF;
  margin-bottom: 40px;
}

.menu-card h2 {
  color: #303133;
  margin-bottom: 10px;
}

.menu-card p {
  color: #606266;
  margin-bottom: 20px;
}
</style>
