#!/bin/bash

# OpenMRS Core REST API Testing Script
# Tests various REST API endpoints to verify functionality

set -e

echo "🧪 Testing OpenMRS Core REST API"
echo "================================"

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

BASE_URL="http://localhost:8090/openmrs"
REST_API_URL="${BASE_URL}/ws/rest/v1"

# Function to test endpoint
test_endpoint() {
    local endpoint=$1
    local description=$2
    local expected_status=${3:-200}
    
    print_status "Testing: $description"
    
    response=$(curl -s -w "%{http_code}" -o /tmp/response.json "$endpoint" || echo "000")
    
    if [ "$response" = "$expected_status" ]; then
        print_success "✓ $description - Status: $response"
        if [ -s /tmp/response.json ]; then
            echo "  Response preview: $(head -c 100 /tmp/response.json)..."
        fi
    else
        print_error "✗ $description - Expected: $expected_status, Got: $response"
        if [ -s /tmp/response.json ]; then
            echo "  Error response: $(cat /tmp/response.json)"
        fi
    fi
    echo ""
}

# Check if OpenMRS is running
print_status "Checking if OpenMRS is accessible..."
if ! curl -s "$BASE_URL" > /dev/null; then
    print_error "OpenMRS is not accessible at $BASE_URL"
    print_error "Make sure you've started the development environment with: ./dev-start.sh"
    exit 1
fi

print_success "OpenMRS is accessible at $BASE_URL"
echo ""

# Test basic endpoints
echo "🔍 Testing Basic Endpoints"
echo "========================="

test_endpoint "$BASE_URL" "OpenMRS Web Interface"
test_endpoint "$REST_API_URL" "REST API Root"
test_endpoint "$REST_API_URL/session" "Session Information"

echo "🏥 Testing Core Resources"
echo "========================"

test_endpoint "$REST_API_URL/patient" "Patient Resource"
test_endpoint "$REST_API_URL/person" "Person Resource"
test_endpoint "$REST_API_URL/user" "User Resource"
test_endpoint "$REST_API_URL/location" "Location Resource"
test_endpoint "$REST_API_URL/concept" "Concept Resource"

echo "🔧 Testing System Information"
echo "============================="

test_endpoint "$REST_API_URL/systemsetting" "System Settings"
test_endpoint "$REST_API_URL/module" "Modules Information"

echo "📋 Summary"
echo "=========="
print_success "REST API testing completed!"
echo ""
echo "📖 Useful REST API URLs:"
echo "  API Root:           $REST_API_URL"
echo "  API Documentation:  ${BASE_URL}/module/webservices/rest/apiDocs.htm"
echo "  REST Test Page:     ${BASE_URL}/module/webservices/rest/test"
echo ""
echo "🔐 Authentication Note:"
echo "  Most POST/PUT/DELETE operations require authentication"
echo "  Use admin/Admin123 credentials for testing"

# Cleanup
rm -f /tmp/response.json