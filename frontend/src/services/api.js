import axios from "axios";
import { useUserStore } from "../store/user";
import { ElMessage } from "element-plus";
import router from "../router";

// 创建axios实例
const api = axios.create({
<<<<<<< HEAD
  baseURL: "/api/v1",
=======
  baseURL: "/api",
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
  timeout: 5000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
<<<<<<< HEAD
    // 公共端点列表，这些端点不需要认证
    const publicEndpoints = [
      '/auth/',
      '/admin/login',
      '/courts',
      '/courts/booking'
    ];
    
    // 检查是否是公共端点 - 更精确的匹配逻辑
    const isPublicEndpoint = publicEndpoints.some(endpoint => {
      // 对于 '/courts' 端点，确保不匹配 '/admin/courts' 路径
      if (endpoint === '/courts') {
        return config.url === '/courts' || config.url.startsWith('/courts?') || config.url.startsWith('/courts/');
      }
      // 对于其他端点，使用原来的 includes 逻辑
      return config.url.includes(endpoint);
    });
    
    console.log(`API请求: ${config.method?.toUpperCase()} ${config.url}, 公共端点: ${isPublicEndpoint}`);
    
    // 如果不是公共端点，才添加Authorization头
    if (!isPublicEndpoint) {
      let token;
      // 更准确的URL检查 - 检查路径而不是完整URL
      const urlPath = config.url.startsWith('/') ? config.url : new URL(config.url, window.location.origin).pathname;
      console.log(`检查URL路径: ${urlPath}`);
      
      if (urlPath.includes('/admin/')) {
        token = localStorage.getItem("adminToken");
        console.log(`管理员API - Token存在: ${!!token}`);
        if (token) {
          console.log(`Token前缀: ${token.substring(0, 20)}...`);
        }
      } else {
        token = localStorage.getItem("token");
        console.log(`用户API - Token存在: ${!!token}`);
      }
      
      if (token) {
        config.headers["Authorization"] = `Bearer ${token}`;
        console.log("✅ 已添加Authorization头");
      } else {
        console.warn("❌ 没有找到token，无法添加Authorization头");
        console.warn("当前localStorage内容:", {
          adminToken: !!localStorage.getItem("adminToken"),
          userToken: !!localStorage.getItem("token")
        });
      }
    }
    
=======
    // 根据请求路径选择合适的token
    let token;
    if (config.url.includes('/v1/admin/')) {
      token = localStorage.getItem("adminToken");
    } else {
      token = localStorage.getItem("token");
    }
    
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
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
<<<<<<< HEAD
      const config = error.config;
      console.log(`API响应错误: ${error.response.status} ${config?.url}`);
      
      switch (error.response.status) {
        case 401:
          // 未授权，根据请求URL决定如何处理
          if (config?.url?.includes('/admin/')) {
            // 管理员API的401错误
            console.log("管理员认证失效，清除adminToken");
            localStorage.removeItem("adminToken");
            router.push("/admin/login");
            ElMessage.error("管理员登录已过期，请重新登录");
          } else {
            // 普通用户API的401错误
            console.log("用户认证失效，清除userToken");
            const userStore = useUserStore();
            userStore.logout();
            router.push("/login");
            ElMessage.error("登录已过期，请重新登录");
          }
          break;
        case 403:
          // 权限不足
          console.log("权限不足:", config?.url);
=======
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
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
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
  getAppointmentById: (id) => api.get(`/appointments/${id}`),
  validateAppointment: (appointmentData) => api.post("/appointments/validate", appointmentData),
<<<<<<< HEAD
  // 使用新的booking API获取场地和时间段信息
=======
  // 使用普通用户API获取场地和时间段信息
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
  getCourtsForBooking: (date) => api.get("/courts/booking", { params: { date } })
};

export const paymentAPI = {
  createPayment: (paymentData) => api.post("/payments", paymentData),
  getPaymentStatus: (id) => api.get(`/payments/${id}/status`),
<<<<<<< HEAD
  processPayment: (id, paymentMethod) => api.post(`/payments/${id}/process`, { paymentMethod }),
  cancelPayment: (id) => api.post(`/payments/${id}/cancel`),
  getUserPayments: () => api.get("/payments/my")
=======
  processPayment: (id, paymentMethod) => api.post(`/payments/${id}/process`, { paymentMethod })
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
};

// 管理员API
export const adminAPI = {
<<<<<<< HEAD
  login: (credentials) => api.post("/admin/login", credentials),
  getProfile: () => api.get("/admin/profile"),
  updateProfile: (adminData) => api.put("/admin/profile", adminData),
  getAllAdmins: () => api.get("/admin/list"),
  createAdmin: (adminData) => api.post("/admin", adminData),
  updateAdmin: (id, adminData) => api.put(`/admin/${id}`, adminData),
  deleteAdmin: (id) => api.delete(`/admin/${id}`),
  activateAdmin: (id) => api.put(`/admin/${id}/activate`),
  deactivateAdmin: (id) => api.put(`/admin/${id}/deactivate`),
  
  // 场地管理API
  getCourtsForManagement: (date) => api.get("/admin/courts/management", { params: { date } }),
  createCourt: (courtData) => api.post("/admin/courts", courtData),
  updateCourt: (id, courtData) => api.put(`/admin/courts/${id}`, courtData),
  deleteCourt: (id) => api.delete(`/admin/courts/${id}`),
  batchUpdateTimeSlots: (updates) => api.put("/admin/courts/time-slots/batch-update", updates),
  generateTimeSlots: (courtId, date) => api.post(`/admin/courts/${courtId}/generate-time-slots`, null, { params: { date } }),
  getCourtStatistics: () => api.get("/admin/courts/statistics"),
  setCourtAvailability: (courtId, available) => api.put(`/admin/courts/${courtId}/availability`, null, { params: { available } }),
  testAuth: () => api.get("/admin/courts/test-auth")
=======
  login: (credentials) => api.post("/v1/admin/login", credentials),
  getProfile: () => api.get("/v1/admin/profile"),
  updateProfile: (adminData) => api.put("/v1/admin/profile", adminData),
  getAllAdmins: () => api.get("/v1/admin/list"),
  createAdmin: (adminData) => api.post("/v1/admin", adminData),
  updateAdmin: (id, adminData) => api.put(`/v1/admin/${id}`, adminData),
  deleteAdmin: (id) => api.delete(`/v1/admin/${id}`),
  activateAdmin: (id) => api.put(`/v1/admin/${id}/activate`),
  deactivateAdmin: (id) => api.put(`/v1/admin/${id}/deactivate`),
  
  // 场地管理API
  getCourtsForManagement: (date) => api.get("/v1/admin/courts/management", { params: { date } }),
  createCourt: (courtData) => api.post("/v1/admin/courts", courtData),
  updateCourt: (id, courtData) => api.put(`/v1/admin/courts/${id}`, courtData),
  deleteCourt: (id) => api.delete(`/v1/admin/courts/${id}`),
  batchUpdateTimeSlots: (updates) => api.put("/v1/admin/courts/time-slots/batch-update", updates),
  generateTimeSlots: (courtId, date) => api.post(`/v1/admin/courts/${courtId}/generate-time-slots`, null, { params: { date } }),
  getCourtStatistics: () => api.get("/v1/admin/courts/statistics"),
  setCourtAvailability: (courtId, available) => api.put(`/v1/admin/courts/${courtId}/availability`, null, { params: { available } })
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
};

export default api;
