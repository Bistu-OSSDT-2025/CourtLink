# ԤԼ������֧������ϵͳ - ����ָ��

## ��Ŀ����

����Ŀ��һ������Spring Boot��ԤԼ�����֧������ϵͳ����Ҫ�������º���ģ�飺

### ԤԼ����ģ�� (C)
- **���Ĺ���**: ����ԤԼ��ȡ��ԤԼ���鿴ԤԼ
- **����Ҫ��**:
  - ԤԼʱ���ͻ���
  - ԤԼ״̬����
  - ԤԼ֪ͨ
  - ��ʱ������

### ֧������ģ�� (D)
- **���Ĺ���**: ģ��֧��������״̬����
- **����Ҫ��**:
  - ֧���������
  - ����״̬����
  - ֧���ص�����
  - �˿��

## ����ջ

- **��˿��**: Spring Boot 2.7.x
- **���ݿ�**: MySQL 8.0
- **����**: Redis
- **��Ϣ����**: RabbitMQ
- **API�ĵ�**: Swagger/OpenAPI 3
- **���Կ��**: JUnit 5 + Mockito
- **��������**: Maven
- **����淶**: Google Java Style Guide

## ���������

### 1. ����Ҫ��
- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+

### 2. ���ݿ��ʼ��
```sql
-- �������ݿ�
CREATE DATABASE appointment_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- �����û�����ѡ��
CREATE USER 'appointment_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON appointment_system.* TO 'appointment_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. �����ļ�����
���� `src/main/resources/application.yml` �����ݱ��ػ����޸����ã�

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/appointment_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
  
  rabbitmq:
    host: localhost
    port: 5672
    username: your_rabbitmq_username
    password: your_rabbitmq_password
```

## ��֧�������

### ��֧����
- `main`: ����֧��������������
- `develop`: ��������֧
- `feat/*`: ���Է�֧���� `feat/appointment-management`
- `fix/*`: �޸���֧
- `release/*`: ������֧

### ��������

#### 1. ��ʼ����Ŀ
```bash
# ��¡��Ŀ
git clone <repository-url>
cd appointment-payment-system

# ��ʼ��������֧
./scripts/git-workflow.sh init
```

#### 2. �����¹���
```bash
# �������Է�֧
./scripts/git-workflow.sh feature appointment-management

# ������ɺ��ύ����
./scripts/git-workflow.sh commit feat "���ԤԼ������"

# ���ʹ���
./scripts/git-workflow.sh push

# �ϲ���develop��֧
./scripts/git-workflow.sh merge-feature appointment-management
```

#### 3. �޸�Bug
```bash
# �����޸���֧
./scripts/git-workflow.sh fix time-conflict-bug

# �޸���ɺ��ύ����
./scripts/git-workflow.sh commit fix "�޸�ʱ���ͻ���bug"

# ���ʹ���
./scripts/git-workflow.sh push

# �ϲ���develop��֧
./scripts/git-workflow.sh merge-fix time-conflict-bug
```

#### 4. �����汾
```bash
# ����������֧
./scripts/git-workflow.sh release 1.0.0

# �ڷ�����֧�Ͻ������ղ��Ժ��޸�

# ��ɷ���
./scripts/git-workflow.sh finish-release 1.0.0
```

## ����淶

### 1. �ύ�淶
ʹ��Լ��ʽ�ύ�淶��

- `feat`: �¹���
- `fix`: �޸�bug
- `docs`: �ĵ�����
- `style`: �����ʽ��
- `refactor`: �����ع�
- `test`: �������
- `chore`: ����/�������

ʾ����
```bash
git commit -m "feat: ���ԤԼʱ���ͻ��鹦��"
git commit -m "fix: �޸�֧���ص������еĿ�ָ���쳣"
git commit -m "docs: ����API�ĵ�"
```

### 2. ������
��ѭGoogle Java Style Guide��

```bash
# ���д�����
mvn checkstyle:check

# ��ʽ������
mvn checkstyle:format
```

### 3. �����淶
- **����**: ʹ��PascalCase���� `AppointmentService`
- **������**: ʹ��camelCase���� `createAppointment`
- **������**: ʹ��camelCase���� `appointmentId`
- **������**: ʹ��UPPER_SNAKE_CASE���� `MAX_RETRY_COUNT`
- **����**: ʹ��Сд���� `com.example.appointment`

## ��������

### 1. ���ܿ�������

#### ����1: �������
- ��ȷ��������
- ���API�ӿ�
- ��������ģ��

#### ����2: ������֧
```bash
./scripts/git-workflow.sh feature feature-name
```

#### ����3: ����ʵ��
- ����ʵ���� (Entity)
- �������ݷ��ʲ� (Repository)
- ����ҵ���߼��� (Service)
- ������������ (Controller)
- ��д��Ԫ����

#### ����4: ������֤
```bash
# ���е�Ԫ����
mvn test

# ���м��ɲ���
mvn verify

# ���д�����
mvn checkstyle:check
```

#### ����5: �ύ����
```bash
./scripts/git-workflow.sh commit feat "��������"
./scripts/git-workflow.sh push
```

#### ����6: �ϲ���֧
```bash
./scripts/git-workflow.sh merge-feature feature-name
```

### 2. ���Բ���

#### ��Ԫ����
- ʹ��JUnit 5��д����
- ʹ��Mockito����Mock
- ���Ը�����Ҫ�� > 80%

ʾ����
```java
@Test
@DisplayName("����ԤԼʱӦ�ü��ʱ���ͻ")
void shouldCheckTimeConflictWhenCreatingAppointment() {
    // Given
    Appointment appointment = createTestAppointment();
    when(appointmentRepository.findConflictingAppointments(any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());
    
    // When
    Appointment result = appointmentService.createAppointment(appointment);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(AppointmentStatus.PENDING);
    verify(appointmentRepository).save(appointment);
}
```

#### ���ɲ���
- �������ݿ⽻��
- ����API�ӿ�
- �����ⲿ���񼯳�

#### �˵��˲���
- ��������ҵ������
- �����û�����

### 3. �ĵ�ά��

#### API�ĵ�
ʹ��Swaggerע��ά��API�ĵ���

```java
@RestController
@RequestMapping("/api/appointments")
@Tag(name = "ԤԼ����", description = "ԤԼ���API")
public class AppointmentController {

    @PostMapping
    @Operation(summary = "����ԤԼ", description = "�����µ�ԤԼ��¼")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "�����ɹ�"),
        @ApiResponse(responseCode = "400", description = "�����������"),
        @ApiResponse(responseCode = "409", description = "ʱ���ͻ")
    })
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody Appointment appointment) {
        // ʵ�ִ���
    }
}
```

#### ����ע��
- ��ͷ���������JavaDocע��
- �����߼���Ҫ����ע��
- ʹ������ע��

## ����ָ��

### 1. ��������
```bash
# ������Ŀ
mvn spring-boot:run

# ����API�ĵ�
http://localhost:8080/api/swagger-ui.html
```

### 2. ���Ի���
```bash
# ���
mvn clean package -Dmaven.test.skip=true

# ����
java -jar target/appointment-payment-system-1.0.0.jar --spring.profiles.active=test
```

### 3. ��������
```bash
# ���
mvn clean package -Dmaven.test.skip=true -Pprod

# ����
java -jar target/appointment-payment-system-1.0.0.jar --spring.profiles.active=prod
```

## ��غ���־

### 1. ��־����
- ʹ��SLF4J + Logback
- ��������ļ��洢
- ���ڹ鵵������

### 2. ���ָ��
- Ӧ�����ܼ�� (APM)
- ���ݿ����ӳؼ��
- ���������ʼ��
- ֧���ɹ��ʼ��

### 3. �澯����
- �쳣�澯
- ���ܸ澯
- ҵ��澯

## ��������

### 1. ���ݿ���������
- ������ݿ�����Ƿ�����
- ��֤���Ӳ����Ƿ���ȷ
- ȷ�����ݿ��û�Ȩ��

### 2. Redis��������
- ���Redis�����Ƿ�����
- ��֤���Ӳ����Ƿ���ȷ
- ȷ��Redis��������

### 3. RabbitMQ��������
- ���RabbitMQ�����Ƿ�����
- ��֤���Ӳ����Ƿ���ȷ
- ȷ�϶��кͽ���������

## ��ϵ��ʽ

�������⣬����ϵ�����Ŷӻ��ύIssue��

---

**ע��**: ��ָ�ϻ�������Ŀ��չ�������£��붨�ڲ鿴���°汾�� 