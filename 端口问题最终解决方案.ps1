# CourtLinkӦ��8080�˿��������ս������
# ���Spring BootӦ���������˿��޷����ʵ�����

param(
    [string]$Port = "8080",
    [switch]$Debug,
    [switch]$Force
)

$ErrorActionPreference = "Continue"

Write-Host "? CourtLinkӦ�ö˿������������" -ForegroundColor Green
Write-Host "=" * 50 -ForegroundColor Cyan

# ����Java����
function Set-JavaEnvironment {
    Write-Host "��1��������Java����" -ForegroundColor Yellow
    $env:JAVA_HOME = "C:\Users\ROG\.vscode\cursor1\Oracle_JDK-21"
    $env:PATH = "C:\Users\ROG\.vscode\cursor1\Oracle_JDK-21\bin;" + $env:PATH
    
    Write-Host "? JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green
    
    try {
        $javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
        Write-Host "? Java�汾: $javaVersion" -ForegroundColor Green
    } catch {
        Write-Host "? Java��������ʧ��" -ForegroundColor Red
        return $false
    }
    return $true
}

# ��������Java����
function Stop-AllJavaProcesses {
    Write-Host "��2������������Java����" -ForegroundColor Yellow
    
    $javaProcesses = Get-Process java -ErrorAction SilentlyContinue
    if ($javaProcesses) {
        Write-Host "���� $($javaProcesses.Count) ��Java���̣�����ֹͣ..." -ForegroundColor Yellow
        $javaProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 3
        Write-Host "? ����������Java����" -ForegroundColor Green
    } else {
        Write-Host "? û�������е�Java����" -ForegroundColor Green
    }
}

# ���˿�ռ��
function Test-PortUsage {
    param([string]$TestPort)
    
    Write-Host "��3�������˿� $TestPort ռ�����" -ForegroundColor Yellow
    
    $portUsage = netstat -ano | findstr ":$TestPort"
    if ($portUsage) {
        Write-Host "?? �˿� $TestPort �����½���ռ��:" -ForegroundColor Yellow
        $portUsage | ForEach-Object { Write-Host "   $_" -ForegroundColor White }
        
        if ($Force) {
            Write-Host "ǿ��ģʽ�������ͷŶ˿�..." -ForegroundColor Yellow
            # �����������ͷŶ˿ڵ��߼�
        }
    } else {
        Write-Host "? �˿� $TestPort ����" -ForegroundColor Green
    }
}

# Ӧ�ñ���ʹ��
function Build-Application {
    Write-Host "��4�������±���ʹ��Ӧ��" -ForegroundColor Yellow
    
    try {
        Write-Host "ִ��: mvn clean package -DskipTests -q" -ForegroundColor Cyan
        mvn clean package -DskipTests -q
        
        $jarFile = Get-ChildItem target\*.jar | Where-Object {$_.Name -notlike "*sources*" -and $_.Name -notlike "*javadoc*"} | Select-Object -First 1
        if ($jarFile) {
            Write-Host "? �����ɹ�: $($jarFile.Name)" -ForegroundColor Green
            return $jarFile.FullName
        } else {
            Write-Host "? δ�ҵ���ִ��JAR�ļ�" -ForegroundColor Red
            return $null
        }
    } catch {
        Write-Host "? ����ʧ��: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# ����1��ʹ��Maven�������
function Start-WithMaven {
    param([string]$StartPort)
    
    Write-Host "����1��ʹ��Maven�������" -ForegroundColor Magenta
    
    try {
        Write-Host "ִ��: mvn spring-boot:run -Dspring-boot.run.arguments='--server.port=$StartPort'" -ForegroundColor Cyan
        Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run", "-Dspring-boot.run.arguments=--server.port=$StartPort" -NoNewWindow
        return $true
    } catch {
        Write-Host "? Maven����ʧ��: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# ����2��ֱ������JAR
function Start-WithJar {
    param([string]$JarPath, [string]$StartPort)
    
    Write-Host "����2��ֱ������JAR�ļ�" -ForegroundColor Magenta
    
    $javaArgs = @(
        "-Xmx1024m",
        "-Xms512m", 
        "-Dserver.port=$StartPort",
        "-Dspring.profiles.active=default",
        "-Dlogging.level.org.springframework.boot=INFO",
        "-jar",
        $JarPath
    )
    
    try {
        Write-Host "ִ��: java $($javaArgs -join ' ')" -ForegroundColor Cyan
        Start-Process -FilePath "java" -ArgumentList $javaArgs -NoNewWindow
        return $true
    } catch {
        Write-Host "? JAR����ʧ��: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# ����3��ʹ�ò�ͬ��������
function Start-WithCustomConfig {
    param([string]$JarPath, [string]$StartPort)
    
    Write-Host "����3��ʹ���Զ�����������" -ForegroundColor Magenta
    
    # ������ʱ�����ļ�
    $tempConfig = @"
server:
  port: $StartPort
  address: 0.0.0.0
  tomcat:
    threads:
      max: 200
      min-spare: 10

spring:
  main:
    web-application-type: servlet

logging:
  level:
    org.springframework.boot: INFO
    org.apache.catalina: INFO
"@
    
    $configFile = "application-temp.yml"
    $tempConfig | Out-File -FilePath $configFile -Encoding UTF8
    
    $javaArgs = @(
        "-Xmx1024m",
        "-Xms512m",
        "-Dspring.config.additional-location=./$configFile",
        "-Dspring.profiles.active=temp",
        "-jar",
        $JarPath
    )
    
    try {
        Write-Host "ִ��: java $($javaArgs -join ' ')" -ForegroundColor Cyan
        Start-Process -FilePath "java" -ArgumentList $javaArgs -NoNewWindow
        return $true
    } catch {
        Write-Host "? �Զ�����������ʧ��: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# ����Ӧ�÷���
function Test-ApplicationAccess {
    param([string]$TestPort, [int]$MaxAttempts = 10)
    
    Write-Host "��5��������Ӧ�÷��� (�˿� $TestPort)" -ForegroundColor Yellow
    
    for ($i = 1; $i -le $MaxAttempts; $i++) {
        Write-Host "���� $i/$MaxAttempts : �ȴ�Ӧ������..." -ForegroundColor Cyan
        Start-Sleep -Seconds 10
        
        # ���˿ڼ���
        $listening = netstat -ano | findstr "LISTENING" | findstr ":$TestPort"
        if ($listening) {
            Write-Host "? �˿� $TestPort ���ڼ�����" -ForegroundColor Green
            Write-Host "   $listening" -ForegroundColor White
        } else {
            Write-Host "?? �˿� $TestPort ��δ����" -ForegroundColor Yellow
            continue
        }
        
        # ���Խ������
        try {
            $health = Invoke-RestMethod -Uri "http://localhost:$TestPort/actuator/health" -TimeoutSec 5
            Write-Host "? �������ɹ���״̬: $($health.status)" -ForegroundColor Green
            
            # �����û�API
            try {
                $users = Invoke-RestMethod -Uri "http://localhost:$TestPort/api/users" -TimeoutSec 5
                Write-Host "? �û�API���ʳɹ������� $($users.Count) ����¼" -ForegroundColor Green
                
                Write-Host "=" * 50 -ForegroundColor Green
                Write-Host "? Ӧ�������ɹ���" -ForegroundColor Green
                Write-Host "? Ӧ�õ�ַ: http://localhost:$TestPort" -ForegroundColor Cyan
                Write-Host "? �������: http://localhost:$TestPort/actuator/health" -ForegroundColor Cyan
                Write-Host "? �û�API: http://localhost:$TestPort/api/users" -ForegroundColor Cyan
                Write-Host "? ����API: http://localhost:$TestPort/api/courts" -ForegroundColor Cyan
                Write-Host "? API�ĵ�: http://localhost:$TestPort/swagger-ui/index.html" -ForegroundColor Cyan
                Write-Host "=" * 50 -ForegroundColor Green
                
                return $true
            } catch {
                Write-Host "?? �û�API����ʧ��: $($_.Exception.Message)" -ForegroundColor Yellow
            }
        } catch {
            Write-Host "? �������ʧ��: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    
    Write-Host "? Ӧ�÷��ʲ���ʧ��" -ForegroundColor Red
    return $false
}

# ��ִ������
function Main {
    Write-Host "��ʼִ��CourtLinkӦ�������������..." -ForegroundColor Cyan
    
    # ��1��������Java����
    if (-not (Set-JavaEnvironment)) {
        Write-Host "? Java��������ʧ�ܣ��˳�" -ForegroundColor Red
        return
    }
    
    # ��2�����������н���
    Stop-AllJavaProcesses
    
    # ��3�������˿�
    Test-PortUsage -TestPort $Port
    
    # ��4��������Ӧ��
    $jarPath = Build-Application
    if (-not $jarPath) {
        Write-Host "? Ӧ�ù���ʧ�ܣ��˳�" -ForegroundColor Red
        return
    }
    
    # ���Բ�ͬ����������
    $success = $false
    
    Write-Host "? ��������Ӧ��..." -ForegroundColor Green
    
    # ����1��Maven���
    if (Start-WithMaven -StartPort $Port) {
        if (Test-ApplicationAccess -TestPort $Port -MaxAttempts 6) {
            $success = $true
        } else {
            Write-Host "����1ʧ�ܣ�ֹͣ���̲����Է���2" -ForegroundColor Yellow
            Stop-AllJavaProcesses
        }
    }
    
    # ����2��ֱ������JAR
    if (-not $success) {
        if (Start-WithJar -JarPath $jarPath -StartPort $Port) {
            if (Test-ApplicationAccess -TestPort $Port -MaxAttempts 6) {
                $success = $true
            } else {
                Write-Host "����2ʧ�ܣ�ֹͣ���̲����Է���3" -ForegroundColor Yellow
                Stop-AllJavaProcesses
            }
        }
    }
    
    # ����3���Զ�������
    if (-not $success) {
        if (Start-WithCustomConfig -JarPath $jarPath -StartPort $Port) {
            if (Test-ApplicationAccess -TestPort $Port -MaxAttempts 6) {
                $success = $true
            }
        }
    }
    
    if ($success) {
        Write-Host "? CourtLinkӦ�óɹ��������ɷ��ʣ�" -ForegroundColor Green
        
        # ���ڿ������зֲ�����
        Write-Host "? ��һ������ִ�зֲ����ԣ�" -ForegroundColor Cyan
        Write-Host "   .\health-check.ps1" -ForegroundColor White
        Write-Host "   .\quick-test.ps1 -Rounds 3" -ForegroundColor White
        Write-Host "   .\comprehensive-test.ps1 -TestRounds 3" -ForegroundColor White
        
    } else {
        Write-Host "? ��������������ʧ����" -ForegroundColor Red
        Write-Host "? ������������Ŀ��" -ForegroundColor Yellow
        Write-Host "   1. ����ǽ����" -ForegroundColor White
        Write-Host "   2. ��������" -ForegroundColor White  
        Write-Host "   3. Spring Boot�����ļ�" -ForegroundColor White
        Write-Host "   4. H2���ݿ�����" -ForegroundColor White
    }
}

# ִ��������
Main 