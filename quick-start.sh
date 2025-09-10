#!/bin/bash

# OpenMRS Core Quick Start Script
# One-command setup and launch for development

set -e

echo "🚀 OpenMRS Core Development - Quick Start"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to handle cleanup on exit
cleanup() {
    if [ $? -ne 0 ]; then
        print_error "Quick start failed. Check the logs above."
        echo ""
        echo "Manual steps to troubleshoot:"
        echo "1. Run: ./verify-setup.sh"
        echo "2. Check: docker compose logs"
        echo "3. Try: ./dev-setup.sh && ./dev-build.sh && ./dev-start.sh"
    fi
}

trap cleanup EXIT

print_status "Starting OpenMRS Core development environment setup..."
echo ""

# Step 1: Setup
print_status "Step 1/4: Setting up development environment..."
./dev-setup.sh
print_success "Environment setup completed"
echo ""

# Step 2: Module setup (optional, may fail due to network)
print_status "Step 2/4: Setting up REST API module..."
if ./setup-rest-module.sh; then
    print_success "REST module setup completed"
else
    print_error "REST module download failed (network issue)"
    echo "📖 Please manually download the module later using REST_MODULE_DOWNLOAD.md"
fi
echo ""

# Step 3: Build
print_status "Step 3/4: Building OpenMRS development image..."
print_status "This may take 10-15 minutes on first run..."
./dev-build.sh
print_success "Build completed"
echo ""

# Step 4: Start
print_status "Step 4/4: Starting development environment..."
./dev-start.sh
print_success "Development environment started!"
echo ""

print_success "🎉 OpenMRS Core is ready for development!"
echo ""
echo "🌐 Access your development environment:"
echo "  OpenMRS Web:    ${YELLOW}http://localhost:8090/openmrs${NC}"
echo "  REST API:       ${YELLOW}http://localhost:8090/openmrs/ws/rest/v1${NC}"
echo "  Admin Panel:    ${YELLOW}http://localhost:8090/openmrs/admin${NC}"
echo ""
echo "🔐 Default Credentials:"
echo "  Username: ${YELLOW}admin${NC}"
echo "  Password: ${YELLOW}Admin123${NC}"
echo ""
echo "🧪 Test the setup:"
echo "  ${YELLOW}./test-api.sh${NC}"
echo ""
echo "📚 Read the development guide:"
echo "  ${YELLOW}cat DEVELOPMENT.md${NC}"
echo ""
echo "🛑 To stop the environment:"
echo "  ${YELLOW}./dev-stop.sh${NC}"

# Remove the error trap since we succeeded
trap - EXIT