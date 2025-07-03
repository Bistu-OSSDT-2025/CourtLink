<template>
  <div class="admin-login-container">
    <div class="admin-login-form">
      <div class="admin-login-header">
        <h2>管理员登录</h2>
        <p>CourtLink 羽毛球场地管理系统</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="admin-login-form-content"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="usernameOrEmail">
          <el-input
            v-model="loginForm.usernameOrEmail"
            placeholder="请输入用户名或邮箱"
            size="large"
            prefix-icon="User"
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="admin-login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="admin-login-footer">
        <router-link to="/login" class="back-to-user-login">
          返回用户登录
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useAdminStore } from "../store/admin";
import { ElMessage } from "element-plus";

const router = useRouter();
const adminStore = useAdminStore();

const loginFormRef = ref();
const loading = ref(false);

const loginForm = reactive({
  usernameOrEmail: "",
  password: "",
});

const loginRules = {
  usernameOrEmail: [
    { required: true, message: "请输入用户名或邮箱", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码长度至少6位", trigger: "blur" },
  ],
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;

  const valid = await loginFormRef.value.validate().catch(() => false);
  if (!valid) return;

  try {
    loading.value = true;
    await adminStore.login(loginForm);
    ElMessage.success("登录成功！");
    router.push("/admin/dashboard");
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.admin-login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.admin-login-form {
  background: white;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  padding: 40px;
  width: 100%;
  max-width: 400px;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.admin-login-header {
  text-align: center;
  margin-bottom: 30px;
}

.admin-login-header h2 {
  color: #333;
  margin-bottom: 8px;
  font-size: 28px;
  font-weight: 600;
}

.admin-login-header p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.admin-login-form-content {
  margin-bottom: 20px;
}

.admin-login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.admin-login-btn:hover {
  background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
}

.admin-login-footer {
  text-align: center;
  margin-top: 20px;
}

.back-to-user-login {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
}

.back-to-user-login:hover {
  color: #667eea;
}

:deep(.el-form-item__content) {
  flex-direction: column;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  padding: 0 16px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style> 