import { defineStore } from "pinia";
import api from "../services/api";

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
        const response = await api.post("/auth/login", credentials);
        this.token = response.token;
        this.user = response.user;
        localStorage.setItem("token", response.token);
        return response;
      } catch (error) {
        console.error("��¼ʧ��:", error);
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async register(userData) {
      try {
        this.loading = true;
        const response = await api.post("/auth/register", userData);
        return response;
      } catch (error) {
        console.error("ע��ʧ��:", error);
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async logout() {
      try {
        await api.post("/auth/logout");
      } catch (error) {
        console.error("�ǳ�ʧ��:", error);
      } finally {
        this.token = null;
        this.user = null;
        localStorage.removeItem("token");
      }
    },

    async fetchUserProfile() {
      try {
        this.loading = true;
        const response = await api.get("/user/profile");
        this.user = response.user;
        return response;
      } catch (error) {
        console.error("��ȡ�û���Ϣʧ��:", error);
        throw error;
      } finally {
        this.loading = false;
      }
    },
  },
});
