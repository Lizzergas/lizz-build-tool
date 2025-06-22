# Installation guide

```bash
# Installs kotlin compiler
brew install kotlin
```

```bash
# Sets KOTLIN_HOME that is used by the build tool to the aliases
echo 'export KOTLIN_HOME="/opt/homebrew/Cellar/kotlin/2.1.21/libexec:$KOTLIN_HOME"' >> ~/.zshrc
```

```bash
# Generates runnable binaries with dependencies to run build tool
./gradlew :app:installDist
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
