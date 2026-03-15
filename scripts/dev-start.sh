#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$ROOT_DIR/.run-logs"
FRONTEND_DIR="$ROOT_DIR/frontend-elementplus"
PID_FILE="$LOG_DIR/backend.pid"
BACKEND_LOG="$LOG_DIR/backend.log"
FRONTEND_LOG="$LOG_DIR/frontend-build.log"

mkdir -p "$LOG_DIR"

cd "$FRONTEND_DIR"
echo "[1/2] 构建前端中（静默输出，详情见 $FRONTEND_LOG）..."
npm run build >"$FRONTEND_LOG" 2>&1

echo "[2/2] 启动后端中（日志见 $BACKEND_LOG）..."
cd "$ROOT_DIR"
nohup mvn spring-boot:run >"$BACKEND_LOG" 2>&1 &
BACKEND_PID=$!
echo "$BACKEND_PID" > "$PID_FILE"

# 等待最多 25 秒检查 8080 端口
for _ in {1..25}; do
  if ss -ltn | grep -q ':8080 '; then
    echo "✅ 后端已启动: http://localhost:8080 (PID: $BACKEND_PID)"
    exit 0
  fi
  sleep 1
done

echo "⚠️ 后端启动超时，请检查日志: $BACKEND_LOG"
exit 1
