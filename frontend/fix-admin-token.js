// 临时修复脚本：强制为所有管理员API添加Authorization头
// 在浏览器控制台中运行此脚本

console.log("🔧 开始修复管理员API token问题...");

// 保存原始的fetch函数
const originalFetch = window.fetch;

// 覆盖fetch函数
window.fetch = function(url, options = {}) {
    console.log(`🌐 拦截请求: ${url}`);
    
    // 检查是否是管理员API
    if (url.includes('/admin/') && !url.includes('/admin/login')) {
        const adminToken = localStorage.getItem("adminToken");
        console.log(`🔑 管理员API请求 - Token存在: ${!!adminToken}`);
        
        if (adminToken) {
            // 确保headers对象存在
            if (!options.headers) {
                options.headers = {};
            }
            
            // 添加Authorization头
            options.headers["Authorization"] = `Bearer ${adminToken}`;
            console.log(`✅ 已为 ${url} 添加Authorization头`);
        } else {
            console.warn(`❌ 管理员API请求但没有token: ${url}`);
        }
    }
    
    // 调用原始fetch
    return originalFetch.call(this, url, options);
};

console.log("✅ 管理员API token修复已应用！");
console.log("现在请尝试访问场地管理页面");

// 5分钟后自动恢复原始fetch（避免永久影响）
setTimeout(() => {
    window.fetch = originalFetch;
    console.log("⏰ 临时修复已到期，已恢复原始fetch函数");
}, 5 * 60 * 1000); 