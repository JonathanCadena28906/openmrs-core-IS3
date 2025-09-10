#!/bin/bash

# OpenMRS Core Module Setup Script
# Downloads and configures the webservices.rest module for API integration

set -e

echo "📦 Setting up OpenMRS REST API Module"
echo "====================================="

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

# Module information
WEBSERVICES_REST_VERSION="2.46.0"
WEBSERVICES_REST_URL="https://addons.openmrs.org/rest/addon/webservices.rest/${WEBSERVICES_REST_VERSION}/file"
MODULE_DIR="./modules"

# Create modules directory if it doesn't exist
mkdir -p "$MODULE_DIR"

print_status "Downloading webservices.rest module..."
print_status "Version: $WEBSERVICES_REST_VERSION"

# Download the module
if [ ! -f "$MODULE_DIR/webservices.rest-${WEBSERVICES_REST_VERSION}.omod" ]; then
    print_status "Downloading from OpenMRS Add-on Index..."
    
    # Alternative download URLs if the main one fails
    DOWNLOAD_URLS=(
        "https://modules.openmrs.org/modulus/api/releases/1178/download"
        "https://github.com/openmrs/openmrs-module-webservices.rest/releases/download/2.46.0/webservices.rest-2.46.0.omod"
    )
    
    downloaded=false
    for url in "${DOWNLOAD_URLS[@]}"; do
        print_status "Trying download from: $url"
        if curl -L -o "$MODULE_DIR/webservices.rest-${WEBSERVICES_REST_VERSION}.omod" "$url"; then
            downloaded=true
            break
        else
            print_warning "Download failed from $url"
        fi
    done
    
    if [ "$downloaded" = false ]; then
        print_error "Failed to download webservices.rest module from all URLs"
        print_error "You can manually download it from: https://addons.openmrs.org/show/org.openmrs.module.webservices-rest"
        print_error "Place the .omod file in the $MODULE_DIR directory"
        exit 1
    fi
    
    print_success "Module downloaded successfully!"
else
    print_success "Module already exists: webservices.rest-${WEBSERVICES_REST_VERSION}.omod"
fi

# Verify the downloaded file
if [ -f "$MODULE_DIR/webservices.rest-${WEBSERVICES_REST_VERSION}.omod" ]; then
    filesize=$(stat -c%s "$MODULE_DIR/webservices.rest-${WEBSERVICES_REST_VERSION}.omod" 2>/dev/null || stat -f%z "$MODULE_DIR/webservices.rest-${WEBSERVICES_REST_VERSION}.omod" 2>/dev/null || echo "0")
    print_status "Module file size: $filesize bytes"
    
    if [ "$filesize" -gt 100000 ]; then  # More than 100KB
        print_success "Module file appears to be valid"
    else
        print_warning "Module file seems too small, might be corrupted"
    fi
fi

# Create a module configuration file for Docker Compose
print_status "Creating module configuration..."

cat > "$MODULE_DIR/modules.txt" << EOF
# OpenMRS Modules Configuration
# List of modules to be automatically loaded on startup
# Format: module-filename.omod

webservices.rest-${WEBSERVICES_REST_VERSION}.omod
EOF

print_success "Module configuration created"

# Update docker-compose.override.yml to mount modules directory
print_status "Updating Docker Compose configuration for modules..."

# Check if modules volume is already configured
if ! grep -q "modules:" docker-compose.override.yml; then
    # Add modules volume mount to the api service
    python3 << 'EOF'
import yaml
import sys

try:
    with open('docker-compose.override.yml', 'r') as f:
        compose = yaml.safe_load(f)
    
    # Ensure api service has volumes
    if 'volumes' not in compose['services']['api']:
        compose['services']['api']['volumes'] = []
    
    # Add modules volume if not already present
    modules_volume = "./modules:/openmrs/data/modules"
    if modules_volume not in compose['services']['api']['volumes']:
        compose['services']['api']['volumes'].append(modules_volume)
    
    with open('docker-compose.override.yml', 'w') as f:
        yaml.dump(compose, f, default_flow_style=False, sort_keys=False)
    
    print("SUCCESS: Updated docker-compose.override.yml")
except Exception as e:
    print(f"WARNING: Could not automatically update docker-compose.override.yml: {e}")
    print("Please manually add this volume mount to the api service:")
    print("  - ./modules:/openmrs/data/modules")
EOF
fi

print_success "REST API module setup completed! 🎉"
echo ""
echo "📖 Module Information:"
echo "  Name: webservices.rest"
echo "  Version: $WEBSERVICES_REST_VERSION"
echo "  Location: $MODULE_DIR/"
echo ""
echo "🚀 Next Steps:"
echo "1. Start the development environment:"
echo "   ${YELLOW}./dev-start.sh${NC}"
echo ""
echo "2. The module will be automatically loaded on startup"
echo ""
echo "3. Verify REST API is working:"
echo "   ${YELLOW}./test-api.sh${NC}"
echo ""
echo "4. Access REST API endpoints:"
echo "   ${YELLOW}http://localhost:8090/openmrs/ws/rest/v1${NC}"
echo ""
echo "5. View API documentation:"
echo "   ${YELLOW}http://localhost:8090/openmrs/module/webservices/rest/apiDocs.htm${NC}"