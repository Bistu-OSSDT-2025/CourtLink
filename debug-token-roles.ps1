# 调试JWT token中的角色信息
Write-Host "=== 调试JWT Token角色信息 ===" -ForegroundColor Green

# 1. 管理员登录
Write-Host "`n1. 管理员登录..." -ForegroundColor Blue
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/login" -Method POST -ContentType "application/json" -Body '{"usernameOrEmail": "admin", "password": "admin123"}'
$adminToken = $loginResponse.token
Write-Host "管理员登录成功，Token: $($adminToken.Substring(0, 20))..." -ForegroundColor Green

# 2. 测试权限端点
Write-Host "`n2. 测试权限端点..." -ForegroundColor Blue
$headers = @{ "Authorization" = "Bearer $adminToken" }
try {
    $authTest = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/courts/test-auth" -Method GET -Headers $headers
    Write-Host "权限测试成功:" -ForegroundColor Green
    Write-Host "  认证状态: $($authTest.authenticated)" -ForegroundColor Gray
    Write-Host "  用户名: $($authTest.username)" -ForegroundColor Gray
    Write-Host "  权限列表: $($authTest.authorities)" -ForegroundColor Gray
    Write-Host "  Principal类型: $($authTest.principal)" -ForegroundColor Gray
} catch {
    Write-Host "权限测试失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. 解析JWT Token (Base64解码)
Write-Host "`n3. 解析JWT Token..." -ForegroundColor Blue
try {
    $tokenParts = $adminToken.Split('.')
    if ($tokenParts.Length -eq 3) {
        $header = $tokenParts[0]
        $payload = $tokenParts[1]
        
        # 添加Base64填充
        while ($payload.Length % 4 -ne 0) {
            $payload += "="
        }
        
        $payloadBytes = [Convert]::FromBase64String($payload)
        $payloadJson = [System.Text.Encoding]::UTF8.GetString($payloadBytes)
        $payloadObj = $payloadJson | ConvertFrom-Json
        
        Write-Host "JWT Payload内容:" -ForegroundColor Yellow
        Write-Host "  Subject: $($payloadObj.sub)" -ForegroundColor Gray
        Write-Host "  Issued At: $($payloadObj.iat)" -ForegroundColor Gray
        Write-Host "  Expiration: $($payloadObj.exp)" -ForegroundColor Gray
        if ($payloadObj.role) {
            Write-Host "  Role: $($payloadObj.role)" -ForegroundColor Gray
        }
        if ($payloadObj.roles) {
            Write-Host "  Roles: $($payloadObj.roles)" -ForegroundColor Gray
        }
        if ($payloadObj.authorities) {
            Write-Host "  Authorities: $($payloadObj.authorities)" -ForegroundColor Gray
        }
        Write-Host "  完整Payload: $payloadJson" -ForegroundColor DarkGray
    }
} catch {
    Write-Host "JWT解析失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. 测试单个时间段更新
Write-Host "`n4. 测试单个时间段更新..." -ForegroundColor Blue
try {
    $singleUpdateData = @{
        timeSlotId = 3
        open = $true
        note = "单个测试"
    }
    
    $singleUpdateJson = $singleUpdateData | ConvertTo-Json
    Write-Host "发送单个更新数据: $singleUpdateJson" -ForegroundColor Gray
    
    $singleUpdateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/courts/time-slots/batch-update" -Method PUT -ContentType "application/json" -Headers $headers -Body "[$singleUpdateJson]"
    Write-Host "单个更新成功" -ForegroundColor Green
} catch {
    Write-Host "单个更新失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "错误详情: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

# 5. 测试其他管理员端点
Write-Host "`n5. 测试其他管理员端点..." -ForegroundColor Blue
try {
    $statsResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/courts/statistics" -Method GET -Headers $headers
    Write-Host "统计端点访问成功" -ForegroundColor Green
} catch {
    Write-Host "统计端点访问失败: $($_.Exception.Message)" -ForegroundColor Red
}

try {
    $mgmtResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/courts/management" -Method GET -Headers $headers
    Write-Host "管理端点访问成功，返回 $($mgmtResponse.Count) 个场地" -ForegroundColor Green
} catch {
    Write-Host "管理端点访问失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 调试完成 ===" -ForegroundColor Green 