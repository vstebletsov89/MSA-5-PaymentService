#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "============================================="
echo "  OrchestrPay - Camunda Payment Orchestrator"
echo "============================================="

# --- 1. Stop previous ---
echo ""
echo "🧹 Stopping previous containers..."
docker compose down --remove-orphans 2>/dev/null || true

# --- 2. Build and start everything ---
echo ""
echo "🔨 Building and starting all services..."
docker compose up --build -d

# --- 3. Wait for payment-orchestrator ---
echo ""
echo "⏳ Waiting for payment-orchestrator to start..."
timeout=180
elapsed=0
while [ $elapsed -lt $timeout ]; do
    if docker compose logs payment-orchestrator 2>/dev/null | grep -q "Started PaymentOrchestratorApplication"; then
        echo "✅ Payment Orchestrator is running!"
        break
    fi
    sleep 5
    elapsed=$((elapsed + 5))
    echo "   ...waiting ($elapsed/${timeout}s)"
done

if [ $elapsed -ge $timeout ]; then
    echo "⚠️  Orchestrator may still be starting."
fi

# --- 4. Show status ---
echo ""
echo "============================================="
echo "  🎉 All services are starting!"
echo "============================================="
echo ""
echo "  Operate UI:   http://localhost:8081  (demo/demo)"
echo "  Tasklist UI:   http://localhost:8082  (demo/demo)"
echo ""
echo "  📋 Useful commands:"
echo "    docker compose logs -f payment-orchestrator   # Watch worker logs"
echo "    docker compose logs create-instance            # Check instance creation"
echo "    docker compose down                            # Stop everything"
echo ""
echo "  🚀 Create more instances:"
echo "    docker compose run --rm create-instance \"/usr/local/bin/zbctl create instance Process_Payment --host zeebe --port 26500 --insecure\""
echo "============================================="