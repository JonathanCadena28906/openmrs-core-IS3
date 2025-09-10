#!/bin/bash

# OpenMRS Core Development Build Script
# Builds the development Docker image with optimizations for fast iteration

set -e

echo "🔨 Building OpenMRS Core Development Image"
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

# Check if Docker is running
if ! docker info &> /dev/null; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

print_status "Building development image (this may take a while on first run)..."

# Build with optimized arguments for development
docker compose build \
    --build-arg MVN_ARGS='clean install -DskipTests -T 1C' \
    --progress=plain

if [ $? -eq 0 ]; then
    print_success "Development image built successfully! 🎉"
    echo ""
    echo "Image details:"
    docker images | grep openmrs-core | head -3
    echo ""
    echo "Next step: Run ${YELLOW}./dev-start.sh${NC} to start the development environment"
else
    print_error "Build failed. Check the output above for errors."
    exit 1
fi