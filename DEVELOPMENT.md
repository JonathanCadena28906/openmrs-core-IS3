# OpenMRS Core Local Development Guide

This guide provides step-by-step instructions for setting up a local development environment for the OpenMRS Core fork with REST API integration.

## 🚀 Quick Start

### Prerequisites

- **Docker** and **Docker Compose** (recommended)
- **Java 17+** (for local builds)
- **Maven 3.6+** (for local builds)
- **Git** (already installed)

### One-Command Setup

```bash
# Setup and start everything
./dev-setup.sh && ./setup-rest-module.sh && ./dev-build.sh && ./dev-start.sh
```

## 📋 Detailed Setup Instructions

### 1. Initial Environment Setup

Run the setup script to configure your development environment:

```bash
./dev-setup.sh
```

This script will:
- Check prerequisites (Docker, Java, Maven)
- Create a `.env` file with development settings
- Set up data directories for persistence
- Configure environment variables

### 2. REST API Module Setup

Download and configure the webservices.rest module:

```bash
./setup-rest-module.sh
```

This script will:
- Download the latest webservices.rest module (v2.46.0)
- Configure module auto-loading
- Update Docker Compose for module mounting

### 3. Build Development Image

Build the OpenMRS development Docker image:

```bash
./dev-build.sh
```

This will:
- Build the OpenMRS Core with optimized Maven settings
- Create a development-ready Docker image
- Skip tests for faster builds

### 4. Start Development Environment

Launch the complete development environment:

```bash
./dev-start.sh
```

This starts:
- **MariaDB** database on port 3306
- **OpenMRS Core** on port 8090
- **Debug port** on 8000

## 🌐 Access URLs

Once started, access these URLs:

| Service | URL | Description |
|---------|-----|-------------|
| **OpenMRS Web** | http://localhost:8090/openmrs | Main web interface |
| **REST API Root** | http://localhost:8090/openmrs/ws/rest/v1 | REST API endpoints |
| **API Documentation** | http://localhost:8090/openmrs/module/webservices/rest/apiDocs.htm | Interactive API docs |
| **REST Test Page** | http://localhost:8090/openmrs/module/webservices/rest/test | API testing interface |
| **Admin Panel** | http://localhost:8090/openmrs/admin | Administration |

## 🔧 Development Commands

### Daily Development Workflow

```bash
# Start development environment
./dev-start.sh

# View logs
docker compose logs -f api

# Restart after code changes
docker compose restart api

# Stop environment
./dev-stop.sh
```

### Testing and Verification

```bash
# Test REST API endpoints
./test-api.sh

# View service status
docker compose ps

# Access database directly
docker compose exec db mysql -u openmrs -popenmrs openmrs_dev
```

### Advanced Commands

```bash
# Rebuild after major changes
./dev-build.sh

# Reset everything (including database)
docker compose down -v
./dev-start.sh

# Debug container
docker compose exec api bash

# View specific service logs
docker compose logs -f db
```

## 📁 Project Structure

```
openmrs-core-IS3/
├── api/                    # Core API Java code
├── web/                    # Web layer Java code
├── webapp/                 # Web application resources
├── modules/                # OpenMRS modules (.omod files)
├── target/data/           # OpenMRS application data
├── docker-compose.yml     # Production Docker config
├── docker-compose.override.yml  # Development overrides
├── dev-setup.sh          # Environment setup script
├── dev-build.sh          # Build script
├── dev-start.sh          # Start development environment
├── dev-stop.sh           # Stop development environment
├── setup-rest-module.sh  # REST module setup
├── test-api.sh           # API testing script
└── .env                  # Environment variables
```

## 🔐 Default Credentials

### OpenMRS Admin User
- **Username:** `admin`
- **Password:** `Admin123`

### Database
- **Host:** `localhost:3306`
- **Database:** `openmrs_dev`
- **Username:** `openmrs`
- **Password:** `openmrs`
- **Root Password:** `openmrs`

## 🛠️ Development Tips

### Hot Reload

For Java code changes:
1. Make changes to Java files
2. Run: `docker compose restart api`
3. Wait for startup (check logs: `docker compose logs -f api`)

### Adding New Modules

1. Place `.omod` files in the `modules/` directory
2. Restart the API service: `docker compose restart api`
3. Verify in Admin → Manage Modules

### Database Management

```bash
# Backup database
docker compose exec db mysqldump -u openmrs -popenmrs openmrs_dev > backup.sql

# Restore database
docker compose exec -T db mysql -u openmrs -popenmrs openmrs_dev < backup.sql

# Reset to fresh database
docker compose down -v
docker compose up -d
```

### Performance Optimization

- The development image uses multi-threading (`-T 1C`)
- Tests are skipped during builds (`-DskipTests`)
- Docker volumes provide persistence across restarts
- Debug port (8000) is available for IDE integration

## 🧪 Testing REST API

### Basic Testing

```bash
# Test all major endpoints
./test-api.sh

# Test specific endpoint
curl http://localhost:8090/openmrs/ws/rest/v1/patient

# Test with authentication
curl -u admin:Admin123 http://localhost:8090/openmrs/ws/rest/v1/patient
```

### Common REST Endpoints

- `GET /ws/rest/v1/patient` - List patients
- `GET /ws/rest/v1/person` - List persons
- `GET /ws/rest/v1/user` - List users
- `GET /ws/rest/v1/location` - List locations
- `GET /ws/rest/v1/concept` - List concepts
- `GET /ws/rest/v1/encounter` - List encounters
- `GET /ws/rest/v1/obs` - List observations

## 🚨 Troubleshooting

### Common Issues

#### Port Already in Use
```bash
# Check what's using port 8090
lsof -i :8090

# Stop conflicting services
./dev-stop.sh
```

#### Database Connection Issues
```bash
# Check database status
docker compose ps

# Restart database
docker compose restart db

# Check database logs
docker compose logs db
```

#### Module Loading Issues
```bash
# Check module directory
ls -la modules/

# Verify module permissions
chmod 644 modules/*.omod

# Check OpenMRS logs
docker compose logs api | grep -i module
```

#### Build Failures
```bash
# Clean and rebuild
docker compose down
docker system prune -f
./dev-build.sh
```

### Getting Help

1. **Check logs:** `docker compose logs -f`
2. **Verify services:** `docker compose ps`
3. **Test connectivity:** `./test-api.sh`
4. **Reset environment:** `docker compose down -v && ./dev-start.sh`

## 🎯 Next Steps for Development

After setup is complete:

1. **Explore the API:** Use the interactive documentation at `/module/webservices/rest/apiDocs.htm`
2. **Test endpoints:** Run `./test-api.sh` to verify functionality
3. **Develop new features:** Modify code in `api/`, `web/`, or `webapp/` directories
4. **Add modules:** Place new `.omod` files in the `modules/` directory
5. **Create tests:** Add unit tests in the respective test directories

## 📚 Additional Resources

- [OpenMRS Developer Guide](https://wiki.openmrs.org/display/docs/Developer+Guide)
- [REST API Documentation](https://wiki.openmrs.org/display/docs/REST+Web+Services+API+For+Clients)
- [Module Development](https://wiki.openmrs.org/display/docs/Module+Development+Guide)
- [OpenMRS SDK](https://wiki.openmrs.org/display/docs/OpenMRS+SDK)

---

**Happy coding!** 🎉 Your OpenMRS Core development environment is ready for building and testing new functionality.