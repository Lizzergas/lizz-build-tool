# Lizz Build Tool - Development Guidelines

## Project Overview
Lizz is a lightweight and simple JVM-only Kotlin build tool that provides an alternative to traditional build systems. It uses YAML configuration files and provides CLI commands for project management.

## Build/Configuration Instructions

### Prerequisites
- Java 21 (configured via Gradle toolchain)
- Kotlin compiler installed via Homebrew: `brew install kotlin`
- Set KOTLIN_HOME environment variable:
  ```bash
  echo 'export KOTLIN_HOME="/opt/homebrew/Cellar/kotlin/2.2.0/libexec:$KOTLIN_HOME"' >> ~/.zshrc
  echo 'export PATH="$HOME/.local/lizz/bin:$PATH"' >> ~/.zshrc
  ```

### Build Commands
```bash
# Standard Gradle build
./gradlew build

# Generate distribution for development
./gradlew :app:installDist

# Generate accessible distribution from everywhere for development (prioritize usage of this when testing)
./gradlew :app:devDist

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
- **Init project**: `lizz init --test`

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
- **Kotlin**: 2.2.0
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

## Lizz CLI Commands Reference

### Available Commands
The lizz tool provides the following commands:

#### `lizz init` - Initialize New Project
Creates a new lizz project with interactive prompts for project configuration.

**Usage:**
```bash
# Interactive mode (prompts for user input)
lizz init

# Test mode (no prompts, predefined values) - NEW FEATURE
lizz init --test <folder_name>
```

**Interactive Mode:**
- Prompts for: name, version, description, author
- Creates `lizz.yaml` configuration file
- Generates `Main.kt` with hello world example
- Creates Gradle stub files for IDE support
- Creates `.gitignore` file

**Test Mode (--test flag):**
- **Purpose**: Designed for AI testing to avoid TTY interaction issues
- **No prompts**: Uses predefined test values automatically
- **File creation**: Creates project files directly in current directory
- **Predefined values**:
  - name: `<test_name>`
  - version: `1.0.0-test`
  - description: `Test project for AI testing`
  - author: `lizz-test`
  - dependencies: `com.google.code.gson:gson:2.10.1`
- **Files created**: `lizz.yaml`, `settings.gradle.kts`, `build.gradle.kts`, `main.kt`, `.gitignore`

**Example:**
```bash
lizz init --test test-sync-issue
# Creates project files in current directory
# Files: lizz.yaml, settings.gradle.kts, build.gradle.kts, main.kt, .gitignore
```

#### `lizz build` - Compile Project
Compiles Kotlin source files and creates executable JAR.

**Usage:**
```bash
lizz build
```

**What it does:**
- Resolves dependencies if not cached
- Compiles all `.kt` files in project
- Creates executable JAR in `build/` directory
- Requires existing `lizz.yaml` configuration

#### `lizz run` - Compile and Execute
Compiles the project and immediately runs the main class.

**Usage:**
```bash
lizz run
```

**What it does:**
- Performs `lizz build` first
- Executes the compiled JAR
- Uses `mainClass` from `lizz.yaml` (default: `MainKt`)

#### `lizz sync` - Resolve Dependencies
Downloads and caches project dependencies from Maven repositories.

**Usage:**
```bash
lizz sync
```

**What it does:**
- Reads dependencies from `lizz.yaml`
- Downloads JARs from Maven Central
- Caches dependencies locally
- Updates Gradle stub files for IDE support
- Creates Gradle files if they don't exist

#### `lizz clean` - Clean Build Artifacts
Removes build directory and compiled artifacts.

**Usage:**
```bash
lizz clean
```

**What it does:**
- Deletes `build/` directory
- Removes compiled JAR files
- Keeps source code and configuration intact

#### `lizz sh` - Execute Custom Scripts
Runs custom shell scripts defined in `lizz.yaml`.

**Usage:**
```bash
# List available scripts
lizz sh

# Run specific script
lizz sh <script_name>
```

**Script Configuration in lizz.yaml:**
```yaml
scripts:
  test: "echo 'Running tests'"
  deploy: "echo 'Deploying application'"
```

### Setting Up Testing Projects

#### For AI Testing (Recommended)
Use the `--test` flag to create projects without TTY interaction:

```bash
# Create test project in current directory
mkdir my-test-project && cd my-test-project
lizz init --test my-test-project

# Test various commands
lizz sync    # Resolve dependencies
lizz build   # Compile project
lizz run     # Run the application
lizz clean   # Clean build artifacts

# Clean up when done
cd .. && rm -rf my-test-project
```

#### For Manual Testing
Use regular init for interactive setup:

```bash
mkdir my-project
cd my-project
lizz init
# Follow prompts...
```

### Command Dependencies and Order

1. **Start with**: `lizz init` (or `lizz init --test <name>`)
2. **Then**: `lizz sync` (resolves dependencies)
3. **Build**: `lizz build` (compiles code)
4. **Run**: `lizz run` (executes application)
5. **Clean**: `lizz clean` (when needed)
6. **Scripts**: `lizz sh <script>` (custom commands)

### Project Structure After Init
```
project-folder/
├── lizz.yaml           # Project configuration
├── Main.kt            # Main source file
├── .gitignore         # Git ignore rules
├── settings.gradle.kts # Gradle stub (IDE support)
└── build.gradle.kts   # Gradle stub (IDE support)
```

### Common Workflows

#### Testing New Features
```bash
mkdir feature-test && cd feature-test
lizz init --test feature-test
# Modify main.kt or add new files
lizz sync
lizz build
lizz run
cd .. && rm -rf feature-test
```

#### Development Workflow
```bash
mkdir dev-project && cd dev-project
lizz init --test dev-project
lizz sync          # Once after init
# Edit code...
lizz run           # Compile and run
# Edit more...
lizz run           # Quick iteration
lizz clean         # When needed
```

## Debugging Tips
- Use `[DEBUG_LOG]` prefix for debug output in tests
- Check KOTLIN_HOME environment variable if compilation fails
- Verify `lizz.yaml` format when configuration issues occur
- Use `./gradlew :app:devDist` for quick local testing iterations
- Use `lizz init --test` for AI testing to avoid TTY issues
- Always clean up test projects by deleting the test directory after testing
