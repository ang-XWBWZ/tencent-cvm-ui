# Billing 自封装 SDK 说明

本项目已将腾讯云 Billing 能力封装为内部服务：

- 入口类：`src/main/java/com/example/tengxuncvm/service/BillingService.java`
- 对外接口：
  - `GET /api/billing/balance`：查询账户余额
  - `GET /api/billing/bills?page=1&size=10&billingCycle=YYYY-MM`：查询账单摘要
  - `GET /api/billing/bills/{billId}`：查询账单详情（当前基于摘要列表映射）

## 设计要点

1. 使用 `CommonClient` 对 Billing OpenAPI 做统一调用，不依赖缺失的 billing sdk jar。
2. 账单查询兼容参数差异：使用 `BeginTime/EndTime`，避免 `Month/Limit` 不兼容导致报错。
3. 错误统一转换为可读提示：
   - `TENCENT_CLOUD_ERROR`
   - `OPERATION_FAILED`
4. 金额单位统一：腾讯部分字段返回“分”，后端转换为“元”后返回前端。

## 注意事项

- 必须先配置有效凭证（`/api/credentials`）。
- 账号需有 Billing 相关权限。
- 若账单列表为空，可能是当前账期无消费数据。