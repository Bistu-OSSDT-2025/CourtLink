import { defineStore } from "pinia";
import { authAPI } from "../services/api";

export const useUserStore = defineStore("user", {
  state: () => ({
    user: null,
    token: localStorage.getItem("token"),
    loading: false,
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    userProfile: (state) => state.user,
  },

  actions: {
    async login(credentials) {
      try {
        this.loading = true;
        const response = await authAPI.login(credentials);
        this.token = response.token;
        this.user = response.user;
        localStorage.setItem("token", response.token);
        return response;
      } catch (error) {
        console.error("登录失败:", error);
        throw new Error(error.response?.data?.message || "登录失败，请稍后重试");
      } finally {
        this.loading = false;
      }
    },

    async register(userData) {
      try {
        this.loading = true;
        const response = await authAPI.register(userData);
        return response;
      } catch (error) {
        console.error("注册失败:", error);
        throw new Error(error.response?.data?.message || "注册失败，请稍后重试");
      } finally {
        this.loading = false;
      }
    },

    async logout() {
      try {
        await authAPI.logout();
      } catch (error) {
        console.error("退出登录失败:", error);
      } finally {
        this.token = null;
        this.user = null;
        localStorage.removeItem("token");
      }
    },

    async fetchUserProfile() {
      try {
        this.loading = true;
        const response = await authAPI.get("/user/profile");
        this.user = response.user;
        return response;
      } catch (error) {
        console.error("获取用户信息失败:", error);
        throw new Error(error.response?.data?.message || "获取用户信息失败，请稍后重试");
      } finally {
        this.loading = false;
      }
    },
  },
});
