# Scripts

This directory contains utility scripts for the Lizz Build Tool project.

## setup-kotlin-home.sh

A shell script that automatically configures the `KOTLIN_HOME` environment variable with the latest Kotlin version installed via Homebrew.

### Features

- **Automatic Detection**: Detects the latest Kotlin version installed via Homebrew
- **Smart Updates**: Only updates `KOTLIN_HOME` if a newer version is available
- **Version Comparison**: Compares current `KOTLIN_HOME` with the latest brew version
- **Shell Integration**: Automatically adds/updates `KOTLIN_HOME` in your shell configuration file
- **Multi-Shell Support**: Full support for bash, zsh, and fish shells with appropriate syntax
- **Interactive Selection**: When multiple shell configuration files are found, prompts user to select which one to update
- **Smart Shell Detection**: Automatically detects your current shell and finds relevant configuration files
- **Safety**: Creates backups of shell configuration files before making changes

### Usage

```bash
# Make the script executable (if not already)
chmod +x scripts/setup-kotlin-home.sh

# Run the script
./scripts/setup-kotlin-home.sh
```

### Scenarios Handled

1. **No KOTLIN_HOME set**: Exports the latest brew Kotlin version
2. **KOTLIN_HOME already set**: Compares versions and updates if necessary
3. **Same version**: Informs that the latest version is already configured
4. **Multiple shell config files**: Prompts user to select which configuration file to update
5. **No Kotlin installed**: Recommends installing via `brew install kotlin`
6. **No Homebrew**: Informs that Homebrew needs to be installed first

### Output Examples

#### When KOTLIN_HOME is already up-to-date:
```
[INFO] Found Kotlin version 2.2.0 installed via Homebrew
[INFO] Kotlin path: /opt/homebrew/opt/kotlin
[SUCCESS] Latest Kotlin version (2.2.0) is already defined in KOTLIN_HOME
[INFO] No changes needed.
```

#### When KOTLIN_HOME needs to be set:
```
[INFO] Found Kotlin version 2.2.0 installed via Homebrew
[INFO] Kotlin path: /opt/homebrew/opt/kotlin
[INFO] KOTLIN_HOME is not currently set
[INFO] Setting KOTLIN_HOME to latest version 2.2.0
[SUCCESS] KOTLIN_HOME has been set to: /opt/homebrew/opt/kotlin/libexec
[WARNING] Please restart your terminal or run: source ~/.zshrc
```

#### When multiple configuration files are found:
```
[INFO] Found Kotlin version 2.2.0 installed via Homebrew
[INFO] Kotlin path: /opt/homebrew/opt/kotlin
[INFO] Detected shell: zsh
[INFO] Multiple shell configuration files found:
  1. /Users/username/.zshrc
  2. /Users/username/.bashrc
  3. /Users/username/.profile
Please select which file to update (1-3): 1
[INFO] Using shell configuration file: /Users/username/.zshrc
[SUCCESS] Added export: export KOTLIN_HOME="/opt/homebrew/opt/kotlin/libexec"
```

#### When Kotlin is not installed:
```
[ERROR] Kotlin is not installed via Homebrew.
[INFO] Please install Kotlin using: brew install kotlin
```

### Requirements

- macOS or Linux
- Homebrew package manager
- Kotlin installed via Homebrew (`brew install kotlin`)

### Notes

- The script automatically detects your shell type (bash, zsh, fish) and finds relevant configuration files
- Supports multiple shell configuration files: `.zshrc`, `.bashrc`, `.bash_profile`, `.profile`, and `~/.config/fish/config.fish`
- When multiple configuration files are found, the script prompts you to select which one to update
- For fish shell, uses `set -gx` syntax instead of `export` for proper environment variable setting
- A backup of your shell configuration file is created before making changes
- The script sets `KOTLIN_HOME` for both the current session and future sessions (except for fish shell, which requires terminal restart)
- The script verifies that the Kotlin compiler is available at the expected location
