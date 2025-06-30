import axios from "axios";

const api = axios.create({
  baseURL: "/api",
  timeout: 5000,
  headers: {
    "Content-Type": "application/json",
  },
});

// ����������
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error("�������:", error);
    return Promise.reject(error);
  }
);

// ��Ӧ������
api.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    console.error("��Ӧ����:", error);
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // δ��Ȩ����� token ����ת����¼ҳ
          localStorage.removeItem("token");
          window.location.href = "/login";
          break;
        case 403:
          // Ȩ�޲���
          console.error("Ȩ�޲���");
          break;
        case 404:
          // �������Դ������
          console.error("�������Դ������");
          break;
        case 500:
          // ����������
          console.error("����������");
          break;
        default:
          console.error("δ֪����");
      }
    }
    return Promise.reject(error);
  }
);

export default api;
