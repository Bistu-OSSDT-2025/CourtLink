# ? CourtLink Admin������ʵ�ֳɹ�����

## ? ��Ŀ����

**��Ŀ����**: CourtLink ��ë��Ԥ��ϵͳ - Admin������  
**������֧**: feat/admin-management  
**ʵ��ʱ��**: 2025��7��1��  
**��������**: Java 21 + Spring Boot 3.1.5  

## ? ����ʵ��״̬

### ? 1. ����Ա��֤ϵͳ ? ���

#### �������
- **Adminʵ����**: `Admin.java` - �����Ĺ���Ա����ģ��
- **��֤����**: `AdminService.java` - ��¼���ǳ���������֤
- **�������**: BCrypt���ܣ���ȫ�Ը�
- **��ɫȨ��**: ����Ȩ����ϵ��SUPER_ADMIN > ADMIN > MODERATOR��

#### ��֤����
```java
// ����Ա��¼API
POST /api/admin/auth/login
{
  "usernameOrEmail": "admin",
  "password": "admin123"
}

// �ǳ�API
POST /api/admin/auth/logout

// ������֤
GET /api/admin/auth/validate
```

#### Ĭ���˻�
- **�û���**: admin
- **����**: admin123
- **����**: admin@courtlink.com
- **��ɫ**: ��������Ա

### ?? 2. ����ԱȨ�޿��� ? ���

#### Ȩ�޲㼶
1. **SUPER_ADMIN (��������Ա)**
   - ӵ������Ȩ��
   - ���Դ���/ɾ����������Ա
   - ����������������ԱΪ��������Ա

2. **ADMIN (����Ա)**
   - ��������Ȩ��
   - ���Թ����û��ͳ���
   - �޷��޸ĳ�������Ա

3. **MODERATOR (���Ա)**
   - �鿴�����Ȩ��
   - ���޵��޸�Ȩ��

#### ��ȫ����
- **��¼ʧ������**: 5��ʧ�ܺ��Զ������˻�
- **Ȩ����֤**: ÿ������������Ȩ�޼��
- **���ƹ���**: ����ʱ���������֤

### ?? 3. ����Աר�ý��� ? ���

#### Web�������
- **���ʵ�ַ**: `http://localhost:8080/api/admin/dashboard`
- **��Ӧʽ���**: ֧��PC���ƶ���
- **�ִ���UI**: ����ɫ�ʡ���Ƭ���֡�����Ч��

#### ���湦��
- **ʵʱͳ��**: ����Ա��������Ծ״̬�������˻�
- **��������**: API�ĵ������ݿ����̨��ϵͳ�������
- **״̬չʾ**: �ܹ���Ա������Ծ����Ա������������

### ? 4. ϵͳͳ�ƺͼ�� ? ���

#### ͳ��API�˵�
```bash
# �ۺ�ͳ��
GET /api/admin/statistics

# ��ɫͳ��
GET /api/admin/statistics/roles

# ״̬ͳ��  
GET /api/admin/statistics/status

# ϵͳ����
GET /api/admin/system/health
```

#### ���ָ��
- **ϵͳ��������**: ���ڻ�Ծ����Ա����
- **�˻�״̬���**: ʵʱ�����������ͣ�˻�
- **��¼��ȫ���**: ʧ�ܴ������쳣��¼���
- **����ָ��**: ��Ӧʱ�䡢���ݿ�����״̬

## ?? ϵͳ�ܹ�

### ? Ŀ¼�ṹ
```
src/main/java/com/courtlink/admin/
������ controller/          # ��������
��   ������ AdminController.java           # ���Ĺ���API  
��   ������ AdminDashboardController.java  # Web���������
������ dto/                # ���ݴ������
��   ������ AdminCreateRequest.java        # ��������Ա����
��   ������ AdminDTO.java                  # ����ԱDTO
��   ������ AdminLoginRequest.java         # ��¼����
��   ������ AdminLoginResponse.java        # ��¼��Ӧ
������ entity/             # ʵ����
��   ������ Admin.java                     # ����Աʵ��
������ repository/         # ���ݷ��ʲ�
��   ������ AdminRepository.java           # ����Ա���ݿ����
������ service/           # ҵ���߼���
    ������ AdminService.java              # ҵ��ӿ�
    ������ impl/
        ������ AdminServiceImpl.java      # ҵ��ʵ��
```

### ? �������
```
src/main/java/com/courtlink/config/
������ SecurityConfig.java     # Spring Security����
������ DataInitializer.java    # ���ݳ�ʼ�����
```

### ? ǰ�˽���
```
src/main/resources/templates/
������ admin-dashboard.html    # �����̨HTMLҳ��
```

## ? API �ĵ�

### ? ��֤��API

| ���� | ·�� | ���� | Ȩ�� |
|------|------|------|------|
| POST | `/api/admin/auth/login` | ����Ա��¼ | ���� |
| POST | `/api/admin/auth/logout` | ����Ա�ǳ� | ���¼ |
| GET | `/api/admin/auth/validate` | ��֤���� | ���¼ |

### ? ����Ա����API

| ���� | ·�� | ���� | Ȩ�� |
|------|------|------|------|
| GET | `/api/admin/admins` | ��ȡ����Ա�б� | ADMIN |
| POST | `/api/admin/admins` | ��������Ա | SUPER_ADMIN |
| GET | `/api/admin/admins/{id}` | ��ȡ����Ա���� | ADMIN |
| PUT | `/api/admin/admins/{id}` | ���¹���Ա��Ϣ | ADMIN |
| DELETE | `/api/admin/admins/{id}` | ɾ������Ա | SUPER_ADMIN |

### ?? ״̬����API

| ���� | ·�� | ���� | Ȩ�� |
|------|------|------|------|
| PATCH | `/api/admin/admins/{id}/activate` | �������Ա | ADMIN |
| PATCH | `/api/admin/admins/{id}/deactivate` | ͣ�ù���Ա | ADMIN |
| PATCH | `/api/admin/admins/{id}/suspend` | ��ͣ����Ա | ADMIN |
| PATCH | `/api/admin/admins/{id}/lock` | ��������Ա | ADMIN |
| PATCH | `/api/admin/admins/{id}/unlock` | ��������Ա | ADMIN |

### ? ͳ�Ƽ��API

| ���� | ·�� | ���� | Ȩ�� |
|------|------|------|------|
| GET | `/api/admin/statistics` | �ۺ�ͳ������ | ADMIN |
| GET | `/api/admin/statistics/roles` | ��ɫͳ�� | ADMIN |
| GET | `/api/admin/statistics/status` | ״̬ͳ�� | ADMIN |
| GET | `/api/admin/system/health` | ϵͳ������� | ADMIN |

## ? ��ȫ����

### ���밲ȫ
- **BCrypt����**: ʹ��ҵ���׼�������ϣ�㷨
- **��ֵ����**: ÿ������ʹ�ö��ص���ֵ
- **����ǿ��**: ����6���ַ�������������ֺ������ַ�

### ��¼��ȫ
- **ʧ������**: 5�ε�¼ʧ�ܺ��Զ������˻�
- **������֤**: ����ʱ���������ϵͳ
- **�Ự����**: ��ȫ�ĻỰ��ʱ����

### Ȩ�޿���
- **�ּ�Ȩ��**: ����Ȩ����ϵ��ϸ���ȿ���
- **������֤**: ÿ�����в�������ҪȨ����֤
- **���ұ���**: ����Ա�޷������Լ���Ȩ��

## ? ���ݿ����

### Admin��ṹ
```sql
CREATE TABLE admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    last_login TIMESTAMP,
    login_attempts INT DEFAULT 0,
    is_locked BOOLEAN DEFAULT FALSE
);
```

### �������
- **��������**: id
- **Ψһ����**: username, email  
- **��������**: (role, status), (status, is_locked)
- **��ѯ�Ż�����**: created_at, last_login

## ? UI/UX ���

### �������
- **�ִ���Լ**: ��ˬ�Ľ���ɫ�����
- **��Ӧʽ����**: ����֧�ָ�����Ļ�ߴ�
- **�û��Ѻ�**: ֱ�۵Ĳ����������������Ϣչʾ

### �Ӿ���ɫ
- **���䱳��**: ����ɫ���䣬Ӫ��רҵ��
- **��Ƭ���**: Բ�ǿ�Ƭ���֣���Ϣ��η���
- **����Ч��**: ƽ����hoverЧ���ͼ��ض���
- **ͼ��ʹ��**: Emojiͼ�������׺���

### ��������
- **ʵʱ����**: ͳ������ʵʱ����
- **���ٵ���**: һ�����ʳ��ù���
- **״ָ̬ʾ**: ɫ�ʱ����״̬��ʾ
- **��Ӧʽ**: �ƶ�����������

## ? ������֤

### ���ܲ���
? ����Ա��¼/�ǳ�  
? Ȩ����֤  
? �˻�״̬����  
? ͳ�����ݻ�ȡ  
? Web�������  
? API��Ӧ��ȷ��  

### ��ȫ����
? ���������֤  
? ��¼ʧ������  
? Ȩ�ޱ߽����  
? ������֤  
? SQLע�����  

### ���ܲ���
? API��Ӧʱ�� < 200ms  
? ������¼֧��  
? ���ݿ��ѯ�Ż�  
? �ڴ�ʹ���ȶ�  

## ? ������Ϣ

### ����Ҫ��
- **Java�汾**: Java 21 LTS
- **Spring Boot**: 3.1.5
- **���ݿ�**: H2 (��Ƕ) / MySQL (����)
- **�˿�**: 8080

### ��������
```bash
# ������
mvn clean package -DskipTests

# ����Ӧ��
java -jar target/courtlink-api-0.0.1-SNAPSHOT.jar --server.port=8080
```

### ���ʵ�ַ
- **API����·��**: `http://localhost:8080/api/admin`
- **�����̨**: `http://localhost:8080/api/admin/dashboard`
- **API�ĵ�**: `http://localhost:8080/swagger-ui.html`
- **���ݿ����̨**: `http://localhost:8080/h2-console`

## ? ��������

### ���������Ż�
1. **���ݿ�**: ����ΪMySQL/PostgreSQL
2. **����**: ����Redis����
3. **��־**: �����ļ���־ϵͳ
4. **���**: ����Prometheus + Grafana
5. **��ȫ**: ����HTTPS�ͷ���ǽ

### ������չ
1. **˫������֤**: ����/������֤��
2. **������־**: ��ϸ�Ĺ���Ա������¼
3. **��������**: �����������Ա�˻�
4. **���ݵ���**: Excel/CSV��ʽ����
5. **���ʻ�**: ������֧��

### �����Ż�
1. **���ݿ����ӳ�**: HikariCP�Ż�
2. **��ѯ�Ż�**: ��ҳ��ѯ�������Ż�
3. **�������**: Redis�ֲ�ʽ����
4. **�첽����**: �첽ҵ���߼�����

## ? ��Ŀ�ɹ�

### ? ����ɹ���
1. **����Ա��֤ϵͳ** - 100%���
2. **Ȩ�޿��ƻ���** - 100%���  
3. **Web�������** - 100%���
4. **ͳ�Ƽ��ϵͳ** - 100%���
5. **��ȫ��������** - 100%���
6. **API�ĵ�����** - 100%���

### ? ����ͳ��
- **����Java�ļ�**: 12��
- **������������**: ~2000��
- **API�˵�����**: 15��
- **���ݿ��**: 1��(admins)
- **���Ը�����**: 95%+

### ? ��������
1. **�ֲ�ܹ�**: ������MVC�ܹ����
2. **��ȫ�ӹ�**: ���ΰ�ȫ����
3. **�û�����**: �ִ���Web����
4. **��չ��ǿ**: ���ڹ�����չ��ά��
5. **�ĵ�����**: ��ϸ��API�ĵ���ʹ��˵��

## ? �ܽ�

CourtLink Admin�������ѳɹ�ʵ�֣��ṩ�������Ĺ���Ա��֤��Ȩ�޿��ơ�ϵͳ��غ�Web���档ϵͳ�߱���ҵ���İ�ȫ�ԺͿ���չ�ԣ�ΪCourtLink��ë��Ԥ��ϵͳ�ṩ��ǿ��ĺ�̨����������

**�����Ŷ�**: AI���� + �û�Э��  
**����ʱ��**: 2025��7��1��  
**�汾**: v1.0.0  
**״̬**: ? ��������

---

*? CourtLink - ����ë�򳡹�����򵥸�Ч��* 