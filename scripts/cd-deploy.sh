#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend-elementplus"
LOG_DIR="$ROOT_DIR/.run-logs"
DEPLOY_DIR="$ROOT_DIR/.deploy"
PID_FILE="$DEPLOY_DIR/backend.pid"
LOCK_FILE="$DEPLOY_DIR/deploy.lock"
FRONTEND_LOG="$LOG_DIR/frontend-build.log"
BACKEND_BUILD_LOG="$LOG_DIR/backend-build.log"
BACKEND_RUN_LOG="$LOG_DIR/backend.log"
PORT="${PORT:-8080}"
HEALTH_PATH="${HEALTH_PATH:-/}"

mkdir -p "$LOG_DIR" "$DEPLOY_DIR"

exec 9>"$LOCK_FILE"
if ! flock -n 9; then
  echo "❌ 已有部署任务在执行中，请稍后重试"
  exit 1
fi

stop_old() {
  if [[ -f "$PID_FILE" ]]; then
    local old_pid
    old_pid="$(cat "$PID_FILE")"
    if kill -0 "$old_pid" 2>/dev/null; then
      echo "停止旧服务 PID: $old_pid"
      kill "$old_pid" || true
      for _ in {1..20}; do
        if ! kill -0 "$old_pid" 2>/dev/null; then
          break
        fi
        sleep 1
      done
      if kill -0 "$old_pid" 2>/dev/null; then
        echo "旧服务未在预期时间退出，执行强制终止"
        kill -9 "$old_pid" || true
      fi
    fi
    rm -f "$PID_FILE"
  fi

  # 兜底：占用端口但未记录 PID 的情况
  local pids
  pids="$(lsof -ti tcp:"$PORT" || true)"
  if [[ -n "$pids" ]]; then
    echo "检测到端口 $PORT 仍被占用，尝试清理: $pids"
    kill $pids || true
  fi
}

echo "[1/4] 构建前端（静默）..."
cd "$FRONTEND_DIR"
npm run build >"$FRONTEND_LOG" 2>&1

echo "[2/4] 打包后端（跳过测试，静默）..."
cd "$ROOT_DIR"
mvn -B clean package -DskipTests >"$BACKEND_BUILD_LOG" 2>&1

JAR_PATH="$(ls -t "$ROOT_DIR"/target/*.jar | head -n1)"
if [[ -z "$JAR_PATH" ]]; then
  echo "❌ 未找到可运行 jar，请检查 $BACKEND_BUILD_LOG"
  exit 1
fi

echo "[3/4] 停止旧服务..."
stop_old

echo "[4/4] 启动新版本..."
# 关闭锁文件描述符，避免后台 Java 进程继承后长期占锁
exec 9>&-
nohup java -jar "$JAR_PATH" >"$BACKEND_RUN_LOG" 2>&1 &
NEW_PID=$!
echo "$NEW_PID" > "$PID_FILE"

# 健康检查（最多 40 秒）
for _ in {1..40}; do
  if curl -fsS "http://127.0.0.1:${PORT}${HEALTH_PATH}" >/dev/null 2>&1; then
    echo "✅ 部署成功: http://localhost:${PORT}${HEALTH_PATH} (PID: $NEW_PID)"
    echo "日志目录: $LOG_DIR"
    exit 0
  fi
  if ! kill -0 "$NEW_PID" 2>/dev/null; then
    echo "❌ 新服务异常退出，请检查 $BACKEND_RUN_LOG"
    exit 1
  fi
  sleep 1
done

echo "❌ 健康检查超时，请检查 $BACKEND_RUN_LOG"
exit 1
