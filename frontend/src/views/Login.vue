<template>
  <div class="login-container">
    <!-- 背景装饰元素 -->
    <div class="background-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
      <div class="floating-element element-1">🏓</div>
      <div class="floating-element element-2">🏸</div>
      <div class="floating-element element-3">🎾</div>
      <div class="floating-element element-4">🏀</div>
    </div>

    <!-- 登录表单卡片 -->
    <div class="login-card" v-motion-slide-visible-bottom :delay="200">
      <!-- 品牌头部 -->
      <div class="brand-header">
        <div class="brand-icon">
          <div class="icon-circle">
            <span class="main-icon">🏟️</span>
          </div>
        </div>
        <h1 class="brand-title">CourtLink</h1>
        <p class="brand-subtitle">专业场地预约系统</p>
      </div>

      <!-- 登录表单 -->
      <div class="login-form-section">
        <h2 class="form-title">欢迎回来</h2>
        <p class="form-subtitle">请登录您的账户</p>
        
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
          <el-form-item prop="username">
            <div class="input-wrapper">
              <label class="input-label">用户名</label>
              <el-input 
                v-model="loginForm.username" 
                placeholder="请输入用户名"
                class="custom-input"
                size="large"
              >
                <template #prefix>
                  <el-icon class="input-icon"><User /></el-icon>
                </template>
              </el-input>
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="input-wrapper">
              <label class="input-label">密码</label>
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="请输入密码"
                class="custom-input"
                size="large"
                show-password
              >
                <template #prefix>
                  <el-icon class="input-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </div>
          </el-form-item>

          <!-- 记住我和忘记密码 -->
          <div class="form-options">
            <el-checkbox v-model="rememberMe" class="remember-me">
              记住我
            </el-checkbox>
            <router-link to="/forgot-password" class="forgot-link">
              忘记密码？
            </router-link>
          </div>

          <!-- 登录按钮 -->
          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleLogin" 
              :loading="loading" 
              class="login-button"
              size="large"
            >
              <span v-if="!loading" class="button-content">
                <span class="button-icon">🚀</span>
                立即登录
              </span>
              <span v-else class="loading-content">
                <span class="loading-spinner">⏳</span>
                登录中...
              </span>
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 其他登录选项 -->
        <div class="other-options">
          <div class="divider">
            <span class="divider-text">或者</span>
          </div>
          
          <div class="register-section">
            <p class="register-text">还没有账户？</p>
            <router-link to="/register" class="register-link">
              <span class="register-icon">✨</span>
              注册新账号
            </router-link>
          </div>
        </div>

        <!-- 管理员入口 -->
        <div class="admin-portal">
          <div class="admin-divider"></div>
          <router-link to="/admin/login" class="admin-login-link">
            <span class="admin-icon">👑</span>
            <span class="admin-text">管理员登录</span>
            <span class="admin-arrow">→</span>
          </router-link>
        </div>
      </div>
    </div>

    <!-- 装饰性图标 -->
    <div class="decorative-icons">
      <div class="icon-floating icon-1">⭐</div>
      <div class="icon-floating icon-2">💫</div>
      <div class="icon-floating icon-3">✨</div>
    </div>
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
const rememberMe = ref(false);

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
    
    ElMessage.success("登录成功！欢迎回来！");
    router.push("/");
  } catch (error) {
    ElMessage.error(error.message || "登录失败，请检查用户名和密码");
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
/* 容器和背景 */
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, 
    #667eea 0%, 
    #764ba2 25%, 
    #f093fb 50%, 
    #f5576c 75%, 
    #4facfe 100%);
  background-size: 400% 400%;
  animation: gradientFlow 15s ease infinite;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

@keyframes gradientFlow {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 背景装饰形状 */
.background-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 10%;
  animation-delay: 2s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

.shape-4 {
  width: 180px;
  height: 180px;
  top: 30%;
  right: 30%;
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

/* 浮动运动图标 */
.floating-element {
  position: absolute;
  font-size: 24px;
  animation: floatBounce 8s ease-in-out infinite;
  z-index: 2;
}

.element-1 {
  top: 15%;
  left: 15%;
  animation-delay: 0s;
}

.element-2 {
  top: 25%;
  right: 20%;
  animation-delay: 2s;
}

.element-3 {
  bottom: 30%;
  left: 25%;
  animation-delay: 4s;
}

.element-4 {
  bottom: 20%;
  right: 15%;
  animation-delay: 6s;
}

@keyframes floatBounce {
  0%, 100% {
    transform: translateY(0px) scale(1);
  }
  25% {
    transform: translateY(-15px) scale(1.1);
  }
  50% {
    transform: translateY(-30px) scale(1);
  }
  75% {
    transform: translateY(-15px) scale(0.9);
  }
}

/* 登录卡片 */
.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(30px);
  border-radius: 24px;
  padding: 40px;
  width: 100%;
  max-width: 450px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.1),
    0 8px 24px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  z-index: 10;
  transition: all 0.3s ease;
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 
    0 30px 80px rgba(0, 0, 0, 0.15),
    0 12px 32px rgba(0, 0, 0, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

/* 品牌头部 */
.brand-header {
  text-align: center;
  margin-bottom: 40px;
}

.brand-icon {
  margin-bottom: 20px;
}

.icon-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
  animation: iconPulse 3s ease-in-out infinite;
}

@keyframes iconPulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 15px 40px rgba(102, 126, 234, 0.4);
  }
}

.main-icon {
  font-size: 36px;
  color: white;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
  font-weight: 500;
}

/* 表单区域 */
.login-form-section {
  text-align: left;
}

.form-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.form-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0 0 30px 0;
}

/* 输入框样式 */
.input-wrapper {
  margin-bottom: 20px;
}

.input-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.custom-input {
  --el-input-border-radius: 12px;
  --el-input-border-color: #e5e7eb;
  --el-input-focus-border-color: #667eea;
  --el-input-hover-border-color: #9ca3af;
}

.custom-input :deep(.el-input__wrapper) {
  padding: 12px 16px;
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: #9ca3af;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.input-icon {
  color: #667eea;
  font-size: 18px;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0 30px 0;
}

.remember-me {
  --el-checkbox-checked-bg-color: #667eea;
  --el-checkbox-checked-border-color: #667eea;
}

.forgot-link {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.forgot-link:hover {
  color: #4f46e5;
  text-decoration: underline;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  margin-bottom: 30px;
}

.login-button:hover {
  background: linear-gradient(135deg, #5a67d8, #6b46c1);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.button-content, .loading-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.button-icon, .loading-spinner {
  font-size: 18px;
}

.loading-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 其他选项 */
.other-options {
  margin: 30px 0;
}

.divider {
  position: relative;
  text-align: center;
  margin: 20px 0;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, #e5e7eb, transparent);
}

.divider-text {
  background: rgba(255, 255, 255, 0.95);
  padding: 0 16px;
  color: #6b7280;
  font-size: 14px;
  position: relative;
  z-index: 1;
}

/* 注册区域 */
.register-section {
  text-align: center;
  margin: 20px 0;
}

.register-text {
  color: #6b7280;
  font-size: 14px;
  margin: 0 0 12px 0;
}

.register-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.register-link:hover {
  background: rgba(102, 126, 234, 0.05);
  border-color: rgba(102, 126, 234, 0.3);
  transform: translateY(-1px);
}

.register-icon {
  font-size: 16px;
}

/* 管理员入口 */
.admin-portal {
  margin-top: 30px;
  text-align: center;
}

.admin-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #e5e7eb, transparent);
  margin: 20px 0;
}

.admin-login-link {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  text-decoration: none;
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 15px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
}

.admin-login-link:hover {
  background: linear-gradient(135deg, #059669, #047857);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.3);
}

.admin-icon {
  font-size: 18px;
}

.admin-arrow {
  font-size: 16px;
  transition: transform 0.3s ease;
}

.admin-login-link:hover .admin-arrow {
  transform: translateX(4px);
}

/* 装饰性图标 */
.decorative-icons {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 2;
}

.icon-floating {
  position: absolute;
  font-size: 20px;
  animation: iconFloat 10s ease-in-out infinite;
  opacity: 0.6;
}

.icon-1 {
  top: 20%;
  left: 80%;
  animation-delay: 0s;
}

.icon-2 {
  top: 70%;
  left: 5%;
  animation-delay: 3s;
}

.icon-3 {
  top: 40%;
  right: 5%;
  animation-delay: 6s;
}

@keyframes iconFloat {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
    opacity: 0.6;
  }
  25% {
    transform: translateY(-10px) rotate(90deg);
    opacity: 0.8;
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
    opacity: 1;
  }
  75% {
    transform: translateY(-10px) rotate(270deg);
    opacity: 0.8;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    padding: 15px;
  }
  
  .login-card {
    padding: 30px 20px;
    max-width: 100%;
  }
  
  .brand-title {
    font-size: 28px;
  }
  
  .form-title {
    font-size: 20px;
  }
  
  .shape {
    opacity: 0.5;
  }
  
  .floating-element {
    font-size: 20px;
  }
  
  .form-options {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .login-card {
    padding: 25px 15px;
    border-radius: 16px;
  }
  
  .icon-circle {
    width: 60px;
    height: 60px;
  }
  
  .main-icon {
    font-size: 28px;
  }
  
  .brand-title {
    font-size: 24px;
  }
  
  .brand-subtitle {
    font-size: 14px;
  }
}

/* 动画入场效果 */
@keyframes slideInBottom {
  from {
    opacity: 0;
    transform: translateY(50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-card {
  animation: slideInBottom 0.6s ease-out;
}

/* 表单验证错误样式优化 */
.login-form :deep(.el-form-item.is-error .el-input__wrapper) {
  border-color: #f56565;
  box-shadow: 0 4px 12px rgba(245, 101, 101, 0.15);
}

.login-form :deep(.el-form-item__error) {
  color: #f56565;
  font-size: 12px;
  font-weight: 500;
}
</style>
