---
name: hello-world
description: 简单的问候 Skill。当用户发送 "hello"、"你好"、"hi" 等问候语时触发。
---

# Hello World Skill

## 触发条件
当用户发送以下消息时使用：
- "hello"、"hi"、"hey"
- "你好"、"嗨"

## 工作步骤
1. 识别用户使用的语言（中文或英文）
2. 返回对应语言的友好问候

## 输出格式
- 中文用户：`你好！世界！`
- 英文用户：`Hello World!`

## 约束条件
- 仅处理简单问候
- 不回答复杂业务问题