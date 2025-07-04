# ԤԼ������֧������ϵͳ - ���ٿ�ʼָ��

## ? 5���ӿ�������

### 1. ����׼��
ȷ������ϵͳ�Ѱ�װ���������
- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Git

### 2. ��¡��Ŀ
```bash
git clone <repository-url>
cd appointment-payment-system
```

### 3. ��ʼ��Git��֧
```bash
# ���ű�ִ��Ȩ��
chmod +x scripts/git-workflow.sh

# ��ʼ��������֧
./scripts/git-workflow.sh init
```

### 4. �������ݿ�
```sql
-- ��¼MySQL
mysql -u root -p

-- �������ݿ�
CREATE DATABASE appointment_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- �����û�����ѡ��
CREATE USER 'appointment_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON appointment_system.* TO 'appointment_user'@'localhost';
FLUSH PRIVILEGES;
```

### 5. �޸������ļ�
�༭ `src/main/resources/application.yml`���޸����ݿ�������Ϣ��
```yaml
spring:
  datasource:
    username: your_username
    password: your_password
```

### 6. ������Ŀ
```bash
# ������Ŀ
mvn clean compile

# ������Ŀ
mvn spring-boot:run
```

### 7. ��֤����
�������µ�ַ��֤��Ŀ�Ƿ������ɹ���
- API�ĵ�: http://localhost:8080/api/swagger-ui.html
- �������: http://localhost:8080/api/actuator/health

## ? ������������

### ��ʼ�¹��ܿ���
```bash
# 1. �������Է�֧
./scripts/git-workflow.sh feature appointment-management

# 2. ��������...

# 3. ���в���
./scripts/git-workflow.sh test

# 4. �ύ����
./scripts/git-workflow.sh commit feat "���ԤԼ������"

# 5. ���ʹ���
./scripts/git-workflow.sh push

# 6. �ϲ���develop��֧
./scripts/git-workflow.sh merge-feature appointment-management
```

### �޸�Bug
```bash
# 1. �����޸���֧
./scripts/git-workflow.sh fix time-conflict-bug

# 2. �޸�����...

# 3. �ύ�޸�
./scripts/git-workflow.sh commit fix "�޸�ʱ���ͻ���bug"

# 4. ���Ͳ��ϲ�
./scripts/git-workflow.sh push
./scripts/git-workflow.sh merge-fix time-conflict-bug
```

## ? ���Ĺ��ܿ���

### ԤԼ����ģ�� (C)

#### 1. ����ԤԼ
```java
// ����ԤԼ����
Appointment appointment = new Appointment();
appointment.setUserId("user123");
appointment.setProviderId("provider456");
appointment.setServiceType("��ѯ");
appointment.setStartTime(LocalDateTime.now().plusHours(1));
appointment.setEndTime(LocalDateTime.now().plusHours(2));
appointment.setAmount(new BigDecimal("100.00"));

// ����ԤԼ
Appointment saved = appointmentService.createAppointment(appointment);
```

#### 2. ���ʱ���ͻ
```java
// ����Ƿ����ʱ���ͻ
boolean hasConflict = appointmentService.hasTimeConflict(
    "provider456", 
    startTime, 
    endTime, 
    null
);
```

#### 3. ȡ��ԤԼ
```java
// ȡ��ԤԼ
Appointment cancelled = appointmentService.cancelAppointment(
    appointmentId, 
    "�û�����ȡ��"
);
```

### ֧������ģ�� (D)

#### 1. ����֧������
```java
// ����֧������
Payment payment = new Payment();
payment.setPaymentNo("PAY" + System.currentTimeMillis());
payment.setAppointmentId("appointment123");
payment.setUserId("user123");
payment.setAmount(new BigDecimal("100.00"));
payment.setPaymentMethod(Payment.PaymentMethod.MOCK);

// ����֧������
Payment saved = paymentService.createPayment(payment);
```

#### 2. ����֧��
```java
// ����ģ��֧��
Payment processed = paymentService.processMockPayment(paymentNo);
```

#### 3. �˿��
```java
// �����˿�
Payment refunded = paymentService.processRefund(
    paymentNo, 
    new BigDecimal("100.00"), 
    "�û������˿�"
);
```

## ? ����ָ��

### ���е�Ԫ����
```bash
# �������в���
mvn test

# �����ض�������
mvn test -Dtest=AppointmentServiceTest

# �����ض����Է���
mvn test -Dtest=AppointmentServiceTest#shouldCreateAppointment
```

### ���м��ɲ���
```bash
# ���м��ɲ���
mvn verify
```

### ���븲���ʼ��
```bash
# ���ɸ����ʱ���
mvn jacoco:report

# �鿴�����ʱ���
open target/site/jacoco/index.html
```

## ? ��غ���־

### �鿴Ӧ����־
```bash
# �鿴ʵʱ��־
tail -f logs/appointment-system.log

# �鿴������־
grep ERROR logs/appointment-system.log
```

### ���ָ��
- Ӧ������: http://localhost:8080/api/actuator/metrics
- ����״̬: http://localhost:8080/api/actuator/health
- Ӧ����Ϣ: http://localhost:8080/api/actuator/info

## ? ��������

### Q: ���ݿ�����ʧ��
**A:** ������¼��㣺
1. MySQL�����Ƿ�����
2. ���ݿ����Ӳ����Ƿ���ȷ
3. ���ݿ��û�Ȩ���Ƿ��㹻

### Q: Redis����ʧ��
**A:** ������¼��㣺
1. Redis�����Ƿ�����
2. Redis���Ӳ����Ƿ���ȷ
3. Redis���������Ƿ���ȷ

### Q: ��Ŀ����ʧ��
**A:** ������¼��㣺
1. JDK�汾�Ƿ�Ϊ11+
2. Maven�Ƿ���ȷ��װ
3. �����ļ��Ƿ����﷨����

### Q: ����ʧ��
**A:** ������¼��㣺
1. �������ݿ��Ƿ���ȷ����
2. ���������Ƿ���ȷ׼��
3. ���Ի����Ƿ�����

## ? ������Դ

- [����ָ��](docs/DEVELOPMENT_GUIDE.md) - ��ϸ�Ŀ����ĵ�
- [�����嵥](docs/TASK_CHECKLIST.md) - �����Ŀ��������б�
- [API�ĵ�](http://localhost:8080/api/swagger-ui.html) - ����API�ĵ�

## ? ��ȡ����

�������ʹ�ù������������⣺

1. �鿴 [��������](#��������) ����
2. ���� [����ָ��](docs/DEVELOPMENT_GUIDE.md)
3. �ύ Issue ����Ŀ�ֿ�
4. ��ϵ�����Ŷ�

---

**ף��������죡** ? 