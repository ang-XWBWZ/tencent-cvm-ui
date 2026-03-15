---
date: 2026-02-25
title: 补充 Billing SDK 说明与接口文件
scope: [docs,backend,frontend]
---

## 背景
用户要求将“自封装 billing sdk”形成说明文档，并补充可直接调用的接口文件后 push。

## 变更内容
- 新增 `docs/billing-sdk.md`：说明真实 Billing OpenAPI 封装方式与注意事项。
- 新增 `api/tengxuncvm-api.http`：提供本地联调接口清单（凭证、资源、购买、余额、账单）。
- 同步更新 `openclaw说明.md`，将账单能力状态从 mock 更新为真实链路。

## 验证方式
- 文档文件可读且路径正确。
- 服务可通过 `/api/billing/balance`、`/api/billing/bills` 返回真实数据。

## 关联提交
待提交后补充。