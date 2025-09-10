#!/bin/bash

# OpenMRS Core Development Start Script
# Starts the development environment with database and OpenMRS

set -e

echo "🚀 Starting OpenMRS Core Development Environment"
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

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check if Docker is running
if ! docker info &> /dev/null; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

# Stop any existing containers
print_status "Stopping any existing containers..."
docker compose down 2>/dev/null || true

# Start the development environment
print_status "Starting development environment..."
print_status "This will start MariaDB and OpenMRS Core on port 8090"

# Start services
docker compose up -d

if [ $? -eq 0 ]; then
    print_success "Development environment started! 🎉"
    echo ""
    echo "Service Status:"
    echo "==============="
    docker compose ps
    echo ""
    echo "🌐 Access URLs:"
    echo "  OpenMRS Web Interface: ${YELLOW}http://localhost:8090/openmrs${NC}"
    echo "  REST API Base:         ${YELLOW}http://localhost:8090/openmrs/ws/rest/v1${NC}"
    echo "  Admin Panel:           ${YELLOW}http://localhost:8090/openmrs/admin${NC}"
    echo ""
    echo "🔧 Development Tools:"
    echo "  Debug Port:    ${YELLOW}localhost:8000${NC}"
    echo "  Database Port: ${YELLOW}localhost:3306${NC}"
    echo ""
    echo "📝 Useful Commands:"
    echo "  View logs:     ${YELLOW}docker compose logs -f${NC}"
    echo "  View API logs: ${YELLOW}docker compose logs -f api${NC}"
    echo "  Stop services: ${YELLOW}./dev-stop.sh${NC}"
    echo "  Restart:       ${YELLOW}docker compose restart api${NC}"
    echo ""
    print_status "Waiting for services to be healthy..."
    echo "This may take a few minutes for initial setup..."
    
    # Wait for services to be ready
    timeout=300
    counter=0
    while [ $counter -lt $timeout ]; do
        if docker compose ps | grep -q "healthy"; then
            print_success "Services are healthy and ready!"
            break
        fi
        sleep 5
        counter=$((counter + 5))
        echo -n "."
    done
    
    if [ $counter -ge $timeout ]; then
        print_warning "Services might still be starting up. Check logs with: docker compose logs -f"
    fi
    
else
    print_error "Failed to start development environment"
    echo "Check the logs with: docker compose logs"
    exit 1
fi