@echo off
echo ========================================
echo CourtLink �˶�����ԤԼϵͳ - ������֤�ű�
echo ========================================
echo.

echo [1/4] �����Ŀ�ṹ...
if exist src\main\java\com\example\appointment (
    echo ? ������Ŀ¼����
) else (
    echo ? ������Ŀ¼������
    pause
    exit /b 1
)

if exist src\test\java\com\example\appointment (
    echo ? ���Դ���Ŀ¼����
) else (
    echo ? ���Դ���Ŀ¼������
    pause
    exit /b 1
)

if exist pom.xml (
    echo ? Maven�����ļ�����
) else (
    echo ? Maven�����ļ�������
    pause
    exit /b 1
)
echo.

echo [2/4] �������ļ�...
set missing_files=0

if exist src\main\java\com\example\appointment\AppointmentApplication.java (
    echo ? ��Ӧ�ó��������
) else (
    echo ? ��Ӧ�ó����಻����
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\entity\Appointment.java (
    echo ? Appointmentʵ�������
) else (
    echo ? Appointmentʵ���಻����
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\entity\Payment.java (
    echo ? Paymentʵ�������
) else (
    echo ? Paymentʵ���಻����
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\service\AppointmentService.java (
    echo ? AppointmentService�ӿڴ���
) else (
    echo ? AppointmentService�ӿڲ�����
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\service\impl\AppointmentServiceImpl.java (
    echo ? AppointmentServiceImplʵ�������
) else (
    echo ? AppointmentServiceImplʵ���಻����
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\repository\AppointmentRepository.java (
    echo ? AppointmentRepository�ӿڴ���
) else (
    echo ? AppointmentRepository�ӿڲ�����
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\repository\PaymentRepository.java (
    echo ? PaymentRepository�ӿڴ���
) else (
    echo ? PaymentRepository�ӿڲ�����
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\dto\AppointmentRequest.java (
    echo ? AppointmentRequest DTO����
) else (
    echo ? AppointmentRequest DTO������
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\dto\AppointmentResponse.java (
    echo ? AppointmentResponse DTO����
) else (
    echo ? AppointmentResponse DTO������
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\dto\AppointmentQuery.java (
    echo ? AppointmentQuery DTO����
) else (
    echo ? AppointmentQuery DTO������
    set /a missing_files+=1
)
echo.

echo [3/4] �������ļ�...
if exist src\test\java\com\example\appointment\AppointmentApplicationTests.java (
    echo ? �����������
) else (
    echo ? �������಻����
    set /a missing_files+=1
)

if exist src\test\java\com\example\appointment\service\AppointmentServiceTest.java (
    echo ? �������������
) else (
    echo ? ���������಻����
    set /a missing_files+=1
)

if exist src\test\java\com\example\appointment\entity\AppointmentTest.java (
    echo ? ʵ������Դ���
) else (
    echo ? ʵ������Բ�����
    set /a missing_files+=1
)

if exist src\test\java\com\example\appointment\dto\AppointmentDTOTest.java (
    echo ? DTO���������
) else (
    echo ? DTO�����಻����
    set /a missing_files+=1
)

if exist src\test\java\com\example\appointment\integration\AppointmentIntegrationTest.java (
    echo ? ���ɲ��������
) else (
    echo ? ���ɲ����಻����
    set /a missing_files+=1
)

if exist src\test\java\com\example\appointment\performance\AppointmentPerformanceTest.java (
    echo ? ���ܲ��������
) else (
    echo ? ���ܲ����಻����
    set /a missing_files+=1
)

if exist src\test\resources\application-test.yml (
    echo ? ���������ļ�����
) else (
    echo ? ���������ļ�������
    set /a missing_files+=1
)
echo.

echo [4/4] ��������ļ�...
if exist src\main\resources\application.yml (
    echo ? �������ļ�����
) else (
    echo ? �������ļ�������
    set /a missing_files+=1
)

if exist .gitignore (
    echo ? Git�����ļ�����
) else (
    echo ? Git�����ļ�������
    set /a missing_files+=1
)

if exist README.md (
    echo ? README�ĵ�����
) else (
    echo ? README�ĵ�������
    set /a missing_files+=1
)
echo.

echo ========================================
echo ������֤���
echo ========================================
echo.

if %missing_files% equ 0 (
    echo ? ���б����ļ������ڣ�
    echo.
    echo ��Ŀ�ṹ��֤ͨ�������Խ������²�����
    echo 1. ��װJava 11����߰汾
    echo 2. ��װMaven 3.6����߰汾
    echo 3. ���� 'mvn clean test' ִ����������
    echo 4. ���� 'mvn spring-boot:run' ����Ӧ�ó���
    echo.
    echo ���Ը��Ƿ�Χ��
    echo - ��Ԫ���ԣ�����㡢ʵ��㡢DTO��
    echo - ���ɲ��ԣ����ݿ������ҵ���߼�
    echo - ���ܲ��ԣ�����������Ӧʱ��
    echo - ���ò��ԣ��������á�����ע��
) else (
    echo ? ���� %missing_files% ��ȱʧ�ļ�
    echo ������Ŀ�ṹ������ȱʧ���ļ�
)

echo.
echo �����������...
pause > nul 