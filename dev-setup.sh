#!/bin/bash

# OpenMRS Core Local Development Setup Script
# This script sets up the local development environment for OpenMRS Core

set -e

echo "🚀 OpenMRS Core Local Development Setup"
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
print_status "Checking prerequisites..."

# Check Docker
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

# Check Docker Compose
if ! command -v docker compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

print_success "Docker and Docker Compose are available"

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n1 | awk -F '"' '{print $2}')
    print_success "Java version: $JAVA_VERSION"
else
    print_warning "Java not found in PATH, but that's OK for Docker-based development"
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn --version | head -n1)
    print_success "Maven available: $MVN_VERSION"
else
    print_warning "Maven not found in PATH, but that's OK for Docker-based development"
fi

print_status "Creating development environment..."

# Create .env file for Docker Compose if it doesn't exist
if [ ! -f .env ]; then
    print_status "Creating .env file for local development..."
    cat > .env << EOF
# OpenMRS Development Configuration
OMRS_DB_NAME=openmrs_dev
OMRS_DB_USER=openmrs
OMRS_DB_PASSWORD=openmrs
OMRS_DB_ROOT_PASSWORD=openmrs

# Admin credentials
OMRS_ADMIN_USER_PASSWORD=Admin123
OMRS_ADMIN_PASSWORD_LOCKED=false

# Development settings
OMRS_CREATE_TABLES=true
OMRS_BUILD=true
OMRS_DEV_DEBUG_PORT=8000

# Use development image
TAG=dev
EOF
    print_success "Created .env file with development settings"
else
    print_warning ".env file already exists, skipping creation"
fi

# Create data directory for persistence
print_status "Setting up data directories..."
mkdir -p target/data
mkdir -p target/modules

print_success "Data directories created"

print_status "Setup complete! 🎉"
echo ""
echo "Next steps:"
echo "==========="
echo "1. Build the development image:"
echo "   ${YELLOW}./dev-build.sh${NC}"
echo ""
echo "2. Start the development environment:"
echo "   ${YELLOW}./dev-start.sh${NC}"
echo ""
echo "3. Access OpenMRS at:"
echo "   ${YELLOW}http://localhost:8090/openmrs${NC}"
echo ""
echo "4. For REST API endpoints:"
echo "   ${YELLOW}http://localhost:8090/openmrs/ws/rest/v1${NC}"
echo ""
echo "5. Stop the environment:"
echo "   ${YELLOW}./dev-stop.sh${NC}"