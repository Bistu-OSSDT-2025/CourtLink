import axios from "axios";
import { useUserStore } from "../store/user";
import { ElMessage } from "element-plus";
import router from "../router";

// 创建axios实例
const api = axios.create({
  baseURL: "http://localhost:8082/api",
  timeout: 5000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未授权，清除token并跳转到登录页面
          const userStore = useUserStore();
          userStore.logout();
          router.push("/login");
          ElMessage.error("登录已过期，请重新登录");
          break;
        case 403:
          // 权限不足
          ElMessage.error("没有权限访问该资源");
          break;
        case 404:
          // 资源不存在
          ElMessage.error("请求的资源不存在");
          break;
        default:
          ElMessage.error(error.response.data?.message || "服务器错误");
      }
    } else if (error.request) {
      ElMessage.error("网络错误，请检查您的网络连接");
    } else {
      ElMessage.error("请求错误");
    }
    return Promise.reject(error);
  }
);

// API接口
export const authAPI = {
  login: (credentials) => api.post("/auth/login", credentials),
  register: (userData) => api.post("/auth/register", userData),
  logout: () => api.post("/auth/logout"),
  forgotPassword: (email) => api.post("/auth/forgot-password", { email }),
  resetPassword: (token, password) => api.post("/auth/reset-password", { token, password }),
};

export const courtAPI = {
  getAllCourts: () => api.get("/courts"),
  getCourtById: (id) => api.get(`/courts/${id}`),
  getAvailableCourts: (date) => api.get("/courts/available", { params: { date } })
};

export const appointmentAPI = {
  createAppointment: (appointmentData) => api.post("/appointments", appointmentData),
  getMyAppointments: () => api.get("/appointments/my"),
  cancelAppointment: (id) => api.post(`/appointments/${id}/cancel`),
  getAppointmentById: (id) => api.get(`/appointments/${id}`)
};

export const paymentAPI = {
  createPayment: (paymentData) => api.post("/payments", paymentData),
  getPaymentStatus: (id) => api.get(`/payments/${id}/status`),
  processPayment: (id, paymentMethod) => api.post(`/payments/${id}/process`, { paymentMethod })
};

export default api;
