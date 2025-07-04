import { defineStore } from "pinia";
import { adminAPI } from "../services/api";

export const useAdminStore = defineStore("admin", {
  state: () => ({
    admin: null,
    token: localStorage.getItem("adminToken"),
    loading: false,
    admins: [], // 管理员列表（仅超级管理员可见）
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    adminProfile: (state) => state.admin,
    isSuper: (state) => state.admin?.role === 'SUPER_ADMIN',
    isAdmin: (state) => state.admin?.role === 'ADMIN',
    isModerator: (state) => state.admin?.role === 'MODERATOR',
    hasAdminRole: (state) => {
      return state.admin && ['SUPER_ADMIN', 'ADMIN', 'MODERATOR'].includes(state.admin.role);
    }
  },

  actions: {
    async login(credentials) {
      try {
        this.loading = true;
        const response = await adminAPI.login(credentials);
        console.log("管理员登录响应:", response);
        this.token = response.token;
        this.admin = response.admin;
        localStorage.setItem("adminToken", response.token);
        console.log("✅ 管理员token已保存到localStorage:", response.token.substring(0, 20) + "...");
        
        // 验证token是否真的保存了
        const savedToken = localStorage.getItem("adminToken");
        console.log("验证localStorage中的token:", savedToken ? "存在" : "不存在");
        
        return response;
      } catch (error) {
        console.error("管理员登录失败:", error);
        throw new Error(error.response?.data?.message || "登录失败，请稍后重试");
      } finally {
        this.loading = false;
      }
    },

    async logout() {
      try {
        // 可以调用后端的logout接口，如果有的话
        // await adminAPI.logout();
      } catch (error) {
        console.error("退出登录失败:", error);
      } finally {
        this.token = null;
        this.admin = null;
        this.admins = [];
        localStorage.removeItem("adminToken");
      }
    },

    async fetchProfile() {
      try {
        this.loading = true;
        const response = await adminAPI.getProfile();
        this.admin = response;
        return response;
      } catch (error) {
        console.error("获取管理员信息失败:", error);
        throw new Error(error.response?.data?.message || "获取管理员信息失败，请稍后重试");
      } finally {
        this.loading = false;
      }
    },

    async updateProfile(adminData) {
      try {
        this.loading = true;
        const response = await adminAPI.updateProfile(adminData);
        this.admin = response;
        return response;
      } catch (error) {
        console.error("更新管理员信息失败:", error);
        throw new Error(error.response?.data?.message || "更新管理员信息失败，请稍后重试");
      } finally {
        this.loading = false;
      }
    },

    async fetchAllAdmins() {
      try {
        this.loading = true;
        const response = await adminAPI.getAllAdmins();
        this.admins = response;
        return response;
      } catch (error) {
        console.error("获取管理员列表失败:", error);
        throw new Error(error.response?.data?.message || "获取管理员列表失败，请稍后重试");
      } finally {
        this.loading = false;
      }
    },

    async createAdmin(adminData) {
      try {
        this.loading = true;
        const response = await adminAPI.createAdmin(adminData);
        this.admins.push(response);
        return response;
      } catch (error) {
        console.error("创建管理员失败:", error);
        throw new Error(error.response?.data?.message || "创建管理员失败，请稍后重试");
      } finally {
        this.loading = false;
      }
    },

    async updateAdmin(id, adminData) {
      try {
        this.loading = true;
        const response = await adminAPI.updateAdmin(id, adminData);
        const index = this.admins.findIndex(admin => admin.id === id);
        if (index !== -1) {
          this.admins[index] = response;
        }
        return response;
      } catch (error) {
        console.error("更新管理员失败:", error);
        throw new Error(error.response?.data?.message || "更新管理员失败，请稍后重试");
      } finally {
        this.loading = false;
      }
    },

    async deleteAdmin(id) {
      try {
        this.loading = true;
        await adminAPI.deleteAdmin(id);
        this.admins = this.admins.filter(admin => admin.id !== id);
      } catch (error) {
        console.error("删除管理员失败:", error);
        throw new Error(error.response?.data?.message || "删除管理员失败，请稍后重试");
      } finally {
        this.loading = false;
      }
    },

    async activateAdmin(id) {
      try {
        this.loading = true;
        const response = await adminAPI.activateAdmin(id);
        const index = this.admins.findIndex(admin => admin.id === id);
        if (index !== -1) {
          this.admins[index] = response;
        }
        return response;
      } catch (error) {
        console.error("激活管理员失败:", error);
        throw new Error(error.response?.data?.message || "激活管理员失败，请稍后重试");
      } finally {
        this.loading = false;
      }
    },

    async deactivateAdmin(id) {
      try {
        this.loading = true;
        const response = await adminAPI.deactivateAdmin(id);
        const index = this.admins.findIndex(admin => admin.id === id);
        if (index !== -1) {
          this.admins[index] = response;
        }
        return response;
      } catch (error) {
        console.error("停用管理员失败:", error);
        throw new Error(error.response?.data?.message || "停用管理员失败，请稍后重试");
      } finally {
        this.loading = false;
      }
    },
  },
}); 