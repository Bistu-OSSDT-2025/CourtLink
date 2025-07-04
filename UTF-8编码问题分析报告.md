# UTF-8���������������

## �������

��`feat/admin-management`��֧�У����ֶ��Java�ļ�����UTF-8����ӳ����󣬵�����Ŀ�޷��������롣

## �������

### ����ԭ��
Maven�������ڴ������������ַ���JavaԴ�ļ�ʱ������UTF-8����Ĳ���ӳ���ַ�����

### �������ʾ��
```
[ERROR] ���� UTF-8 �Ĳ���ӳ���ַ� (0xE6)
[ERROR] ���� UTF-8 �Ĳ���ӳ���ַ� (0xD4)
[ERROR] ���� UTF-8 �Ĳ���ӳ���ַ� (0xDA)
```

## ��Ӱ����ļ�

### 1. AdminLoginRequest.java
**����λ��**: 
- ��7��: `@NotBlank(message = "�û��������䲻��Ϊ��")`
- ��10��: `@NotBlank(message = "���벻��Ϊ��")`

**Ӱ��**: Bean Validation ��֤��Ϣʹ������

### 2. AdminLoginResponse.java
**����λ��**:
- ��30��: `this.message = "��¼�ɹ�";`

**Ӱ��**: ��Ӧ��ϢӲ��������

### 3. Admin.java (������)
**����λ��**:
- ��18-19��: �û�����֤��Ϣ
- ��23-24��: ������֤��Ϣ  
- ��28-29��: ������֤��Ϣ
- ��32��: ������֤��Ϣ
- ��56-58��: AdminRoleö����ʾ����
- ��69-71��: AdminStatusö����ʾ����

**Ӱ��**: ʵ����֤��ö����ʾ���ƴ���ʹ������

### 4. AdminRepository.java
**����λ��**: ��ѯ����������ע��

**Ӱ��**: ���ݷ��ʲ�ע��ʹ������

## ����ԭ�����

### ������������
1. **Maven����������**: ��Ŀ����ȱ����ȷ�ı�������
2. **Դ�ļ�����**: Դ�ļ�����ʱ�ı����ʽ��һ��
3. **IDE��������**: ��������������������ĿҪ�󲻷�

### �����ַ�ӳ�����
UTF-8�����е������ַ��ڱ���ʱ�޷���ȷӳ�䣬�����ֽ����д���
- `0xE6, 0xD4, 0xDA` - ��Ӧ�����ַ���UTF-8�ֽ�����
- `0xB8, 0xF9, 0xBE` - ���������ַ�����

## �������

### ����1: ���ʻ����� (�Ƽ�)
ʹ��Spring Boot�Ĺ��ʻ����ƣ���������Ϣ�⻯�������ļ��У�

```properties
# messages.properties
validation.username.notblank=Username cannot be empty
validation.password.notblank=Password cannot be empty
login.success=Login successful

# messages_zh_CN.properties  
validation.username.notblank=�û�������Ϊ��
validation.password.notblank=���벻��Ϊ��
login.success=��¼�ɹ�
```

### ����2: Ӣ���滻 (���ٽ��)
�����������ַ��滻ΪӢ�ģ�

```java
// �޸�ǰ
@NotBlank(message = "�û�������Ϊ��")

// �޸���  
@NotBlank(message = "Username cannot be empty")
```

### ����3: Maven��������
��`pom.xml`��ȷ����ȷ�ı������ã�

```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    <maven.compiler.encoding>UTF-8</maven.compiler.encoding>
</properties>
```

## �����޸��ƻ�

### ���ȼ�1: �ؼ��ļ��޸�
1. `AdminLoginRequest.java` - �滻��֤��ϢΪӢ��
2. `AdminLoginResponse.java` - �滻�ɹ���ϢΪӢ��
3. `Admin.java` - �滻������֤��Ϣ��ö����ʾ����ΪӢ��

### ���ȼ�2: �����ļ�
1. `AdminRepository.java` - �滻����ע��ΪӢ��
2. �������ܴ������ĵ�Admin����ļ�

### ���ȼ�3: �����Ż�
1. ʵʩ���ʻ�����
2. ��������淶
3. IDEͳһ��������

## ��֤����

�޸���ִ������������֤��
```bash
# 1. ���������±���
mvnw clean compile

# 2. ���в���
mvnw test

# 3. ����Ӧ����֤
mvnw spring-boot:run
```

## Ԥ�ڽ��

�޸���Ӧ�ã�
- ? ������UTF-8�������
- ? Ӧ������������
- ? Admin������������
- ? CORS������ʧ

## ����

1. **ͳһ�����׼**: ��Ŀ��ͳһʹ��Ӣ��ע�ͺ���Ϣ
2. **���ʻ�֧��**: ��������ʵʩ�����Ĺ��ʻ�����
3. **�����淶**: ���������д�淶�����������Ӣ��
4. **IDE����**: ȷ�����п�����ʹ��ͳһ�ı������� 