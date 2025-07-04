# ��˿�����Ա - �����ƻ�

## ??? ������Ա��Ϣ
- **��ɫ**: ��˿�����ԤԼ&֧��ģ�飩
- **�����֧**: `feat/booking-management`, `feat/payment-integration`
- **����ģ��**: ԤԼ����ģ��(C) + ֧������ģ��(D)

## ? ����ְ��

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

## ? ��ϸ��������

### ��һ�ܣ�ԤԼ����ģ����Ĺ���

#### Day 1-2: ԤԼʵ���DTO���
- [ ] ����Appointmentʵ����
- [ ] ����AppointmentDTO��
- [ ] ����AppointmentRequest��
- [ ] ����AppointmentResponse��
- [ ] ����AppointmentQuery��
- [ ] ���������֤ע��

#### Day 3-4: ԤԼ����ʵ��
- [ ] ʵ��AppointmentServiceImpl��
  - [ ] createAppointment����������ʱ���ͻ��飩
  - [ ] updateAppointment����
  - [ ] cancelAppointment����
  - [ ] confirmAppointment����
  - [ ] completeAppointment����
  - [ ] getAppointmentById����
  - [ ] getAppointmentsByUserId����
  - [ ] getAppointmentsByProviderId����
  - [ ] getAppointmentsByStatus����
  - [ ] hasTimeConflict����
  - [ ] getAppointmentsByTimeRange����
  - [ ] getUpcomingAppointments����
  - [ ] cleanupExpiredAppointments����
  - [ ] sendAppointmentReminder����
  - [ ] sendAppointmentNotification����

#### Day 5: ԤԼ������ʵ��
- [ ] ʵ��AppointmentController��
  - [ ] POST /api/appointments - ����ԤԼ
  - [ ] PUT /api/appointments/{id} - ����ԤԼ
  - [ ] DELETE /api/appointments/{id} - ȡ��ԤԼ
  - [ ] PUT /api/appointments/{id}/confirm - ȷ��ԤԼ
  - [ ] PUT /api/appointments/{id}/complete - ���ԤԼ
  - [ ] GET /api/appointments/{id} - ��ȡԤԼ����
  - [ ] GET /api/appointments/user/{userId} - ��ȡ�û�ԤԼ�б�
  - [ ] GET /api/appointments/provider/{providerId} - ��ȡ�����ṩ��ԤԼ�б�
  - [ ] GET /api/appointments/status/{status} - ����״̬��ȡԤԼ�б�
  - [ ] GET /api/appointments/conflict-check - ���ʱ���ͻ
  - [ ] GET /api/appointments/statistics - ��ȡԤԼͳ��

### �ڶ��ܣ�֧������ģ����Ĺ���

#### Day 1-2: ֧��ʵ���DTO���
- [ ] ����Paymentʵ����
- [ ] ����PaymentDTO��
- [ ] ����PaymentRequest��
- [ ] ����PaymentResponse��
- [ ] ����PaymentCallback��
- [ ] ����RefundRequest��
- [ ] ���������֤ע��

#### Day 3-4: ֧������ʵ��
- [ ] ʵ��PaymentServiceImpl��
  - [ ] createPayment����
  - [ ] processPayment����
  - [ ] processMockPayment����
  - [ ] handlePaymentCallback����
  - [ ] processRefund����
  - [ ] cancelPayment����
  - [ ] getPaymentByPaymentNo����
  - [ ] getPaymentsByUserId����
  - [ ] getPaymentsByStatus����
  - [ ] getPaymentsByPaymentMethod����
  - [ ] getPaymentsByTimeRange����
  - [ ] processTimeoutPayments����
  - [ ] retryFailedPayment����
  - [ ] sumAmountByUserIdAndStatus����
  - [ ] calculateSuccessRate����
  - [ ] generatePaymentNo����
  - [ ] validatePayment����
  - [ ] sendPaymentNotification����

#### Day 5: ֧��������ʵ��
- [ ] ʵ��PaymentController��
  - [ ] POST /api/payments - ����֧������
  - [ ] POST /api/payments/{paymentNo}/process - ����֧��
  - [ ] POST /api/payments/{paymentNo}/mock - ģ��֧��
  - [ ] POST /api/payments/callback - ֧���ص�
  - [ ] POST /api/payments/{paymentNo}/refund - �˿��
  - [ ] DELETE /api/payments/{paymentNo} - ȡ��֧��
  - [ ] GET /api/payments/{paymentNo} - ��ȡ֧������
  - [ ] GET /api/payments/user/{userId} - ��ȡ�û�֧���б�
  - [ ] GET /api/payments/status/{status} - ����״̬��ȡ֧���б�
  - [ ] GET /api/payments/method/{method} - ����֧����ʽ��ȡ֧���б�
  - [ ] GET /api/payments/statistics - ��ȡ֧��ͳ��

### �����ܣ��߼����ܺͼ���

#### Day 1-2: ʱ���ͻ����㷨
- [ ] ʵ�ָ�Ч��ʱ���ͻ����㷨
- [ ] ���ʱ�仺��������
- [ ] ʵ�ֳ�ͻԤԼ��ѯ�Ż�
- [ ] ��ӳ�ͻ��鵥Ԫ����

#### Day 3-4: ֧��״̬��������
- [ ] ʵ��֧��״̬��
- [ ] ���֧����ʱ����
- [ ] ʵ��֧�����Ի���
- [ ] ���֧����֤�߼�

#### Day 5: ֪ͨϵͳ����
- [ ] ����NotificationService�ӿ�
- [ ] ʵ��EmailNotificationService
- [ ] ʵ��SmsNotificationService
- [ ] ʵ��WebhookNotificationService
- [ ] ���֪ͨģ��

### �����ܣ���ʱ������Ż�

#### Day 1-2: ��ʱ����ʵ��
- [ ] ����AppointmentScheduler��
- [ ] ʵ���������ԤԼ����
- [ ] ʵ�ַ�����������
- [ ] ʵ��״̬��������
- [ ] ��Ӷ�ʱ��������

#### Day 3-4: ����������Ż�
- [ ] ʵ��Redis��������
- [ ] ��ӻ���ע��
- [ ] ʵ�ֻ�����²���
- [ ] ��ӻ��浥Ԫ����

#### Day 5: �쳣�������־
- [ ] �����Զ����쳣��
- [ ] ����ȫ���쳣������
- [ ] ����쳣��־��¼
- [ ] ʵ���쳣֪ͨ����

## ?? �������ߺ�����

### ��֧����
```bash
# �л���ԤԼ�����֧
git checkout feat/booking-management

# �л���֧�����ɷ�֧
git checkout feat/payment-integration

# �����µĹ��ܷ�֧
git checkout -b feat/booking-conflict-check

# ���ͷ�֧��Զ��
git push -u origin feat/booking-management
```

### ��������
```bash
# 1. ��ȡ���´���
git checkout develop
git pull origin develop

# 2. �������ܷ�֧
git checkout -b feat/feature-name

# 3. ��������...

# 4. ���в���
mvn test

# 5. �����ʽ��
mvn spotless:apply

# 6. �ύ����
git add .
git commit -m "feat(booking): ʵ��ԤԼʱ���ͻ��鹦��"

# 7. ���ͷ�֧
git push origin feat/feature-name

# 8. ����Pull Request
# ��GitHub�ϴ���PR����feat/feature-name��develop
```

### ��������
```bash
# �������в���
mvn test

# �����ض�������
mvn test -Dtest=AppointmentServiceTest

# �����ض����Է���
mvn test -Dtest=AppointmentServiceTest#shouldCheckTimeConflict

# ���ɲ��Ը����ʱ���
mvn jacoco:report

# �鿴�����ʱ���
open target/site/jacoco/index.html
```

### �����������
```bash
# �����ʽ��
mvn spotless:apply

# ������
mvn checkstyle:check

# ������Ŀ
mvn clean compile

# �����Ŀ
mvn clean package
```

## ? �����ύ�淶

### �ύ��ʽ
```
<type>(<scope>): <subject>

type��feat | fix | docs | style | refactor | test | chore
scope��booking | payment | user | court | admin | ci | deps | *
subject��һ�仰���������������� 50 ��
```

### �ύʾ��
```bash
# ԤԼ����ģ��
git commit -m "feat(booking): ʵ��ԤԼ��������"
git commit -m "feat(booking): ���ʱ���ͻ����㷨"
git commit -m "fix(booking): �޸�ԤԼ״̬����bug"
git commit -m "test(booking): ���ԤԼ����Ԫ����"

# ֧������ģ��
git commit -m "feat(payment): ʵ��֧����������"
git commit -m "feat(payment): ���֧��״̬��"
git commit -m "fix(payment): �޸�֧���ص�����"
git commit -m "test(payment): ���֧���������"
```

## ? ��Ԫ����Ҫ��

### ���Ը�����Ҫ��
- **���帲����**: �� 80%
- **Service�㸲����**: �� 90%
- **Controller�㸲����**: �� 70%
- **Repository�㸲����**: �� 85%

### ����ʾ��
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

## ? ����Ҫ��

### API��Ӧʱ��
- **��ѯ�ӿ�**: < 200ms
- **����/���½ӿ�**: < 500ms
- **����ҵ��ӿ�**: < 1000ms

### ����Ҫ��
- **���ӿڲ���**: > 100 QPS
- **ϵͳ���岢��**: > 1000 �û�

### ���ݿ��Ż�
- [ ] ��ӱ�Ҫ�����ݿ�����
- [ ] �Ż����Ӳ�ѯ
- [ ] ʵ�ַ�ҳ��ѯ
- [ ] ��Ӳ�ѯ����

## ? �������Ҫ��

### ���������Ŀ
- [ ] �����Ƿ����Google Java Style Guide
- [ ] �Ƿ�����˱�Ҫ��Swaggerע��
- [ ] �Ƿ�����˵�Ԫ����
- [ ] �Ƿ�����˱�Ҫ����־��¼
- [ ] �Ƿ������쳣���
- [ ] �Ƿ����˲�����ȫ
- [ ] �Ƿ�����˱�Ҫ��ע��

### �����������
```bash
# �������м��
mvn clean verify

# �����븲����
mvn jacoco:check

# ��������
mvn checkstyle:check
```

## ? ���ȸ���

### ÿ������
- [ ] �����������
- [ ] �ύ���뵽��Ӧ��֧
- [ ] ���е�Ԫ����
- [ ] ���¿�����־

### ÿ�ܻع�
- [ ] ����������ָ��
- [ ] ���²��Ը�����
- [ ] �ع˿�������
- [ ] �ƻ���������

## ? ����ͷ���

### ��������
- ��֧: `feat/booking-management`, `feat/payment-integration`
- �Զ�����: ���͵���֧ʱ�Զ����𵽿�������

### ���Ի���
- ��֧: `develop`
- �Զ�����: PR�ϲ���developʱ�Զ����𵽲��Ի���

### ��������
- ��֧: `main`
- �ֶ�����: ��main��֧�ֶ�������������

---

**ע��**: ���ƻ��������Ŀ��չ������仯���е������붨�ڲ鿴���°汾�� 