<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>登录</h2>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" class="login-button">
            登录
          </el-button>
        </el-form-item>
        <div class="login-links">
          <router-link to="/register">注册新账号</router-link>
          <router-link to="/forgot-password">忘记密码？</router-link>
        </div>
        <div class="admin-link">
          <router-link to="/admin/login" class="admin-login-link">管理员登录</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { User, Lock } from "@element-plus/icons-vue";
import { useUserStore } from "../store/user";
import { ElMessage } from "element-plus";

const router = useRouter();
const userStore = useUserStore();
const loginFormRef = ref(null);
const loading = ref(false);

const loginForm = reactive({
  username: "",
  password: "",
});

const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码长度不能小于6位", trigger: "blur" },
  ],
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;
  
  try {
    await loginFormRef.value.validate();
    loading.value = true;
    
    await userStore.login({
      username: loginForm.username,
      password: loginForm.password
    });
    
    ElMessage.success("登录成功");
    router.push("/");
  } catch (error) {
    ElMessage.error(error.message || "登录失败，请稍后重试");
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.login-card :deep(.el-card__header) {
  text-align: center;
  font-size: 24px;
  padding: 20px;
}

.login-button {
  width: 100%;
}

.login-links {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
}

.login-links a {
  color: #409EFF;
  text-decoration: none;
  font-size: 14px;
}

.login-links a:hover {
  color: #66b1ff;
}

.admin-link {
  text-align: center;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #e4e7ed;
}

.admin-login-link {
  color: #67c23a;
  text-decoration: none;
  font-size: 16px;
  font-weight: 500;
}

.admin-login-link:hover {
  color: #85ce61;
}
</style>
