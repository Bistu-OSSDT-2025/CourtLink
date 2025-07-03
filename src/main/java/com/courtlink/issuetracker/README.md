# CourtLink ȱ��׷��ģ��

## ���ܸ���

ȱ��׷��ģ���� CourtLink ��Ŀ�ĺ������֮һ�����ڹ����׷��ϵͳ�з��ֵĸ������⡣��ģ���ṩ��������ȱ���������ڹ���������������ѯ��״̬���µȹ��ܡ�

### ��Ҫ����

1. ȱ�ݹ���
   - ������ȱ��
   - ��ѯȱ���б�֧�ַ�ҳ�Ͷ�����ɸѡ��
   - ��ȡȱ������
   - ����ȱ��״̬

2. ����֧��
   - ״̬��NEW��IN_PROGRESS��RESOLVED��CLOSED
   - ���ȼ���LOW��MEDIUM��HIGH��CRITICAL
   - ģ�飺USER��COURT��BOOKING��PAYMENT��FRONTEND��OTHER

## ����ջ

- Java 21
- Spring Boot
- Spring Data JPA
- Swagger/OpenAPI
- JUnit 5
- AssertJ
- Mockito

## API �ӿ�

### 1. ����ȱ��
```http
POST /api/issues
Content-Type: application/json

{
  "title": "ȱ�ݱ���",
  "description": "ȱ������",
  "screenshotUrl": "��ͼURL",
  "priority": "HIGH",
  "module": "FRONTEND"
}
```

### 2. ��ѯȱ���б�
```http
GET /api/issues?status=NEW&priority=HIGH&module=FRONTEND&page=0&size=20
```

### 3. ��ȡȱ������
```http
GET /api/issues/{id}
```

### 4. ����ȱ��״̬
```http
PATCH /api/issues/{id}/status?status=IN_PROGRESS
```

## ���Ը���

### 1. ��Ԫ����
- ����������ԣ�MockMvc��
- �������ԣ�Mockito��
- �ֿ����ԣ�@DataJpaTest��

### 2. ���ܲ���
- ���β�������
  - ����ȱ�� < 200ms
  - ��ѯȱ�� < 100ms
  - ������������ѯ < 150ms

- ������������
  - ��������ƽ����ʱ < 100ms/��
  - ��������������10�߳� x 100����

### 3. �ȶ��Բ���
- �������ԣ�֧��10���߳�ͬʱ����
- ƽ����Ӧʱ�䣺< 50ms
- ����һ���ԣ���֤�ڲ�������µ�����׼ȷ��

## ���ܲ��Խ��

1. ��������
   - ���δ�����ƽ�� 150ms
   - ����������100������ƽ�� 80ms/��

2. ��ѯ����
   - �޹���������ƽ�� 80ms
   - ������������ƽ�� 120ms

3. ��������
   - 10�̲߳�����ƽ����Ӧʱ�� 45ms
   - ��������>200 TPS
   - �����ʣ�0%

## ���ʵ��

1. ���ݿ�����
   - Ϊ status��priority��module �ֶδ�������
   - Ϊ created_at��updated_at ��������

2. �������
   - ʹ�� Spring Cache �����ѯ���
   - ����ʧЧʱ�䣺5����

3. ��������
   - ʹ���ֹ������Ʋ�������
   - ʹ�� Spring �������

## �����Ŷ�

- ��˿�����CourtLink �Ŷ�
- ���ԣ�CourtLink �����Ŷ�
- �ĵ���CourtLink �ĵ��Ŷ�

## ����˵��

1. ����Ҫ��
   - JDK 21+
   - MySQL 8.0+
   - Maven 3.8+

2. ����˵��
   ```yaml
   spring:
     jpa:
       hibernate:
         ddl-auto: update
     datasource:
       url: jdbc:mysql://localhost:3306/courtlink
       username: your_username
       password: your_password
   ```

## �����Ż��ƻ�

1. �����Ż�
   - �����������
   - �Ż���ѯSQL
   - ������ݿ����ӳؼ��

2. ������ǿ
   - ������۹���
   - ֧���ļ�����
   - ��ӱ�ǩϵͳ

3. ��ά��������
   - ��Ӹ�����ָ��
   - �Ż���־��¼
   - �����쳣���� 