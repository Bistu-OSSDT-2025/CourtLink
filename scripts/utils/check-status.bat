@echo off
echo ========================================
echo ԤԼ������֧������ϵͳ - ��Ŀ״̬���
echo ========================================
echo.

REM ���Git״̬
echo [1/4] ���Git״̬...
git status --porcelain
if %errorlevel% equ 0 (
    echo Git������״̬����
) else (
    echo ����: Git��������δ�ύ�ĸ���
)
echo.

REM ��鵱ǰ��֧
echo [2/4] ��鵱ǰ��֧...
git branch --show-current
echo.

REM ���Զ�ֿ̲�����
echo [3/4] ���Զ�ֿ̲�����...
git remote -v
echo.

REM �����Ŀ����
echo [4/4] �����Ŀ����...
mvn dependency:tree -DoutputFile=dependency-tree.txt >nul 2>&1
if %errorlevel% equ 0 (
    echo ��Ŀ���������ɣ�������鿴 dependency-tree.txt
) else (
    echo ����: ��Ŀ�������ʧ��
)
echo.

echo ========================================
echo �����ɣ�
echo ========================================
echo.
echo ��һ����������:
echo 1. ���Git�������и��ģ����ύ����
echo 2. ȷ������ȷ�ķ�֧�Ͽ���
echo 3. ���� start-dev.bat ������������
echo 4. �鿴 QUICK_START.md �˽���ϸʹ��˵��
echo.

pause 