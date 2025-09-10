#!/bin/bash

# OpenMRS Core Development Stop Script
# Stops the development environment

set -e

echo "🛑 Stopping OpenMRS Core Development Environment"
echo "==============================================="

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

# Check if Docker is running
if ! docker info &> /dev/null; then
    print_error "Docker is not running."
    exit 1
fi

print_status "Stopping all development services..."

# Stop and remove containers
docker compose down

if [ $? -eq 0 ]; then
    print_success "Development environment stopped successfully! 🎉"
    echo ""
    echo "Data is preserved in Docker volumes for next startup."
    echo ""
    echo "To completely reset (including database):"
    echo "  ${YELLOW}docker compose down -v${NC}"
    echo ""
    echo "To start again:"
    echo "  ${YELLOW}./dev-start.sh${NC}"
else
    print_error "Failed to stop development environment"
    exit 1
fi