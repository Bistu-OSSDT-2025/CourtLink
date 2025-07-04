<template>
  <div class="forgot-password-container">
    <el-card class="forgot-password-card">
      <template #header>
        <h2>找回密码</h2>
      </template>
      
      <el-form
        ref="forgotPasswordFormRef"
        :model="forgotPasswordForm"
        :rules="rules"
        label-position="top"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="forgotPasswordForm.email"
            placeholder="请输入注册时使用的邮箱"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="submit-button"
            @click="handleSubmit"
          >
            发送重置链接
          </el-button>
        </el-form-item>

        <div class="links">
          <router-link to="/login">返回登录</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";

const router = useRouter();
const loading = ref(false);
const forgotPasswordFormRef = ref();

const forgotPasswordForm = reactive({
  email: "",
});

const rules = {
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入有效的邮箱地址", trigger: "blur" },
  ],
};

const handleSubmit = async () => {
  if (!forgotPasswordFormRef.value) return;

  try {
    await forgotPasswordFormRef.value.validate();
    loading.value = true;

    // TODO: 调用重置密码API
    ElMessage.success("重置链接已发送到您的邮箱，请查收");
    router.push("/login");
  } catch (error) {
    ElMessage.error(error.message || "发送重置链接失败，请稍后重试");
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.forgot-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.forgot-password-card {
  width: 100%;
  max-width: 400px;
}

.submit-button {
  width: 100%;
}

.links {
  margin-top: 1rem;
  text-align: center;
}

.links a {
  color: var(--el-color-primary);
  text-decoration: none;
}

.links a:hover {
  text-decoration: underline;
}
</style> 