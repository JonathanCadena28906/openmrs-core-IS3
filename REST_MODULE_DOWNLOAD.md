# REST API Module Download Instructions

Since automatic download may fail in some environments, here are manual download options for the webservices.rest module:

## Option 1: Direct Download Links

Download the webservices.rest module manually from these sources:

1. **OpenMRS Add-on Index:**
   - URL: https://addons.openmrs.org/show/org.openmrs.module.webservices-rest
   - Look for the latest version (2.46.0 or newer)

2. **GitHub Releases:**
   - URL: https://github.com/openmrs/openmrs-module-webservices.rest/releases
   - Download the `.omod` file for version 2.46.0

3. **Alternative sources:**
   - Maven Central: https://search.maven.org/artifact/org.openmrs.module/webservices.rest-omod
   - OpenMRS Maven Repository: https://mavenrepo.openmrs.org/

## Option 2: Build from Source

```bash
# Clone the webservices.rest module
git clone https://github.com/openmrs/openmrs-module-webservices.rest.git

# Navigate to the repository
cd openmrs-module-webservices.rest

# Build the module (requires Java 8)
mvn clean install

# Copy the built module
cp omod/target/webservices.rest-*.omod ../openmrs-core-IS3/modules/
```

## Installation Instructions

1. **Download the module** using one of the methods above
2. **Place the .omod file** in the `modules/` directory of your OpenMRS Core project
3. **Rename the file** to `webservices.rest-2.46.0.omod` (or appropriate version)
4. **Restart OpenMRS** using `./dev-start.sh`

## Verification

After installation:

1. Start OpenMRS: `./dev-start.sh`
2. Access admin panel: http://localhost:8090/openmrs/admin
3. Go to "Manage Modules" to verify the REST module is loaded
4. Test API: `./test-api.sh`

## Note about Java Compatibility

The webservices.rest module is compatible with OpenMRS Core 3.x and should work with Java 17+ (the version we're using), despite some documentation mentioning Java 8 requirements for building.