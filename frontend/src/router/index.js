import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "../store/user";
import { useAdminStore } from "../store/admin";

const routes = [
  {
    path: "/",
    name: "Home",
    component: () => import("../views/Home.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/login",
    name: "Login",
    component: () => import("../views/Login.vue"),
    meta: { guest: true }
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("../views/Register.vue"),
    meta: { guest: true }
  },
  {
    path: "/forgot-password",
    name: "ForgotPassword",
    component: () => import("../views/ForgotPassword.vue"),
    meta: { guest: true }
  },
  // 管理员路由
  {
    path: "/admin/login",
    name: "AdminLogin",
    component: () => import("../views/AdminLogin.vue"),
    meta: { adminGuest: true }
  },
  {
    path: "/admin",
    component: () => import("../views/AdminDashboard.vue"),
    meta: { requiresAdminAuth: true },
    children: [
      {
        path: "",
        redirect: "/admin/dashboard"
      },
      {
        path: "dashboard",
        name: "AdminDashboard",
        component: () => import("../views/admin/Dashboard.vue")
      },
      {
        path: "profile",
        name: "AdminProfile",
        component: () => import("../views/admin/Profile.vue")
      },
      {
        path: "courts",
        name: "AdminCourts",
        component: () => import("../views/admin/Courts.vue")
      },
      {
        path: "bookings",
        name: "AdminBookings",
        component: () => import("../views/admin/Bookings.vue")
      },
      {
        path: "users",
        name: "AdminUsers",
        component: () => import("../views/admin/Users.vue")
      },
      {
        path: "admins",
        name: "AdminManagement",
        component: () => import("../views/admin/AdminManagement.vue"),
        meta: { requiresSuper: true }
      },
      {
        path: "settings",
        name: "AdminSettings",
        component: () => import("../views/admin/Settings.vue")
      }
    ]
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("../views/NotFound.vue")
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  const adminStore = useAdminStore();
  const isAuthenticated = userStore.isAuthenticated;
  const isAdminAuthenticated = adminStore.isAuthenticated;

  // 管理员需要认证的页面
  if (to.matched.some(record => record.meta.requiresAdminAuth)) {
    if (!isAdminAuthenticated) {
      next({
        path: '/admin/login',
        query: { redirect: to.fullPath }
      });
    } else {
      // 检查是否需要超级管理员权限
      if (to.matched.some(record => record.meta.requiresSuper)) {
        if (!adminStore.isSuper) {
          next('/admin/dashboard');
          return;
        }
      }
      next();
    }
  }
  // 管理员游客页面（管理员登录）
  else if (to.matched.some(record => record.meta.adminGuest)) {
    if (isAdminAuthenticated) {
      next('/admin/dashboard');
    } else {
      next();
    }
  }
  // 用户需要认证的页面
  else if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!isAuthenticated) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
    } else {
      next();
    }
  }
  // 用户游客页面（登录、注册）
  else if (to.matched.some(record => record.meta.guest)) {
    if (isAuthenticated) {
      next('/');
    } else {
      next();
    }
  }
  // 其他页面
  else {
    next();
  }
});

export default router;
