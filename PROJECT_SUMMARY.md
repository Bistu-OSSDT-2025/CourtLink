# ԤԼ������֧������ϵͳ - ��Ŀ�ܽ�

## ? ��Ŀ����

����Ŀ�ѳɹ����ӵ�����GitHubԶ�ֿ̲⣺`git@github.com:Bistu-OSSDT-2025/CourtLink.git`

### ��Ŀ�ṹ
```
CourtLink/
������ src/main/java/com/example/appointment/
��   ������ AppointmentApplication.java          # ��������
��   ������ entity/
��   ��   ������ Appointment.java                # ԤԼʵ��
��   ��   ������ Payment.java                    # ֧��ʵ��
��   ������ repository/
��   ��   ������ AppointmentRepository.java      # ԤԼ���ݷ��ʲ�
��   ��   ������ PaymentRepository.java          # ֧�����ݷ��ʲ�
��   ������ service/
��       ������ AppointmentService.java         # ԤԼ����ӿ�
��       ������ PaymentService.java             # ֧������ӿ�
������ src/main/resources/
��   ������ application.yml                     # �������ļ�
������ docs/
��   ������ DEVELOPMENT_GUIDE.md               # ����ָ��
��   ������ TASK_CHECKLIST.md                  # �����嵥
������ scripts/
��   ������ git-workflow.sh                    # Git�������̽ű�
������ pom.xml                                # Maven����
������ README.md                              # ��Ŀ˵��
������ QUICK_START.md                         # ���ٿ�ʼָ��
������ start-dev.bat                          # �������������ű�
������ check-status.bat                       # ��Ŀ״̬���ű�
������ .gitignore                             # Git�����ļ�
```

## ? ����ɵĹ���

### 1. ��Ŀ��ʼ�� ?
- [x] ����������Spring Boot��Ŀ�ṹ
- [x] ����Maven��������
- [x] ����Ӧ�������ļ�
- [x] ������������

### 2. ����ʵ����� ?
- [x] **ԤԼʵ�� (Appointment)**
  - �û�ID�������ṩ��ID
  - �������͡�ԤԼʱ��
  - ԤԼ״̬����
  - ����ע��Ϣ
- [x] **֧��ʵ�� (Payment)**
  - ֧�������š�����ԤԼID
  - ֧����ʽ��֧��״̬
  - ����ID���˿���Ϣ

### 3. ���ݷ��ʲ� ?
- [x] **ԤԼRepository**
  - ����CRUD����
  - ʱ���ͻ����ѯ
  - ״̬��ѯ��ͳ�Ʋ�ѯ
- [x] **֧��Repository**
  - ֧����������
  - ״̬��ѯ��ͳ�Ʋ�ѯ
  - ��ʱ�������Ի���

### 4. ҵ���߼���ӿ� ?
- [x] **ԤԼ����ӿ�**
  - ���������¡�ȡ��ԤԼ
  - ʱ���ͻ���
  - ״̬����֪ͨ����
- [x] **֧������ӿ�**
  - ֧�������ص�����
  - �˿��״̬����
  - ͳ�Ʋ�ѯ����֤�߼�

### 5. �������ߺͽű� ?
- [x] **Git�������̽ű�**
  - ��֧�����Զ���
  - �����ύ�淶��
  - ���������Զ���
- [x] **��Ŀ�����ű�**
  - �������
  - �Զ���������
- [x] **״̬���ű�**
  - Git״̬���
  - �������

### 6. �ĵ���ϵ ?
- [x] **README.md** - ��Ŀ����˵��
- [x] **QUICK_START.md** - ���ٿ�ʼָ��
- [x] **DEVELOPMENT_GUIDE.md** - ��ϸ����ָ��
- [x] **TASK_CHECKLIST.md** - ���������嵥

## ? ��ǰ״̬

### Git�ֿ�״̬
- **Զ�ֿ̲�**: `git@github.com:Bistu-OSSDT-2025/CourtLink.git`
- **����֧**: `main` (������)
- **������֧**: `develop` (��ǰ��֧��������)
- **������**: �ɾ�����δ�ύ����

### ��֧����
```
main (������֧)
������ develop (��������֧) �� ��ǰ��֧
������ feat/* (���Է�֧)
������ fix/* (�޸���֧)
������ release/* (������֧)
```

## ? ��һ�������ƻ�

### ��һ�ܣ����Ĺ���ʵ��
1. **ʵ��Service��**
   - AppointmentServiceImpl
   - PaymentServiceImpl
   - ��ӵ�Ԫ����

2. **����Controller��**
   - AppointmentController
   - PaymentController
   - ���API�ĵ�

3. **ʵ�ֺ���ҵ���߼�**
   - ʱ���ͻ����㷨
   - ֧��״̬��
   - �쳣�������

### �ڶ��ܣ����ɺͲ���
1. **���ݿ⼯��**
   - �������ݿ��
   - ������ݳ�ʼ���ű�
   - ���ɲ���

2. **�������Ϣ����**
   - Redis��������
   - RabbitMQ��Ϣ����
   - �����Ż�

3. **֪ͨϵͳ**
   - �ʼ�֪ͨ
   - ����֪ͨ
   - ��ʱ����

### �����ܣ����ƺ��Ż�
1. **��ȫ����**
   - Spring Security
   - JWT��֤
   - API����

2. **��غ���־**
   - Ӧ�ü��
   - ��־����
   - �澯����

3. **��������**
   - Docker������
   - CI/CD����
   - ������������

## ?? ��������ʹ��

### ���ٿ�ʼ
```bash
# 1. �����Ŀ״̬
./check-status.bat

# 2. ������������
./start-dev.bat

# 3. �������Է�֧
./scripts/git-workflow.sh feature feature-name

# 4. �ύ����
./scripts/git-workflow.sh commit feat "��������"

# 5. �ϲ���֧
./scripts/git-workflow.sh merge-feature feature-name
```

### ��������
```bash
# ������Ŀ
mvn clean compile

# ���в���
mvn test

# ����Ӧ��
mvn spring-boot:run

# �鿴API�ĵ�
# ����: http://localhost:8080/api/swagger-ui.html
```

## ? ����ջ

### ��˼���
- **���**: Spring Boot 2.7.x
- **���ݿ�**: MySQL 8.0
- **����**: Redis 6.0
- **��Ϣ����**: RabbitMQ 3.8
- **API�ĵ�**: Swagger/OpenAPI 3
- **����**: JUnit 5 + Mockito
- **����**: Maven 3.6+

### �����淶
- **����淶**: Google Java Style Guide
- **�ύ�淶**: Conventional Commits
- **��֧����**: Git Flow
- **���Ը�����**: > 80%

## ? ���Ĺ���ģ��

### ԤԼ����ģ�� (C)
- ? ʵ�����
- ? ���ݷ��ʲ�
- ? ����ӿ�
- ? ҵ���߼�ʵ��
- ? ��������
- ? ʱ���ͻ���
- ? ״̬����
- ? ֪ͨϵͳ

### ֧������ģ�� (D)
- ? ʵ�����
- ? ���ݷ��ʲ�
- ? ����ӿ�
- ? ҵ���߼�ʵ��
- ? ��������
- ? ֧������
- ? �ص�����
- ? �˿��

## ? ��ϵ��ʽ

�����������Ҫ�������룺
1. �鿴 [QUICK_START.md](QUICK_START.md) ���ٿ�ʼָ��
2. ���� [DEVELOPMENT_GUIDE.md](docs/DEVELOPMENT_GUIDE.md) ��ϸ�����ĵ�
3. �鿴 [TASK_CHECKLIST.md](docs/TASK_CHECKLIST.md) �����嵥
4. �ύIssue��GitHub�ֿ�

---

**��Ŀ�ѳɹ���ʼ�������ӵ�Զ�ֿ̲⣬���Կ�ʼ�����ˣ�** ? 