#!/usr/bin/env pwsh
# CourtLink feat/admin-management ��֧���ܲ��Խű�
# �������й���Ա������

param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$TestRounds = 1,
    [bool]$Verbose = $true
)

# ������ɫ���
$Host.UI.RawUI.ForegroundColor = "White"

# ȫ�ֱ���
$global:TestResults = @()
$global:AdminToken = ""
$global:CreatedAdminId = $null

# ���ߺ���
function Write-ColoredOutput($message, $color = "White") {
    $Host.UI.RawUI.ForegroundColor = $color
    Write-Host $message
    $Host.UI.RawUI.ForegroundColor = "White"
}

function Log-Test($testName, $success, $message, $details = "") {
    $result = @{
        TestName = $testName
        Success = $success
        Message = $message
        Details = $details
        Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    }
    $global:TestResults += $result
    
    if ($success) {
        Write-ColoredOutput "? $testName - $message" "Green"
    } else {
        Write-ColoredOutput "? $testName - $message" "Red"
    }
    
    if ($Verbose -and $details) {
        Write-ColoredOutput "   ����: $details" "Gray"
    }
}

function Invoke-ApiRequest($method, $endpoint, $body = $null, $headers = @{}) {
    try {
        $uri = "$BaseUrl$endpoint"
        $defaultHeaders = @{
            "Content-Type" = "application/json"
            "Accept" = "application/json"
        }
        
        if ($global:AdminToken) {
            $defaultHeaders["Authorization"] = "Bearer $global:AdminToken"
        }
        
        $allHeaders = $defaultHeaders + $headers
        
        $params = @{
            Uri = $uri
            Method = $method
            Headers = $allHeaders
            UseBasicParsing = $true
        }
        
        if ($body) {
            $params.Body = ($body | ConvertTo-Json -Depth 10)
        }
        
        $response = Invoke-RestMethod @params
        return @{ Success = $true; Data = $response; StatusCode = 200 }
    }
    catch {
        $statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.value__ } else { 0 }
        $errorMsg = $_.Exception.Message
        return @{ Success = $false; Error = $errorMsg; StatusCode = $statusCode }
    }
}

function Test-ApplicationStartup {
    Write-ColoredOutput "`n? ����Ӧ�ó�������..." "Cyan"
    
    $maxRetries = 30
    $retryCount = 0
    
    while ($retryCount -lt $maxRetries) {
        try {
            $response = Invoke-RestMethod -Uri "$BaseUrl/api/health" -UseBasicParsing
            Log-Test "Ӧ���������" $true "Ӧ�ó���ɹ�����" "���������Ӧ����"
            return $true
        }
        catch {
            $retryCount++
            Write-ColoredOutput "�ȴ�Ӧ������... ($retryCount/$maxRetries)" "Yellow"
            Start-Sleep -Seconds 2
        }
    }
    
    Log-Test "Ӧ���������" $false "Ӧ�ó�������ʧ��" "��ʱ�ȴ����������Ӧ"
    return $false
}

function Test-AdminInitialization {
    Write-ColoredOutput "`n??? ���Թ���Ա��ʼ��..." "Cyan"
    
    # �ȴ�һ���ó�ʼ�����
    Start-Sleep -Seconds 3
    
    # ����Ĭ�Ϲ���Ա��¼
    $loginRequest = @{
        usernameOrEmail = "admin"
        password = "admin123"
    }
    
    $result = Invoke-ApiRequest "POST" "/api/admin/auth/login" $loginRequest
    
    if ($result.Success -and $result.Data.success) {
        $global:AdminToken = $result.Data.token
        Log-Test "Ĭ�Ϲ���Ա��ʼ��" $true "Ĭ�Ϲ���Ա�˻������ɹ�" "�û���: admin, ��¼�ɹ�"
        return $true
    } else {
        Log-Test "Ĭ�Ϲ���Ա��ʼ��" $false "Ĭ�Ϲ���Ա�˻�����ʧ��" $result.Error
        return $false
    }
}

function Test-AdminAuthentication {
    Write-ColoredOutput "`n? ���Թ���Ա��֤����..." "Cyan"
    
    # ������ȷ��¼
    $loginRequest = @{
        usernameOrEmail = "admin"
        password = "admin123"
    }
    
    $result = Invoke-ApiRequest "POST" "/api/admin/auth/login" $loginRequest
    
    if ($result.Success -and $result.Data.success) {
        $global:AdminToken = $result.Data.token
        Log-Test "����Ա��¼" $true "��ȷƾ�ݵ�¼�ɹ�" "��ȡ������"
    } else {
        Log-Test "����Ա��¼" $false "��ȷƾ�ݵ�¼ʧ��" $result.Error
        return $false
    }
    
    # ���Դ��������¼
    $wrongLoginRequest = @{
        usernameOrEmail = "admin"
        password = "wrongpassword"
    }
    
    $result = Invoke-ApiRequest "POST" "/api/admin/auth/login" $wrongLoginRequest
    
    if (-not $result.Success -or -not $result.Data.success) {
        Log-Test "���������¼" $true "�������뱻��ȷ�ܾ�" "��ȫ��֤����"
    } else {
        Log-Test "���������¼" $false "���������¼�ɹ�����ȫ©��" "Ӧ�þܾ���������"
    }
    
    # ���Եǳ�
    $result = Invoke-ApiRequest "POST" "/api/admin/auth/logout" $null @{"Authorization" = "Bearer $global:AdminToken"}
    
    if ($result.Success -and $result.Data.success) {
        Log-Test "����Ա�ǳ�" $true "�ǳ��ɹ�" "�Ự��ȷ��ֹ"
    } else {
        Log-Test "����Ա�ǳ�" $false "�ǳ�ʧ��" $result.Error
    }
    
    return $true
}

function Test-AdminManagement {
    Write-ColoredOutput "`n? ���Թ���Ա������..." "Cyan"
    
    # ���µ�¼��ȡ����
    $loginRequest = @{
        usernameOrEmail = "admin"
        password = "admin123"
    }
    $loginResult = Invoke-ApiRequest "POST" "/api/admin/auth/login" $loginRequest
    if ($loginResult.Success) {
        $global:AdminToken = $loginResult.Data.token
    }
    
    # ���Դ�������Ա
    $createAdminRequest = @{
        username = "testadmin"
        email = "testadmin@courtlink.com"
        password = "testpass123"
        fullName = "���Թ���Ա"
        role = "ADMIN"
    }
    
    $result = Invoke-ApiRequest "POST" "/api/admin/admins" $createAdminRequest
    
    if ($result.Success -and $result.Data.success) {
        $global:CreatedAdminId = $result.Data.data.id
        Log-Test "��������Ա" $true "����Ա�����ɹ�" "ID: $global:CreatedAdminId, �û���: testadmin"
    } else {
        Log-Test "��������Ա" $false "����Ա����ʧ��" $result.Error
    }
    
    # ���Ի�ȡ���й���Ա
    $result = Invoke-ApiRequest "GET" "/api/admin/admins?page=0&size=10"
    
    if ($result.Success -and $result.Data.success) {
        $adminCount = $result.Data.data.Count
        Log-Test "��ȡ����Ա�б�" $true "�ɹ���ȡ����Ա�б�" "�� $adminCount ������Ա"
    } else {
        Log-Test "��ȡ����Ա�б�" $false "��ȡ����Ա�б�ʧ��" $result.Error
    }
    
    # ���Ի�ȡ�ض�����Ա
    if ($global:CreatedAdminId) {
        $result = Invoke-ApiRequest "GET" "/api/admin/admins/$global:CreatedAdminId"
        
        if ($result.Success -and $result.Data.success) {
            Log-Test "��ȡ�ض�����Ա" $true "�ɹ���ȡ����Ա����" "�û���: $($result.Data.data.username)"
        } else {
            Log-Test "��ȡ�ض�����Ա" $false "��ȡ����Ա����ʧ��" $result.Error
        }
    }
    
    # ���Ը��¹���Ա
    if ($global:CreatedAdminId) {
        $updateAdminRequest = @{
            username = "testadmin"
            email = "testadmin@courtlink.com"
            fullName = "���º�Ĳ��Թ���Ա"
            role = "ADMIN"
        }
        
        $result = Invoke-ApiRequest "PUT" "/api/admin/admins/$global:CreatedAdminId" $updateAdminRequest
        
        if ($result.Success -and $result.Data.success) {
            Log-Test "���¹���Ա" $true "����Ա���³ɹ�" "��������Ϊ: ���º�Ĳ��Թ���Ա"
        } else {
            Log-Test "���¹���Ա" $false "����Ա����ʧ��" $result.Error
        }
    }
    
    return $true
}

function Test-AdminStatusManagement {
    Write-ColoredOutput "`n? ���Թ���Ա״̬����..." "Cyan"
    
    if (-not $global:CreatedAdminId) {
        Log-Test "����Ա״̬����" $false "û�п��õĲ��Թ���ԱID" "��Ҫ�ȴ�������Ա"
        return $false
    }
    
    # ����ͣ�ù���Ա
    $result = Invoke-ApiRequest "PATCH" "/api/admin/admins/$global:CreatedAdminId/deactivate"
    
    if ($result.Success -and $result.Data.success) {
        Log-Test "ͣ�ù���Ա" $true "����Աͣ�óɹ�" "ID: $global:CreatedAdminId"
    } else {
        Log-Test "ͣ�ù���Ա" $false "����Աͣ��ʧ��" $result.Error
    }
    
    # ���Լ������Ա
    $result = Invoke-ApiRequest "PATCH" "/api/admin/admins/$global:CreatedAdminId/activate"
    
    if ($result.Success -and $result.Data.success) {
        Log-Test "�������Ա" $true "����Ա����ɹ�" "ID: $global:CreatedAdminId"
    } else {
        Log-Test "�������Ա" $false "����Ա����ʧ��" $result.Error
    }
    
    return $true
}

function Test-AdminStatistics {
    Write-ColoredOutput "`n? ���Թ���Աͳ�ƹ���..." "Cyan"
    
    # ���Ի�ȡ����Աͳ��
    $result = Invoke-ApiRequest "GET" "/api/admin/statistics"
    
    if ($result.Success -and $result.Data.success) {
        $stats = $result.Data.data
        $details = "����: $($stats.totalCount), ��Ծ: $($stats.activeCount), ����: $($stats.lockedCount)"
        Log-Test "��ȡ����Աͳ��" $true "ͳ�����ݻ�ȡ�ɹ�" $details
    } else {
        Log-Test "��ȡ����Աͳ��" $false "ͳ�����ݻ�ȡʧ��" $result.Error
    }
    
    return $true
}

function Test-AdminSystemHealth {
    Write-ColoredOutput "`n? ���Թ���Աϵͳ�������..." "Cyan"
    
    # ����ϵͳ����״̬
    $result = Invoke-ApiRequest "GET" "/api/admin/system/health"
    
    if ($result.Success -and $result.Data.success) {
        $health = $result.Data.health
        $healthData = $result.Data.data
        $details = "״̬: $health, ��������: $($healthData.healthScore)"
        Log-Test "ϵͳ�������" $true "ϵͳ����״̬����" $details
    } else {
        Log-Test "ϵͳ�������" $false "ϵͳ�������ʧ��" $result.Error
    }
    
    return $true
}

function Test-AdminDashboard {
    Write-ColoredOutput "`n? ���Թ���Ա�Ǳ��..." "Cyan"
    
    # �����Ǳ��ҳ�����
    try {
        $response = Invoke-WebRequest -Uri "$BaseUrl/api/admin/dashboard" -UseBasicParsing
        
        if ($response.StatusCode -eq 200 -and $response.Content.Contains("CourtLink �����̨")) {
            Log-Test "����Ա�Ǳ��" $true "�Ǳ��ҳ����ʳɹ�" "HTMLҳ����ȷ��Ⱦ"
        } else {
            Log-Test "����Ա�Ǳ��" $false "�Ǳ��ҳ�������쳣" "ҳ�����ݲ�����Ԥ�ڱ���"
        }
    }
    catch {
        Log-Test "����Ա�Ǳ��" $false "�Ǳ��ҳ�����ʧ��" $_.Exception.Message
    }
    
    return $true
}

function Test-SecurityConfiguration {
    Write-ColoredOutput "`n? ���԰�ȫ����..." "Cyan"
    
    # ����δ��Ȩ���ʹ���ԱAPI
    $oldToken = $global:AdminToken
    $global:AdminToken = ""
    
    $result = Invoke-ApiRequest "GET" "/api/admin/admins"
    
    if (-not $result.Success -or $result.StatusCode -eq 401 -or $result.StatusCode -eq 403) {
        Log-Test "δ��Ȩ���ʱ���" $true "δ��Ȩ���ʱ���ȷ�ܾ�" "��ȫ��������"
    } else {
        Log-Test "δ��Ȩ���ʱ���" $false "δ��Ȩ����δ���ܾ�" "���ڰ�ȫ©��"
    }
    
    $global:AdminToken = $oldToken
    
    # ����H2����̨����
    try {
        $response = Invoke-WebRequest -Uri "$BaseUrl/h2-console" -UseBasicParsing
        if ($response.StatusCode -eq 200) {
            Log-Test "H2����̨����" $true "H2����̨����������" "��������������ȷ"
        }
    }
    catch {
        Log-Test "H2����̨����" $false "H2����̨����ʧ��" $_.Exception.Message
    }
    
    return $true
}

function Cleanup-TestData {
    Write-ColoredOutput "`n? �����������..." "Cyan"
    
    # ɾ�����Դ����Ĺ���Ա
    if ($global:CreatedAdminId) {
        $result = Invoke-ApiRequest "DELETE" "/api/admin/admins/$global:CreatedAdminId"
        
        if ($result.Success -and $result.Data.success) {
            Log-Test "�����������" $true "���Թ���Աɾ���ɹ�" "ID: $global:CreatedAdminId"
        } else {
            Log-Test "�����������" $false "���Թ���Աɾ��ʧ��" $result.Error
        }
    }
}

function Generate-TestReport {
    Write-ColoredOutput "`n? ���ɲ��Ա���..." "Cyan"
    
    $totalTests = $global:TestResults.Count
    $passedTests = ($global:TestResults | Where-Object { $_.Success }).Count
    $failedTests = $totalTests - $passedTests
    $successRate = if ($totalTests -gt 0) { [math]::Round(($passedTests / $totalTests) * 100, 2) } else { 0 }
    
    $report = @{
        TestSummary = @{
            TotalTests = $totalTests
            PassedTests = $passedTests
            FailedTests = $failedTests
            SuccessRate = $successRate
        }
        TestResults = $global:TestResults
        TestConfiguration = @{
            BaseUrl = $BaseUrl
            TestRounds = $TestRounds
            Branch = "feat/admin-management"
            TestDate = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        }
    }
    
    $reportFile = "feat-admin-management-test-results-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
    $report | ConvertTo-Json -Depth 10 | Out-File -FilePath $reportFile -Encoding UTF8
    
    Write-ColoredOutput "`n? �����ܽᱨ��" "Yellow"
    Write-ColoredOutput "=================" "Yellow"
    Write-ColoredOutput "�ܲ�����: $totalTests" "White"
    Write-ColoredOutput "ͨ������: $passedTests" "Green"
    Write-ColoredOutput "ʧ�ܲ���: $failedTests" "Red"
    Write-ColoredOutput "�ɹ���: $successRate%" $(if ($successRate -ge 90) { "Green" } elseif ($successRate -ge 70) { "Yellow" } else { "Red" })
    Write-ColoredOutput "�����ļ�: $reportFile" "Cyan"
    
    if ($failedTests -gt 0) {
        Write-ColoredOutput "`n? ʧ�ܵĲ���:" "Red"
        $global:TestResults | Where-Object { -not $_.Success } | ForEach-Object {
            Write-ColoredOutput "  - $($_.TestName): $($_.Message)" "Red"
        }
    }
    
    return $report
}

# ������ִ�к���
function Start-AdminManagementBranchTest {
    Write-ColoredOutput "? ��ʼ feat/admin-management ��֧���ܲ���" "Magenta"
    Write-ColoredOutput "================================================" "Magenta"
    Write-ColoredOutput "����URL: $BaseUrl" "White"
    Write-ColoredOutput "�����ִ�: $TestRounds" "White"
    Write-ColoredOutput "��ϸģʽ: $Verbose" "White"
    Write-ColoredOutput "��ʼʱ��: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "White"
    
    for ($round = 1; $round -le $TestRounds; $round++) {
        if ($TestRounds -gt 1) {
            Write-ColoredOutput "`n? �� $round �ֲ���" "Magenta"
        }
        
        # ִ�����в���
        if (-not (Test-ApplicationStartup)) {
            Write-ColoredOutput "? Ӧ�ó�������ʧ�ܣ���ֹ����" "Red"
            break
        }
        
        Test-AdminInitialization
        Test-AdminAuthentication
        Test-AdminManagement
        Test-AdminStatusManagement
        Test-AdminStatistics
        Test-AdminSystemHealth
        Test-AdminDashboard
        Test-SecurityConfiguration
        Cleanup-TestData
    }
    
    # ���ɲ��Ա���
    $report = Generate-TestReport
    
    # ���ݳɹ���ȷ���˳�����
    if ($report.TestSummary.SuccessRate -ge 90) {
        Write-ColoredOutput "`n? ��֧���ܲ������ - ����ͨ��" "Green"
        exit 0
    } elseif ($report.TestSummary.SuccessRate -ge 70) {
        Write-ColoredOutput "`n?? ��֧���ܲ������ - ��������" "Yellow"
        exit 1
    } else {
        Write-ColoredOutput "`n? ��֧���ܲ������ - ������������" "Red"
        exit 2
    }
}

# ��������
Start-AdminManagementBranchTest
