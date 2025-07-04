@echo off
echo ========================================
echo CourtLink 运动场地预约系统 - 测试验证脚本
echo ========================================
echo.

echo [1/4] 检查项目结构...
if exist src\main\java\com\example\appointment (
    echo ? 主代码目录存在
) else (
    echo ? 主代码目录不存在
    pause
    exit /b 1
)

if exist src\test\java\com\example\appointment (
    echo ? 测试代码目录存在
) else (
    echo ? 测试代码目录不存在
    pause
    exit /b 1
)

if exist pom.xml (
    echo ? Maven配置文件存在
) else (
    echo ? Maven配置文件不存在
    pause
    exit /b 1
)
echo.

echo [2/4] 检查核心文件...
set missing_files=0

if exist src\main\java\com\example\appointment\AppointmentApplication.java (
    echo ? 主应用程序类存在
) else (
    echo ? 主应用程序类不存在
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\entity\Appointment.java (
    echo ? Appointment实体类存在
) else (
    echo ? Appointment实体类不存在
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\entity\Payment.java (
    echo ? Payment实体类存在
) else (
    echo ? Payment实体类不存在
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\service\AppointmentService.java (
    echo ? AppointmentService接口存在
) else (
    echo ? AppointmentService接口不存在
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\service\impl\AppointmentServiceImpl.java (
    echo ? AppointmentServiceImpl实现类存在
) else (
    echo ? AppointmentServiceImpl实现类不存在
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\repository\AppointmentRepository.java (
    echo ? AppointmentRepository接口存在
) else (
    echo ? AppointmentRepository接口不存在
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\repository\PaymentRepository.java (
    echo ? PaymentRepository接口存在
) else (
    echo ? PaymentRepository接口不存在
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\dto\AppointmentRequest.java (
    echo ? AppointmentRequest DTO存在
) else (
    echo ? AppointmentRequest DTO不存在
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\dto\AppointmentResponse.java (
    echo ? AppointmentResponse DTO存在
) else (
    echo ? AppointmentResponse DTO不存在
    set /a missing_files+=1
)

if exist src\main\java\com\example\appointment\dto\AppointmentQuery.java (
    echo ? AppointmentQuery DTO存在
) else (
    echo ? AppointmentQuery DTO不存在
    set /a missing_files+=1
)
echo.

echo [3/4] 检查测试文件...
if exist src\test\java\com\example\appointment\AppointmentApplicationTests.java (
    echo ? 主测试类存在
) else (
    echo ? 主测试类不存在
    set /a missing_files+=1
)

if exist src\test\java\com\example\appointment\service\AppointmentServiceTest.java (
    echo ? 服务层测试类存在
) else (
    echo ? 服务层测试类不存在
    set /a missing_files+=1
)

if exist src\test\java\com\example\appointment\entity\AppointmentTest.java (
    echo ? 实体类测试存在
) else (
    echo ? 实体类测试不存在
    set /a missing_files+=1
)

if exist src\test\java\com\example\appointment\dto\AppointmentDTOTest.java (
    echo ? DTO测试类存在
) else (
    echo ? DTO测试类不存在
    set /a missing_files+=1
)

if exist src\test\java\com\example\appointment\integration\AppointmentIntegrationTest.java (
    echo ? 集成测试类存在
) else (
    echo ? 集成测试类不存在
    set /a missing_files+=1
)

if exist src\test\java\com\example\appointment\performance\AppointmentPerformanceTest.java (
    echo ? 性能测试类存在
) else (
    echo ? 性能测试类不存在
    set /a missing_files+=1
)

if exist src\test\resources\application-test.yml (
    echo ? 测试配置文件存在
) else (
    echo ? 测试配置文件不存在
    set /a missing_files+=1
)
echo.

echo [4/4] 检查配置文件...
if exist src\main\resources\application.yml (
    echo ? 主配置文件存在
) else (
    echo ? 主配置文件不存在
    set /a missing_files+=1
)

if exist .gitignore (
    echo ? Git忽略文件存在
) else (
    echo ? Git忽略文件不存在
    set /a missing_files+=1
)

if exist README.md (
    echo ? README文档存在
) else (
    echo ? README文档不存在
    set /a missing_files+=1
)
echo.

echo ========================================
echo 测试验证结果
echo ========================================
echo.

if %missing_files% equ 0 (
    echo ? 所有必需文件都存在！
    echo.
    echo 项目结构验证通过，可以进行以下操作：
    echo 1. 安装Java 11或更高版本
    echo 2. 安装Maven 3.6或更高版本
    echo 3. 运行 'mvn clean test' 执行完整测试
    echo 4. 运行 'mvn spring-boot:run' 启动应用程序
    echo.
    echo 测试覆盖范围：
    echo - 单元测试：服务层、实体层、DTO层
    echo - 集成测试：数据库操作、业务逻辑
    echo - 性能测试：并发处理、响应时间
    echo - 配置测试：环境配置、依赖注入
) else (
    echo ? 发现 %missing_files% 个缺失文件
    echo 请检查项目结构并补充缺失的文件
)

echo.
echo 按任意键继续...
pause > nul 