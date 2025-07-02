@echo off
echo ========================================
echo CourtLink �˶�����ԤԼϵͳ - �������нű�
echo ========================================
echo.

echo [1/5] ����֮ǰ�Ĳ��Խ��...
if exist target\surefire-reports rmdir /s /q target\surefire-reports
if exist target\test-classes rmdir /s /q target\test-classes
echo �������
echo.

echo [2/5] ������Ŀ...
call mvn clean compile test-compile
if %errorlevel% neq 0 (
    echo ����ʧ�ܣ�����������
    pause
    exit /b 1
)
echo ����ɹ�
echo.

echo [3/5] ���е�Ԫ����...
call mvn test -Dtest=*Test -DfailIfNoTests=false
if %errorlevel% neq 0 (
    echo ��Ԫ����ʧ��
    pause
    exit /b 1
)
echo ��Ԫ�������
echo.

echo [4/5] ���м��ɲ���...
call mvn test -Dtest=*IntegrationTest -DfailIfNoTests=false
if %errorlevel% neq 0 (
    echo ���ɲ���ʧ��
    pause
    exit /b 1
)
echo ���ɲ������
echo.

echo [5/5] �������ܲ���...
call mvn test -Dtest=*PerformanceTest -DfailIfNoTests=false
if %errorlevel% neq 0 (
    echo ���ܲ���ʧ��
    pause
    exit /b 1
)
echo ���ܲ������
echo.

echo ========================================
echo ���в�����ɣ�
echo ========================================
echo.
echo ���Ա���λ�ã�
echo - ��Ԫ����: target\surefire-reports\
echo - �����ʱ���: target\site\jacoco\
echo.

echo �Ƿ�򿪲��Ա��棿(Y/N)
set /p choice=
if /i "%choice%"=="Y" (
    start target\surefire-reports\
)

pause 