@echo off
echo ========================================
echo ԤԼ������֧������ϵͳ - ������������
echo ========================================
echo.

REM ���Java����
echo [1/5] ���Java����...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ����: δ�ҵ�Java�������밲װJDK 11+
    pause
    exit /b 1
)
echo Java�������ͨ��

REM ���Maven����
echo [2/5] ���Maven����...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ����: δ�ҵ�Maven�������밲װMaven 3.6+
    pause
    exit /b 1
)
echo Maven�������ͨ��

REM ���Git����
echo [3/5] ���Git����...
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ����: δ�ҵ�Git�������밲װGit
    pause
    exit /b 1
)
echo Git�������ͨ��

REM ����������Ŀ
echo [4/5] ����������Ŀ...
mvn clean compile
if %errorlevel% neq 0 (
    echo ����: ��Ŀ����ʧ��
    pause
    exit /b 1
)
echo ��Ŀ����ɹ�

REM ����Ӧ��
echo [5/5] ����Ӧ��...
echo.
echo Ӧ�ý������µ�ַ����:
echo - API�ĵ�: http://localhost:8080/api/swagger-ui.html
echo - �������: http://localhost:8080/api/actuator/health
echo.
echo �� Ctrl+C ֹͣӦ��
echo.

mvn spring-boot:run

pause 