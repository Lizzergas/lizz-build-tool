# Installation guide

```bash
# Installs kotlin compiler
brew install kotlin
```

```bash
# Option 1: Automated setup (recommended)
# Use the provided script to automatically set KOTLIN_HOME with the latest brew version
./scripts/setup-kotlin-home.sh

# Option 2: Manual setup
# Sets KOTLIN_HOME that is used by the build tool to the aliases
echo 'export KOTLIN_HOME="/opt/homebrew/Cellar/kotlin/2.1.21/libexec:$KOTLIN_HOME"' >> ~/.zshrc
echo 'export PATH="$HOME/.local/lizz/bin:$PATH"' >> ~/.zshrc

# Note: when this is set, don't forget to restart your Intellij or other editor that has in-built terminal to update system environment variables
```

```bash
# Generates runnable binaries with dependencies to run build tool
./gradlew :app:installDist
```

```bash
# Generates a runnable binary in .local/lizz
./gradlew :app:devDist
```

```bash
# Location of where build tool is generated
cd app/build/install/app/bin
```

```bash
# How to init a project
./app init

# How to build a project
./app build
```
