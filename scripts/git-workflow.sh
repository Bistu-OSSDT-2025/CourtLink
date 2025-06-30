#!/bin/bash

# ԤԼ������֧������ϵͳ - Git�������̽ű�
# ����: Your Name
# �汾: 1.0.0

set -e

# ��ɫ����
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ��־����
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# ���Git�ֿ�
check_git_repo() {
    if [ ! -d ".git" ]; then
        log_error "��ǰĿ¼����Git�ֿ�"
        exit 1
    fi
}

# ��鹤�����Ƿ�ɾ�
check_working_directory() {
    if [ -n "$(git status --porcelain)" ]; then
        log_warning "��������δ�ύ�ĸ���"
        read -p "�Ƿ����? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
}

# ����������֧
create_develop_branch() {
    log_info "����������֧..."
    
    # ���develop��֧�Ƿ����
    if git show-ref --verify --quiet refs/heads/develop; then
        log_warning "develop��֧�Ѵ���"
        git checkout develop
        git pull origin develop
    else
        git checkout -b develop
        git push -u origin develop
    fi
    
    log_success "���л���develop��֧"
}

# �������Է�֧
create_feature_branch() {
    local feature_name=$1
    
    if [ -z "$feature_name" ]; then
        log_error "���ṩ���Է�֧����"
        echo "�÷�: $0 feature <feature-name>"
        exit 1
    fi
    
    log_info "�������Է�֧: feat/$feature_name"
    
    # ȷ����develop��֧
    git checkout develop
    git pull origin develop
    
    # �������Է�֧
    git checkout -b "feat/$feature_name"
    log_success "�Ѵ������Է�֧: feat/$feature_name"
}

# �����޸���֧
create_fix_branch() {
    local fix_name=$1
    
    if [ -z "$fix_name" ]; then
        log_error "���ṩ�޸���֧����"
        echo "�÷�: $0 fix <fix-name>"
        exit 1
    fi
    
    log_info "�����޸���֧: fix/$fix_name"
    
    # ȷ����develop��֧
    git checkout develop
    git pull origin develop
    
    # �����޸���֧
    git checkout -b "fix/$fix_name"
    log_success "�Ѵ����޸���֧: fix/$fix_name"
}

# �ϲ����Է�֧
merge_feature_branch() {
    local feature_name=$1
    
    if [ -z "$feature_name" ]; then
        log_error "���ṩ���Է�֧����"
        echo "�÷�: $0 merge-feature <feature-name>"
        exit 1
    fi
    
    log_info "�ϲ����Է�֧: feat/$feature_name"
    
    # �л���develop��֧
    git checkout develop
    git pull origin develop
    
    # �ϲ����Է�֧
    git merge "feat/$feature_name" --no-ff -m "feat: �ϲ����Է�֧ $feature_name"
    
    # ���͵�Զ��
    git push origin develop
    
    # ɾ���������Է�֧
    git branch -d "feat/$feature_name"
    
    # ɾ��Զ�����Է�֧
    git push origin --delete "feat/$feature_name" || true
    
    log_success "�Ѻϲ����Է�֧: feat/$feature_name"
}

# �ϲ��޸���֧
merge_fix_branch() {
    local fix_name=$1
    
    if [ -z "$fix_name" ]; then
        log_error "���ṩ�޸���֧����"
        echo "�÷�: $0 merge-fix <fix-name>"
        exit 1
    fi
    
    log_info "�ϲ��޸���֧: fix/$fix_name"
    
    # �л���develop��֧
    git checkout develop
    git pull origin develop
    
    # �ϲ��޸���֧
    git merge "fix/$fix_name" --no-ff -m "fix: �ϲ��޸���֧ $fix_name"
    
    # ���͵�Զ��
    git push origin develop
    
    # ɾ�������޸���֧
    git branch -d "fix/$fix_name"
    
    # ɾ��Զ���޸���֧
    git push origin --delete "fix/$fix_name" || true
    
    log_success "�Ѻϲ��޸���֧: fix/$fix_name"
}

# ����������֧
create_release_branch() {
    local version=$1
    
    if [ -z "$version" ]; then
        log_error "���ṩ�汾��"
        echo "�÷�: $0 release <version>"
        exit 1
    fi
    
    log_info "����������֧: release/$version"
    
    # ȷ����develop��֧
    git checkout develop
    git pull origin develop
    
    # ����������֧
    git checkout -b "release/$version"
    git push -u origin "release/$version"
    
    log_success "�Ѵ���������֧: release/$version"
}

# ��ɷ���
finish_release() {
    local version=$1
    
    if [ -z "$version" ]; then
        log_error "���ṩ�汾��"
        echo "�÷�: $0 finish-release <version>"
        exit 1
    fi
    
    log_info "��ɷ���: $version"
    
    # �л���main��֧
    git checkout main
    git pull origin main
    
    # �ϲ�������֧
    git merge "release/$version" --no-ff -m "release: �����汾 $version"
    
    # ������ǩ
    git tag -a "v$version" -m "Release version $version"
    
    # ���͵�Զ��
    git push origin main
    git push origin "v$version"
    
    # �л���develop��֧
    git checkout develop
    git pull origin develop
    
    # �ϲ�������֧��develop
    git merge "release/$version" --no-ff -m "release: �ϲ������汾 $version ��develop"
    git push origin develop
    
    # ɾ��������֧
    git branch -d "release/$version"
    git push origin --delete "release/$version" || true
    
    log_success "����ɷ���: $version"
}

# ���в���
run_tests() {
    log_info "���в���..."
    
    # ���е�Ԫ����
    mvn test
    
    # ���д�����
    mvn checkstyle:check
    
    log_success "�������"
}

# �ύ����
commit_code() {
    local commit_type=$1
    local message=$2
    
    if [ -z "$commit_type" ] || [ -z "$message" ]; then
        log_error "���ṩ�ύ���ͺ���Ϣ"
        echo "�÷�: $0 commit <type> <message>"
        echo "����: feat, fix, docs, style, refactor, test, chore"
        exit 1
    fi
    
    log_info "�ύ����: $commit_type: $message"
    
    # ��������ļ�
    git add .
    
    # �ύ
    git commit -m "$commit_type: $message"
    
    log_success "���ύ����"
}

# ���ʹ���
push_code() {
    log_info "���ʹ���..."
    
    git push origin HEAD
    
    log_success "�����ʹ���"
}

# ��ʾ������Ϣ
show_help() {
    echo "ԤԼ������֧������ϵͳ - Git�������̽ű�"
    echo ""
    echo "�÷�: $0 <command> [options]"
    echo ""
    echo "����:"
    echo "  init                   ��ʼ����Ŀ������develop��֧"
    echo "  feature <name>         �������Է�֧"
    echo "  fix <name>             �����޸���֧"
    echo "  merge-feature <name>   �ϲ����Է�֧��develop"
    echo "  merge-fix <name>       �ϲ��޸���֧��develop"
    echo "  release <version>      ����������֧"
    echo "  finish-release <version> ��ɷ���"
    echo "  test                   ���в���"
    echo "  commit <type> <message> �ύ����"
    echo "  push                   ���ʹ���"
    echo "  help                   ��ʾ������Ϣ"
    echo ""
    echo "�ύ����:"
    echo "  feat     �¹���"
    echo "  fix      �޸�bug"
    echo "  docs     �ĵ�����"
    echo "  style    �����ʽ��"
    echo "  refactor �����ع�"
    echo "  test     �������"
    echo "  chore    ����/�������"
    echo ""
    echo "ʾ��:"
    echo "  $0 init"
    echo "  $0 feature appointment-management"
    echo "  $0 commit feat \"���ԤԼ������\""
    echo "  $0 merge-feature appointment-management"
    echo "  $0 release 1.0.0"
}

# ������
main() {
    check_git_repo
    
    case "$1" in
        "init")
            create_develop_branch
            ;;
        "feature")
            create_feature_branch "$2"
            ;;
        "fix")
            create_fix_branch "$2"
            ;;
        "merge-feature")
            merge_feature_branch "$2"
            ;;
        "merge-fix")
            merge_fix_branch "$2"
            ;;
        "release")
            create_release_branch "$2"
            ;;
        "finish-release")
            finish_release "$2"
            ;;
        "test")
            run_tests
            ;;
        "commit")
            commit_code "$2" "$3"
            ;;
        "push")
            push_code
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            log_error "δ֪����: $1"
            show_help
            exit 1
            ;;
    esac
}

# ִ��������
main "$@" 