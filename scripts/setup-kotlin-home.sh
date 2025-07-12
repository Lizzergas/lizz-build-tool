#!/bin/bash

# Script to set up KOTLIN_HOME with the latest Kotlin version from Homebrew
# This script handles various scenarios for KOTLIN_HOME management

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if brew is installed
if ! command -v brew &> /dev/null; then
    print_error "Homebrew is not installed. Please install Homebrew first."
    exit 1
fi

# Check if kotlin is installed via brew
if ! brew list kotlin &> /dev/null; then
    print_error "Kotlin is not installed via Homebrew."
    print_info "Please install Kotlin using: ${GREEN}brew install kotlin${NC}"
    exit 1
fi

# Get the latest kotlin version installed via brew
BREW_KOTLIN_PATH=$(brew --prefix kotlin)
BREW_KOTLIN_VERSION=$(brew list --versions kotlin | awk '{print $2}')

if [ -z "$BREW_KOTLIN_VERSION" ]; then
    print_error "Could not determine Kotlin version from Homebrew."
    exit 1
fi

print_info "Found Kotlin version $BREW_KOTLIN_VERSION installed via Homebrew"
print_info "Kotlin path: $BREW_KOTLIN_PATH"

# Construct the new KOTLIN_HOME path
NEW_KOTLIN_HOME="$BREW_KOTLIN_PATH/libexec"

# Check current KOTLIN_HOME
if [ -n "$KOTLIN_HOME" ]; then
    print_info "Current KOTLIN_HOME: $KOTLIN_HOME"

    # Extract version from current KOTLIN_HOME if possible
    CURRENT_VERSION=""
    if [[ "$KOTLIN_HOME" =~ kotlin/([0-9]+\.[0-9]+\.[0-9]+) ]]; then
        CURRENT_VERSION="${BASH_REMATCH[1]}"
    fi

    # Check if the versions are the same
    if [ "$CURRENT_VERSION" = "$BREW_KOTLIN_VERSION" ]; then
        print_success "Latest Kotlin version ($BREW_KOTLIN_VERSION) is already defined in KOTLIN_HOME"
        print_info "No changes needed."
        exit 0
    else
        print_warning "KOTLIN_HOME currently points to version $CURRENT_VERSION"
        print_info "Updating KOTLIN_HOME to latest version $BREW_KOTLIN_VERSION"
    fi
else
    print_info "KOTLIN_HOME is not currently set"
    print_info "Setting KOTLIN_HOME to latest version $BREW_KOTLIN_VERSION"
fi

# Function to detect shell type and find configuration files
detect_shell_configs() {
    local configs=()

    # Detect current shell
    local current_shell=""
    if [ -n "$ZSH_VERSION" ]; then
        current_shell="zsh"
    elif [ -n "$BASH_VERSION" ]; then
        current_shell="bash"
    elif [ -n "$FISH_VERSION" ]; then
        current_shell="fish"
    else
        # Try to detect from SHELL environment variable
        case "$SHELL" in
            */zsh) current_shell="zsh" ;;
            */bash) current_shell="bash" ;;
            */fish) current_shell="fish" ;;
        esac
    fi

    print_info "Detected shell: ${current_shell:-unknown}"

    # Find existing shell configuration files
    local potential_configs=(
        "$HOME/.zshrc"
        "$HOME/.bashrc"
        "$HOME/.bash_profile"
        "$HOME/.profile"
        "$HOME/.config/fish/config.fish"
    )

    for config in "${potential_configs[@]}"; do
        if [ -f "$config" ]; then
            configs+=("$config")
        fi
    done

    # If no configs found, create default based on detected shell
    if [ ${#configs[@]} -eq 0 ]; then
        case "$current_shell" in
            "zsh")
                configs=("$HOME/.zshrc")
                ;;
            "bash")
                if [[ "$OSTYPE" == "darwin"* ]]; then
                    configs=("$HOME/.bash_profile")
                else
                    configs=("$HOME/.bashrc")
                fi
                ;;
            "fish")
                mkdir -p "$HOME/.config/fish"
                configs=("$HOME/.config/fish/config.fish")
                ;;
            *)
                configs=("$HOME/.profile")
                ;;
        esac
        print_info "No existing shell configuration files found. Will create: ${configs[0]}"
    fi

    echo "${configs[@]}"
}

# Function to let user select configuration file
select_shell_config() {
    local configs=($@)

    if [ ${#configs[@]} -eq 1 ]; then
        echo "${configs[0]}"
        return
    fi

    print_info "Multiple shell configuration files found:"
    for i in "${!configs[@]}"; do
        echo "  $((i+1)). ${configs[i]}"
    done

    while true; do
        echo -n "Please select which file to update (1-${#configs[@]}): "
        read -r choice

        if [[ "$choice" =~ ^[0-9]+$ ]] && [ "$choice" -ge 1 ] && [ "$choice" -le ${#configs[@]} ]; then
            echo "${configs[$((choice-1))]}"
            return
        else
            print_error "Invalid selection. Please enter a number between 1 and ${#configs[@]}."
        fi
    done
}

# Function to add export to shell config file
add_export_to_config() {
    local config_file="$1"
    local export_line="$2"

    # Handle fish shell differently
    if [[ "$config_file" == *"config.fish" ]]; then
        # Fish shell uses 'set -gx' instead of 'export'
        local fish_export="set -gx KOTLIN_HOME \"$NEW_KOTLIN_HOME\""

        # Remove existing KOTLIN_HOME lines for fish
        if [ -f "$config_file" ]; then
            grep -v "set -gx KOTLIN_HOME" "$config_file" > "$config_file.tmp" 2>/dev/null || cp "$config_file" "$config_file.tmp"
            mv "$config_file.tmp" "$config_file"
        fi

        echo "$fish_export" >> "$config_file"
        print_success "Added fish shell export: $fish_export"
    else
        # Standard bash/zsh export
        # Remove existing KOTLIN_HOME exports (if any)
        if [ -f "$config_file" ]; then
            grep -v "export KOTLIN_HOME=" "$config_file" > "$config_file.tmp" 2>/dev/null || cp "$config_file" "$config_file.tmp"
            mv "$config_file.tmp" "$config_file"
        fi

        echo "$export_line" >> "$config_file"
        print_success "Added export: $export_line"
    fi
}

# Determine shell configuration file(s)
SHELL_CONFIGS=($(detect_shell_configs))
SHELL_CONFIG=$(select_shell_config "${SHELL_CONFIGS[@]}")

print_info "Using shell configuration file: $SHELL_CONFIG"

# Backup the shell config file
cp "$SHELL_CONFIG" "$SHELL_CONFIG.backup.$(date +%Y%m%d_%H%M%S)" 2>/dev/null || true

# Add the new KOTLIN_HOME export using the appropriate method for the shell
EXPORT_LINE="export KOTLIN_HOME=\"$NEW_KOTLIN_HOME\""
add_export_to_config "$SHELL_CONFIG" "$EXPORT_LINE"

print_success "KOTLIN_HOME has been set to: $NEW_KOTLIN_HOME"
print_info "Added to: $SHELL_CONFIG"

# Provide appropriate reload instruction based on shell type
if [[ "$SHELL_CONFIG" == *"config.fish" ]]; then
    print_warning "Please restart your terminal or run: ${GREEN}source $SHELL_CONFIG${NC}"
else
    print_warning "Please restart your terminal or run: ${GREEN}source $SHELL_CONFIG${NC}"
fi

# Also export for current session
if [[ "$SHELL_CONFIG" == *"config.fish" ]]; then
    # Fish shell - we can't directly set environment variables from bash script for fish
    print_info "For fish shell, please restart your terminal or source the config file to apply changes"
else
    export KOTLIN_HOME="$NEW_KOTLIN_HOME"
    print_info "KOTLIN_HOME has been exported for the current session"
fi

# Verify the installation
if [ -f "$NEW_KOTLIN_HOME/bin/kotlinc" ]; then
    print_success "Kotlin compiler found at: $NEW_KOTLIN_HOME/bin/kotlinc"
else
    print_warning "Kotlin compiler not found at expected location: $NEW_KOTLIN_HOME/bin/kotlinc"
fi

print_success "Setup completed successfully!"
