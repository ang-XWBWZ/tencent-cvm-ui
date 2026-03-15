# Tencent CVM UI

一个面向腾讯云 CVM 的 Web 管理界面项目，提供实例购买、实例管理与账单查看等功能。

## 项目定位

这是一个正规的工程化项目，包含：

- 前端界面（Vue 3 + Element Plus）
- 后端服务（Spring Boot）
- CVM 相关接口封装
- 基础文档与变更记录

## 主要功能

- CVM 实例购买
- 地区、可用区、规格、镜像筛选
- 实例管理（开机、关机、重启、销毁、续费、修改名称）
- 账单与余额查看
- 导入 / 导出模板

## 项目截图

![Tencent CVM UI](docs/images/ui-screenshot-2026-03-15.png)

## 技术栈

- Frontend: Vue 3, Vite, Element Plus
- Backend: Spring Boot, Maven
- API: Tencent Cloud CVM / Billing related integration

## 目录结构

```text
frontend-elementplus/   前端项目
src/main/java/          后端源码
src/main/resources/     配置与静态资源
scripts/                开发/部署辅助脚本
```

## 说明

当前仓库为 GitHub 专用整理版，已移除部署环境相关隐私信息与内部敏感配置。

## License

Private / Internal Use
