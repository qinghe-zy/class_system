#!/bin/bash

# =====================================================
# push_to_github.sh
# 功能：初始化 Git 仓库并推送到 GitHub（统一推送到 main）
# 适用：Git Bash / Linux / macOS
# =====================================================

set -u

echo "=== 项目 Git 推送脚本（统一 main 分支）==="

# ---------- 工具检查 ----------
if ! command -v git >/dev/null 2>&1; then
    echo "❌ 未检测到 git，请先安装 Git。"
    exit 1
fi

# ---------- 初始化仓库 ----------
if [ -d ".git" ]; then
    echo "✅ 已检测到 Git 仓库"
else
    echo "未检测到 Git 仓库，正在初始化..."
    git init || {
        echo "❌ Git 初始化失败"
        exit 1
    }
    echo "✅ Git 仓库初始化完成"
fi

# ---------- 显示当前状态 ----------
echo
echo "========== 当前 Git 状态 =========="
git status
echo "=================================="
echo

# ---------- 输入远程仓库 ----------
read -r -p "请输入 GitHub 仓库 SSH 地址（例如：git@github.com:用户名/仓库.git）: " REPO_URL

if [ -z "${REPO_URL}" ]; then
    echo "❌ 仓库地址不能为空"
    exit 1
fi

# ---------- 设置远程 origin ----------
if git remote | grep -q "^origin$"; then
    echo "检测到已存在远程 origin，正在更新 URL..."
    git remote set-url origin "${REPO_URL}" || {
        echo "❌ 更新远程仓库地址失败"
        exit 1
    }
else
    echo "正在添加远程仓库 origin..."
    git remote add origin "${REPO_URL}" || {
        echo "❌ 添加远程仓库失败"
        exit 1
    }
fi

echo "当前远程仓库："
git remote -v
echo

# ---------- 统一主分支为 main ----------
# 若已有提交，直接重命名分支
if git rev-parse --verify HEAD >/dev/null 2>&1; then
    git branch -M main || {
        echo "❌ 切换/重命名到 main 失败"
        exit 1
    }
else
    # 无提交时，直接把 HEAD 指向 main
    git symbolic-ref HEAD refs/heads/main || {
        echo "❌ 设置默认分支为 main 失败"
        exit 1
    }
fi

CURRENT_BRANCH="main"
echo "✅ 当前分支已统一为：${CURRENT_BRANCH}"
echo

# ---------- 添加文件 ----------
echo "正在添加所有文件..."
git add -A || {
    echo "❌ git add 失败"
    exit 1
}

# ---------- 判断是否需要提交 ----------
NEED_COMMIT=1
if git diff --cached --quiet; then
    NEED_COMMIT=0
fi

if [ "${NEED_COMMIT}" -eq 1 ]; then
    read -r -p "请输入本次提交说明: " COMMIT_MSG
    if [ -z "${COMMIT_MSG}" ]; then
        COMMIT_MSG="chore: update project"
    fi

    git commit -m "${COMMIT_MSG}" || {
        echo "❌ 提交失败"
        exit 1
    }
    echo "✅ 提交完成"
else
    echo "ℹ️ 没有检测到可提交的变更，跳过 commit"
fi

echo
echo "========== 提交后状态 =========="
git status
echo "================================"
echo

# ---------- 检查远程 main 是否存在 ----------
echo "正在检查远程 main 分支..."
git fetch origin >/dev/null 2>&1

if git ls-remote --exit-code --heads origin main >/dev/null 2>&1; then
    echo "⚠️ 远程仓库已存在 main 分支"
    echo "请选择推送方式："
    echo "1) 合并远程 main 后推送（推荐）"
    echo "2) 强制覆盖远程 main（危险）"
    echo "3) 取消操作"
    read -r -p "请输入选项 [默认 1]: " PUSH_MODE

    if [ -z "${PUSH_MODE}" ]; then
        PUSH_MODE="1"
    fi

    case "${PUSH_MODE}" in
        1)
            echo "正在拉取并合并远程 main..."
            git pull origin main --allow-unrelated-histories --no-rebase || {
                echo "❌ 合并远程 main 失败，可能存在冲突，请手动解决后再推送"
                exit 1
            }

            echo "正在推送到远程 main..."
            git push -u origin main || {
                echo "❌ 推送失败"
                exit 1
            }
            ;;
        2)
            echo "⚠️ 即将强制覆盖远程 main"
            read -r -p "确认继续？输入 YES 才会执行: " CONFIRM_FORCE
            if [ "${CONFIRM_FORCE}" != "YES" ]; then
                echo "已取消强制推送"
                exit 0
            fi

            echo "正在强制推送到远程 main..."
            git push -u origin main --force-with-lease || {
                echo "❌ 强制推送失败"
                exit 1
            }
            ;;
        3)
            echo "已取消操作"
            exit 0
            ;;
        *)
            echo "❌ 无效选项，脚本终止"
            exit 1
            ;;
    esac
else
    echo "✅ 远程仓库不存在 main 分支，正在首次推送..."
    git push -u origin main || {
        echo "❌ 推送失败，请检查 SSH 地址、仓库权限或网络"
        exit 1
    }
fi

# ---------- 最终状态 ----------
echo
echo "========== 最终 Git 状态 =========="
git status
echo "=================================="
echo
echo "✅ 脚本执行完成，项目已处理为 main 分支推送逻辑"