// 在浏览器控制台中运行此脚本来调试token问题

console.log("=== 前端Token调试信息 ===");

// 检查localStorage中的token
const adminToken = localStorage.getItem("adminToken");
const userToken = localStorage.getItem("token");

console.log("Admin Token:", adminToken ? "存在" : "不存在");
console.log("User Token:", userToken ? "存在" : "不存在");

if (adminToken) {
    console.log("Admin Token (前20字符):", adminToken.substring(0, 20) + "...");
    
    // 尝试解析JWT payload (不验证签名)
    try {
        const parts = adminToken.split('.');
        if (parts.length === 3) {
            const payload = JSON.parse(atob(parts[1]));
            console.log("JWT Payload:", payload);
        }
    } catch (e) {
        console.error("JWT解析失败:", e);
    }
}

// 检查Pinia store状态
if (window.Vue && window.Pinia) {
    console.log("检查Pinia store状态...");
    // 这里需要根据实际的store结构调整
}

// 测试发送请求时的头信息
console.log("=== 测试API请求头 ===");

// 创建一个测试请求来检查头信息
const testConfig = {
    url: "/admin/courts/test-auth",
    method: "GET"
};

// 模拟API拦截器逻辑
const publicEndpoints = [
    '/auth/',
    '/admin/login',
    '/courts',
    '/courts/booking'
];

const isPublicEndpoint = publicEndpoints.some(endpoint => 
    testConfig.url.includes(endpoint)
);

console.log("是否为公共端点:", isPublicEndpoint);

if (!isPublicEndpoint) {
    let token;
    if (testConfig.url.includes('/admin/')) {
        token = localStorage.getItem("adminToken");
        console.log("使用Admin Token");
    } else {
        token = localStorage.getItem("token");
        console.log("使用User Token");
    }
    
    if (token) {
        console.log("会添加Authorization头: Bearer " + token.substring(0, 20) + "...");
    } else {
        console.log("❌ 没有token，不会添加Authorization头！");
    }
} else {
    console.log("公共端点，不需要Authorization头");
}

console.log("=== 调试完成 ==="); 