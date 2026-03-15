#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$ROOT_DIR/.run-logs"
DEPLOY_DIR="$ROOT_DIR/.deploy"
PID_FILE="$LOG_DIR/backend.pid"
CD_PID_FILE="$DEPLOY_DIR/backend.pid"

stop_by_pid_file() {
  local file="$1"
  if [[ ! -f "$file" ]]; then
    return 1
  fi

  local pid
  pid="$(cat "$file")"
  if kill -0 "$pid" 2>/dev/null; then
    kill "$pid"
    echo "✅ 已终止后端进程 PID: $pid"
  else
    echo "ℹ️ 进程 $pid 不存在，可能已停止"
  fi
  rm -f "$file"
  return 0
}

if stop_by_pid_file "$PID_FILE"; then
  exit 0
fi

if stop_by_pid_file "$CD_PID_FILE"; then
  exit 0
fi

echo "未找到 PID 文件，尝试按端口结束 8080..."
PIDS=$(lsof -ti tcp:8080 || true)
if [[ -n "$PIDS" ]]; then
  kill $PIDS || true
  echo "✅ 已结束 8080 端口进程: $PIDS"
else
  echo "ℹ️ 未发现运行中的后端进程"
fi
