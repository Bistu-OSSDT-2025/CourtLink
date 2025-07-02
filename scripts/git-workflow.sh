#!/bin/bash

# 预约管理与支付集成系统 - Git工作流程脚本
# 作者: Your Name
# 版本: 1.0.0

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
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

# 检查Git仓库
check_git_repo() {
    if [ ! -d ".git" ]; then
        log_error "当前目录不是Git仓库"
        exit 1
    fi
}

# 检查工作区是否干净
check_working_directory() {
    if [ -n "$(git status --porcelain)" ]; then
        log_warning "工作区有未提交的更改"
        read -p "是否继续? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
}

# 创建开发分支
create_develop_branch() {
    log_info "创建开发分支..."
    
    # 检查develop分支是否存在
    if git show-ref --verify --quiet refs/heads/develop; then
        log_warning "develop分支已存在"
        git checkout develop
        git pull origin develop
    else
        git checkout -b develop
        git push -u origin develop
    fi
    
    log_success "已切换到develop分支"
}

# 创建特性分支
create_feature_branch() {
    local feature_name=$1
    
    if [ -z "$feature_name" ]; then
        log_error "请提供特性分支名称"
        echo "用法: $0 feature <feature-name>"
        exit 1
    fi
    
    log_info "创建特性分支: feat/$feature_name"
    
    # 确保在develop分支
    git checkout develop
    git pull origin develop
    
    # 创建特性分支
    git checkout -b "feat/$feature_name"
    log_success "已创建特性分支: feat/$feature_name"
}

# 创建修复分支
create_fix_branch() {
    local fix_name=$1
    
    if [ -z "$fix_name" ]; then
        log_error "请提供修复分支名称"
        echo "用法: $0 fix <fix-name>"
        exit 1
    fi
    
    log_info "创建修复分支: fix/$fix_name"
    
    # 确保在develop分支
    git checkout develop
    git pull origin develop
    
    # 创建修复分支
    git checkout -b "fix/$fix_name"
    log_success "已创建修复分支: fix/$fix_name"
}

# 合并特性分支
merge_feature_branch() {
    local feature_name=$1
    
    if [ -z "$feature_name" ]; then
        log_error "请提供特性分支名称"
        echo "用法: $0 merge-feature <feature-name>"
        exit 1
    fi
    
    log_info "合并特性分支: feat/$feature_name"
    
    # 切换到develop分支
    git checkout develop
    git pull origin develop
    
    # 合并特性分支
    git merge "feat/$feature_name" --no-ff -m "feat: 合并特性分支 $feature_name"
    
    # 推送到远程
    git push origin develop
    
    # 删除本地特性分支
    git branch -d "feat/$feature_name"
    
    # 删除远程特性分支
    git push origin --delete "feat/$feature_name" || true
    
    log_success "已合并特性分支: feat/$feature_name"
}

# 合并修复分支
merge_fix_branch() {
    local fix_name=$1
    
    if [ -z "$fix_name" ]; then
        log_error "请提供修复分支名称"
        echo "用法: $0 merge-fix <fix-name>"
        exit 1
    fi
    
    log_info "合并修复分支: fix/$fix_name"
    
    # 切换到develop分支
    git checkout develop
    git pull origin develop
    
    # 合并修复分支
    git merge "fix/$fix_name" --no-ff -m "fix: 合并修复分支 $fix_name"
    
    # 推送到远程
    git push origin develop
    
    # 删除本地修复分支
    git branch -d "fix/$fix_name"
    
    # 删除远程修复分支
    git push origin --delete "fix/$fix_name" || true
    
    log_success "已合并修复分支: fix/$fix_name"
}

# 创建发布分支
create_release_branch() {
    local version=$1
    
    if [ -z "$version" ]; then
        log_error "请提供版本号"
        echo "用法: $0 release <version>"
        exit 1
    fi
    
    log_info "创建发布分支: release/$version"
    
    # 确保在develop分支
    git checkout develop
    git pull origin develop
    
    # 创建发布分支
    git checkout -b "release/$version"
    git push -u origin "release/$version"
    
    log_success "已创建发布分支: release/$version"
}

# 完成发布
finish_release() {
    local version=$1
    
    if [ -z "$version" ]; then
        log_error "请提供版本号"
        echo "用法: $0 finish-release <version>"
        exit 1
    fi
    
    log_info "完成发布: $version"
    
    # 切换到main分支
    git checkout main
    git pull origin main
    
    # 合并发布分支
    git merge "release/$version" --no-ff -m "release: 发布版本 $version"
    
    # 创建标签
    git tag -a "v$version" -m "Release version $version"
    
    # 推送到远程
    git push origin main
    git push origin "v$version"
    
    # 切换回develop分支
    git checkout develop
    git pull origin develop
    
    # 合并发布分支到develop
    git merge "release/$version" --no-ff -m "release: 合并发布版本 $version 到develop"
    git push origin develop
    
    # 删除发布分支
    git branch -d "release/$version"
    git push origin --delete "release/$version" || true
    
    log_success "已完成发布: $version"
}

# 运行测试
run_tests() {
    log_info "运行测试..."
    
    # 运行单元测试
    mvn test
    
    # 运行代码检查
    mvn checkstyle:check
    
    log_success "测试完成"
}

# 提交代码
commit_code() {
    local commit_type=$1
    local message=$2
    
    if [ -z "$commit_type" ] || [ -z "$message" ]; then
        log_error "请提供提交类型和消息"
        echo "用法: $0 commit <type> <message>"
        echo "类型: feat, fix, docs, style, refactor, test, chore"
        exit 1
    fi
    
    log_info "提交代码: $commit_type: $message"
    
    # 添加所有文件
    git add .
    
    # 提交
    git commit -m "$commit_type: $message"
    
    log_success "已提交代码"
}

# 推送代码
push_code() {
    log_info "推送代码..."
    
    git push origin HEAD
    
    log_success "已推送代码"
}

# 显示帮助信息
show_help() {
    echo "预约管理与支付集成系统 - Git工作流程脚本"
    echo ""
    echo "用法: $0 <command> [options]"
    echo ""
    echo "命令:"
    echo "  init                   初始化项目，创建develop分支"
    echo "  feature <name>         创建特性分支"
    echo "  fix <name>             创建修复分支"
    echo "  merge-feature <name>   合并特性分支到develop"
    echo "  merge-fix <name>       合并修复分支到develop"
    echo "  release <version>      创建发布分支"
    echo "  finish-release <version> 完成发布"
    echo "  test                   运行测试"
    echo "  commit <type> <message> 提交代码"
    echo "  push                   推送代码"
    echo "  help                   显示帮助信息"
    echo ""
    echo "提交类型:"
    echo "  feat     新功能"
    echo "  fix      修复bug"
    echo "  docs     文档更新"
    echo "  style    代码格式化"
    echo "  refactor 代码重构"
    echo "  test     测试相关"
    echo "  chore    构建/工具相关"
    echo ""
    echo "示例:"
    echo "  $0 init"
    echo "  $0 feature appointment-management"
    echo "  $0 commit feat \"添加预约管理功能\""
    echo "  $0 merge-feature appointment-management"
    echo "  $0 release 1.0.0"
}

# 主函数
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
            log_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@" 