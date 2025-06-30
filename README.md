# ԤԼ������֧������ϵͳ

## ��Ŀ����

����Ŀ��һ������Spring Boot��ԤԼ�����֧������ϵͳ����Ҫ�������º���ģ�飺

- **ԤԼ����ģ�� (C)**: ����ԤԼ��ȡ��ԤԼ���鿴ԤԼ��ʱ���ͻ��顢״̬����֪ͨϵͳ
- **֧������ģ�� (D)**: ģ��֧��������״̬���¡�֧���ص����˿��

## ����ջ

- **��˿��**: Spring Boot 2.7.x
- **���ݿ�**: MySQL 8.0
- **����**: Redis
- **��Ϣ����**: RabbitMQ
- **API�ĵ�**: Swagger/OpenAPI 3
- **���Կ��**: JUnit 5 + Mockito
- **��������**: Maven
- **����淶**: Google Java Style Guide

## ��Ŀ�ṹ

```
appointment-payment-system/
������ src/
��   ������ main/
��   ��   ������ java/com/example/appointment/
��   ��   ��   ������ AppointmentApplication.java
��   ��   ��   ������ config/
��   ��   ��   ������ controller/
��   ��   ��   ������ service/
��   ��   ��   ������ repository/
��   ��   ��   ������ entity/
��   ��   ��   ������ dto/
��   ��   ��   ������ exception/
��   ��   ��   ������ util/
��   ��   ������ resources/
��   ��       ������ application.yml
��   ��       ������ application-dev.yml
��   ��       ������ application-prod.yml
��   ������ test/
��       ������ java/com/example/appointment/
������ docs/
������ scripts/
������ pom.xml
```

## ��������

### ��֧����
- `main`: ����֧��������������
- `develop`: ��������֧
- `feat/*`: ���Է�֧���� `feat/appointment-management`
- `fix/*`: �޸���֧
- `release/*`: ������֧

### �ύ�淶
- `feat`: �¹���
- `fix`: �޸�bug
- `docs`: �ĵ�����
- `style`: �����ʽ��
- `refactor`: �����ع�
- `test`: �������
- `chore`: ����/�������

## ���ٿ�ʼ

1. ��¡��Ŀ
```bash
git clone <repository-url>
cd appointment-payment-system
```

2. �������л���������֧
```bash
git checkout -b develop
```

3. ��װ����
```bash
mvn clean install
```

4. ������Ŀ
```bash
mvn spring-boot:run
```

## API�ĵ�

������Ŀ�����: http://localhost:8080/swagger-ui.html

## ����

```bash
# �������в���
mvn test

# �����ض�����
mvn test -Dtest=AppointmentServiceTest
``` 