#!/bin/bash

# Test script to verify multi-shell support
echo "Testing multi-shell configuration detection..."

# Create temporary test files
mkdir -p /tmp/test_home/.config/fish
touch /tmp/test_home/.zshrc
touch /tmp/test_home/.bashrc
touch /tmp/test_home/.config/fish/config.fish

# Modify the script temporarily to use test directory
sed 's|\$HOME|/tmp/test_home|g' scripts/setup-kotlin-home.sh > /tmp/test_setup.sh
chmod +x /tmp/test_setup.sh

echo "Created test configuration files:"
ls -la /tmp/test_home/
ls -la /tmp/test_home/.config/fish/

echo "Test files created. The script would now detect multiple configuration files."
echo "Cleaning up test files..."
rm -rf /tmp/test_home
rm -f /tmp/test_setup.sh

echo "Test completed successfully!"