#!/bin/bash

# =====================================================
# push_to_github.sh
# 功能：初始化 Git 仓库并推送到 GitHub（支持 SSH）
# =====================================================

echo "=== 智能图书管理系统 Git 推送脚本 ==="

# 1. 检查是否已经是 Git 仓库
if [ -d ".git" ]; then
    echo "已检测到 Git 仓库"
else
    echo "未检测到 Git 仓库，正在初始化..."
    git init
    echo "Git 仓库初始化完成"
fi

# 2. 显示当前状态
echo "当前 Git 状态："
git status

# 3. 询问 SSH 仓库地址
read -p "请输入 GitHub 仓库 SSH 地址 (例如：git@github.com:用户名/仓库.git): " REPO_URL

# 4. 检查是否已有远程 origin
if git remote | grep origin > /dev/null; then
    echo "已存在远程仓库 origin，正在更新 URL..."
    git remote set-url origin $REPO_URL
else
    echo "添加远程仓库 origin..."
    git remote add origin $REPO_URL
fi

# 5. 添加所有文件
echo "正在添加所有文件..."
git add .

# 6. 提交
read -p "请输入本次提交说明: " COMMIT_MSG
git commit -m "$COMMIT_MSG"

# 7. 自动检测当前分支
CURRENT_BRANCH=$(git branch --show-current)
if [ -z "$CURRENT_BRANCH" ]; then
    # 如果没有分支（第一次提交），默认用 main
    git branch -M main
    CURRENT_BRANCH="main"
fi
echo "当前分支：$CURRENT_BRANCH"

# 8. 推送到远程仓库
echo "正在推送到远程仓库..."
git push -u origin $CURRENT_BRANCH

# 9. 检查推送状态
if [ $? -eq 0 ]; then
    echo "✅ 推送成功！"
else
    echo "❌ 推送失败，请检查 SSH 地址或远程仓库权限"
fi

# 10. 显示最终状态
echo "最终 Git 状态："
git status

echo "脚本执行完成"