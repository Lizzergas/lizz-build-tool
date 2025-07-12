# Lizz Build Tool - Development Guidelines

## Project Overview
Lizz is a lightweight and simple JVM-only Kotlin build tool that provides an alternative to traditional build systems. It uses YAML configuration files and provides CLI commands for project management.

## Build/Configuration Instructions

### Prerequisites
- Java 21 (configured via Gradle toolchain)
- Kotlin compiler installed via Homebrew: `brew install kotlin`
- Set KOTLIN_HOME environment variable:
  ```bash
  echo 'export KOTLIN_HOME="/opt/homebrew/Cellar/kotlin/2.1.21/libexec:$KOTLIN_HOME"' >> ~/.zshrc
  echo 'export PATH="$HOME/.local/lizz/bin:$PATH"' >> ~/.zshrc
  ```

### Build Commands
```bash
# Standard Gradle build
./gradlew build

# Generate distribution for development
./gradlew :app:installDist

# Install to local development directory (~/.local/lizz)
./gradlew :app:devDist
```

### Key Build Features
- **Custom Build Config Generation**: The `generateBuildConfig` task creates a `BuildConfig.kt` file with version information
- **Application Distribution**: Creates executable scripts in `app/build/install/app/bin/`
- **Development Installation**: `devDist` task installs to `~/.local/lizz/bin` for easy testing

## Testing Information

### Test Configuration
- **Framework**: JUnit 5 (Jupiter)
- **Kotlin Test**: Uses `kotlin-test-junit5` for Kotlin-specific assertions
- **Test Location**: `app/src/test/kotlin/com/skommy/`

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.skommy.AppTest"

# Run specific test method
./gradlew test --tests "com.skommy.AppTest.test BuildSettingsService save and load functionality"
```

### Adding New Tests
1. Create test classes in `app/src/test/kotlin/com/skommy/` following the package structure
2. Use JUnit 5 annotations (`@Test`, `@BeforeEach`, `@AfterEach`)
3. Use `@TempDir` for file system tests to avoid side effects
4. Add debug logging with `[DEBUG_LOG]` prefix for debugging:
   ```kotlin
   println("[DEBUG_LOG] Your debug message here")
   ```

### Example Test Structure
```kotlin
class MyComponentTest {
    @TempDir
    lateinit var tempDir: Path
    
    @Test
    fun `test component functionality`() {
        println("[DEBUG_LOG] Testing component functionality")
        // Test implementation
        assertTrue(condition, "Assertion message")
    }
}
```

## Architecture & Key Components

### Core Components
- **Main.kt**: CLI entry point using Clikt framework with subcommands (Init, Build, Run, Clean, Sync, Sh)
- **BuildSettings**: YAML-serializable configuration model for project settings
- **BuildSettingsService**: Handles YAML serialization/deserialization using KAML library
- **CLI Commands**: Individual command implementations in `com.skommy.cli` package
- **MavenResolver**: Dependency resolution using Apache Maven Resolver
- **LizzJVMCompiler**: Kotlin compilation using embedded Kotlin compiler

### Configuration
- **Config File**: `lizz.yaml` in project root
- **Main Class**: Configurable via `mainClass` property (default: `MainKt`)
- **Dependencies**: Maven coordinates in `dependencies` array
- **Scripts**: Custom commands in `scripts` map

### Technology Stack
- **Kotlin**: 2.1.20
- **CLI Framework**: Clikt 5.0.1
- **Serialization**: kotlinx.serialization 1.8.1 + KAML 0.82.0 for YAML
- **Dependency Resolution**: Apache Maven Resolver 2.0.9
- **Compilation**: kotlin-compiler-embeddable
- **Testing**: JUnit 5.12.1

## Development Guidelines

### Code Style
- Use Kotlin idioms and conventions
- Prefer data classes for configuration models
- Use sealed classes for command/script hierarchies
- Implement proper error handling with meaningful messages

### File Organization
- CLI commands: `com.skommy.cli`
- Models: `com.skommy.models`
- Services: `com.skommy.services`
- Utilities: `com.skommy` (root package)

### Constants Management
- **BuildConstants**: Project-level constants (file names, versions, defaults)
- **CompilerConstants**: Kotlin compiler and classpath related constants

### Error Handling
- Use `currentContext.exitProcess(1)` for CLI command failures
- Provide clear error messages to users
- Log debug information when appropriate

### Testing Best Practices
- Use temporary directories for file system tests
- Test both success and failure scenarios
- Mock external dependencies when possible
- Use descriptive test names with backticks for readability

### Dependency Management
- Keep dependencies minimal and focused
- Use version catalogs (`gradle/libs.versions.toml`) for dependency management
- Bundle related dependencies (e.g., maven-resolver bundle)

## Debugging Tips
- Use `[DEBUG_LOG]` prefix for debug output in tests
- Check KOTLIN_HOME environment variable if compilation fails
- Verify `lizz.yaml` format when configuration issues occur
- Use `./gradlew :app:devDist` for quick local testing iterations