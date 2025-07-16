#!/bin/bash

echo "🔧 Updating SculkSignal project..."

# First, let's check current Gradle version
echo "Current Gradle wrapper version:"
./gradlew --version

# Update Gradle wrapper to latest version
echo ""
echo "📦 Updating Gradle wrapper to latest version..."
./gradlew wrapper --gradle-version=8.10.2 --distribution-type=bin

# Update build.gradle with latest versions and fix deprecations
echo ""
echo "🔨 Updating build.gradle..."

cat > build.gradle << 'EOF'
plugins {
    id 'java'
    id 'maven-publish'
    id 'java-library'
}

group = 'com.sculksignal'
version = '1.0.0-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven {
        name = 'papermc-repo'
        url = 'https://repo.papermc.io/repository/maven-public/'
    }
    maven {
        name = 'sonatype'
        url = 'https://oss.sonatype.org/content/groups/public/'
    }
}

dependencies {
    compileOnly 'io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT'
    
    // Testing dependencies
    testImplementation platform('org.junit:junit-bom:5.10.1')
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testImplementation 'org.mockito:mockito-core:5.8.0'
    testImplementation 'org.mockito:mockito-junit-jupiter:5.8.0'
}

test {
    useJUnitPlatform()
    testLogging {
        events "passed", "skipped", "failed"
    }
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
    options.release = 21
}

jar {
    archiveFileName = "${project.name}-${project.version}.jar"
    
    manifest {
        attributes(
            'Implementation-Title': project.name,
            'Implementation-Version': project.version,
            'Implementation-Vendor': 'voidfemme'
        )
    }
}

// Process resources to replace placeholders
processResources {
    def props = [version: version]
    inputs.properties props
    filteringCharset 'UTF-8'
    filesMatching('plugin.yml') {
        expand props
    }
}
EOF

# Update gradle.properties with latest optimizations
echo ""
echo "⚡ Updating gradle.properties with performance optimizations..."

cat > gradle.properties << 'EOF'
# Gradle performance optimizations
org.gradle.jvmargs=-Xmx4G -XX:+UseG1GC -XX:+ParallelRefProcEnabled
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.daemon=true
org.gradle.configureondemand=true

# Build reproducibility
org.gradle.console=plain
org.gradle.logging.level=lifecycle
EOF

# Create gradle wrapper properties if missing
echo ""
echo "📋 Ensuring gradle wrapper is properly configured..."

mkdir -p gradle/wrapper

cat > gradle/wrapper/gradle-wrapper.properties << 'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.10.2-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/cache
EOF

# Download gradle wrapper jar if missing
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "⬇️  Downloading gradle wrapper jar..."
    curl -L -o gradle/wrapper/gradle-wrapper.jar https://github.com/gradle/gradle/raw/v8.10.2/gradle/wrapper/gradle-wrapper.jar
fi

# Make gradlew executable
chmod +x gradlew

# Test the build
echo ""
echo "🧪 Testing the build..."
./gradlew clean build --info

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful! Project is ready for development."
    echo ""
    echo "📋 Next steps:"
    echo "1. ./gradlew build      - Build the project"
    echo "2. ./gradlew test       - Run tests"
    echo "3. ./gradlew jar        - Create plugin JAR"
    echo ""
    echo "🔍 Key files to implement next:"
    echo "- PathCalculator.java   - Network pathfinding algorithms"
    echo "- SignalPropagator.java - Signal transmission logic"
    echo "- ChunkCoordinate.java  - Spatial indexing utilities"
else
    echo ""
    echo "❌ Build failed. Check the output above for errors."
    echo "💡 Common fixes:"
    echo "- Ensure Java 21 is installed"
    echo "- Check internet connection for dependency downloads"
    echo "- Review any compilation errors in the Java files"
fi
