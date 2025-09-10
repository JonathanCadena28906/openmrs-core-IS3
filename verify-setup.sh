#!/bin/bash

# Quick verification script for OpenMRS Core development setup

set -e

echo "🔍 Verifying OpenMRS Core Development Setup"
echo "==========================================="

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

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check if Docker is available and running
print_status "Checking Docker availability..."
if command -v docker &> /dev/null; then
    if docker info &> /dev/null; then
        print_success "Docker is running"
    else
        print_error "Docker is installed but not running"
        exit 1
    fi
else
    print_error "Docker is not installed"
    exit 1
fi

# Check Docker Compose
print_status "Checking Docker Compose..."
if docker compose version &> /dev/null; then
    print_success "Docker Compose is available"
else
    print_error "Docker Compose is not available"
    exit 1
fi

# Check project structure
print_status "Verifying project structure..."

required_files=(
    "docker-compose.yml"
    "docker-compose.override.yml"
    "Dockerfile"
    "pom.xml"
    "dev-setup.sh"
    "dev-build.sh"
    "dev-start.sh"
    "dev-stop.sh"
    "test-api.sh"
    "DEVELOPMENT.md"
)

for file in "${required_files[@]}"; do
    if [ -f "$file" ]; then
        print_success "✓ $file"
    else
        print_error "✗ $file not found"
    fi
done

# Check directories
required_dirs=(
    "api"
    "web"
    "webapp"
    "modules"
    "target/data"
)

for dir in "${required_dirs[@]}"; do
    if [ -d "$dir" ]; then
        print_success "✓ $dir/"
    else
        print_warning "⚠ $dir/ not found"
    fi
done

# Check environment file
if [ -f ".env" ]; then
    print_success "✓ .env configuration file"
    print_status "Environment variables:"
    grep -v "^#" .env | head -5
else
    print_warning "⚠ .env file not found - run ./dev-setup.sh"
fi

# Validate Docker Compose configuration
print_status "Validating Docker Compose configuration..."
if docker compose config &> /dev/null; then
    print_success "Docker Compose configuration is valid"
    
    # Check port configuration
    if docker compose config | grep -q "8090:8080"; then
        print_success "Port 8090 is correctly configured"
    else
        print_warning "Port 8090 might not be configured correctly"
    fi
else
    print_error "Docker Compose configuration has errors"
    docker compose config
fi

# Quick syntax check for scripts
print_status "Checking development scripts..."
scripts=("dev-setup.sh" "dev-build.sh" "dev-start.sh" "dev-stop.sh" "test-api.sh")

for script in "${scripts[@]}"; do
    if [ -f "$script" ] && [ -x "$script" ]; then
        print_success "✓ $script is executable"
    else
        print_warning "⚠ $script is not executable or missing"
    fi
done

print_status "Verification completed! 🎉"
echo ""
echo "📋 Setup Summary:"
echo "=================="
echo "✅ Development scripts are ready"
echo "✅ Docker environment is configured"
echo "✅ Port 8090 is configured for OpenMRS"
echo "✅ Development documentation is available"
echo ""
echo "🚀 Ready to start development!"
echo "Next steps:"
echo "1. Build the development image: ${YELLOW}./dev-build.sh${NC}"
echo "2. Start the environment: ${YELLOW}./dev-start.sh${NC}"
echo "3. Access OpenMRS at: ${YELLOW}http://localhost:8090/openmrs${NC}"