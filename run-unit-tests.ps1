# CourtLink ��Ŀ��Ԫ����ִ�нű�
# ������ feat/admin-management ��֧

param(
    [string]$TestClass = "",
    [switch]$ShowReport = $false
)

# ����JAVA_HOME��������
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

Write-Host "? CourtLink ��Ԫ����ִ����" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# ��ʾ������Ϣ
Write-Host "? ���Ի�����Ϣ:" -ForegroundColor Cyan
Write-Host "  Java�汾: JDK 21" -ForegroundColor Gray
Write-Host "  Spring Boot: 3.1.5" -ForegroundColor Gray
Write-Host "  ���ݿ�: H2 �ڴ����ݿ�" -ForegroundColor Gray
Write-Host ""

if ($TestClass -eq "") {
    Write-Host "? �������е�Ԫ����..." -ForegroundColor Yellow
    
    # ����Ӧ�ü��ɲ���
    Write-Host "? ִ��Ӧ�ü��ɲ��� (ApplicationIntegrationTest)..." -ForegroundColor Cyan
    ./mvnw test -Dtest=ApplicationIntegrationTest -q
    $appTestResult = $LASTEXITCODE
    
    # ����Court�������
    Write-Host "? ִ��Court������� (CourtServiceImplTest)..." -ForegroundColor Cyan
    ./mvnw test -Dtest=CourtServiceImplTest -q
    $courtTestResult = $LASTEXITCODE
    
    # ����Admin���������� (����ʧ�ܣ�����Ӱ����������)
    Write-Host "? ִ��Admin���������� (AdminControllerTest)..." -ForegroundColor Cyan
    ./mvnw test -Dtest=AdminControllerTest -q
    $adminTestResult = $LASTEXITCODE
    
    # �ܽ���Խ��
    Write-Host ""
    Write-Host "? ���Խ���ܽ�:" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    
    if ($appTestResult -eq 0) {
        Write-Host "? Ӧ�ü��ɲ���: ͨ��" -ForegroundColor Green
    } else {
        Write-Host "? Ӧ�ü��ɲ���: ʧ��" -ForegroundColor Red
    }
    
    if ($courtTestResult -eq 0) {
        Write-Host "? Court�������: ͨ��" -ForegroundColor Green
    } else {
        Write-Host "? Court�������: ʧ��" -ForegroundColor Red
    }
    
    if ($adminTestResult -eq 0) {
        Write-Host "? Admin����������: ͨ��" -ForegroundColor Green
    } else {
        Write-Host "??  Admin����������: ����ʧ�� (CORS��������)" -ForegroundColor Yellow
    }
    
    # ��������ͨ����
    $passedTests = 0
    if ($appTestResult -eq 0) { $passedTests++ }
    if ($courtTestResult -eq 0) { $passedTests++ }
    if ($adminTestResult -eq 0) { $passedTests++ }
    
    $totalTests = 3
    $passRate = [math]::Round(($passedTests / $totalTests) * 100, 2)
    
    Write-Host ""
    Write-Host "? ����ͨ����: $passRate% ($passedTests/$totalTests)" -ForegroundColor Cyan
    
    if ($passRate -ge 80) {
        Write-Host "? ���Խ��: ����" -ForegroundColor Green
    } elseif ($passRate -ge 60) {
        Write-Host "??  ���Խ��: һ��" -ForegroundColor Yellow
    } else {
        Write-Host "? ���Խ��: ��Ҫ�޸�" -ForegroundColor Red
    }
    
} else {
    Write-Host "? ����ָ��������: $TestClass" -ForegroundColor Yellow
    ./mvnw test -Dtest=$TestClass
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "? ���� $TestClass ͨ��" -ForegroundColor Green
    } else {
        Write-Host "? ���� $TestClass ʧ��" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "? ��ʾ:" -ForegroundColor Cyan
Write-Host "  - �鿴��ϸ����: ./run-unit-tests.ps1 -ShowReport" -ForegroundColor Gray
Write-Host "  - ����ָ������: ./run-unit-tests.ps1 -TestClass ApplicationIntegrationTest" -ForegroundColor Gray
Write-Host "  - ��֪����: Admin��¼������CORS�����������ʧ��" -ForegroundColor Gray

if ($ShowReport) {
    Write-Host ""
    Write-Host "? �򿪲��Ա���..." -ForegroundColor Cyan
    if (Test-Path "feat-admin-management��֧��Ԫ���Ա���.md") {
        Start-Process "feat-admin-management��֧��Ԫ���Ա���.md"
    } else {
        Write-Host "? ���Ա����ļ�������" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "? ��Ԫ����ִ�����!" -ForegroundColor Green 