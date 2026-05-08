# Contributing to Springfield

Thank you for your interest in contributing to Springfield! This document provides guidelines and instructions for contributing.

## Development Setup

1. Fork and clone the repository
2. Ensure you have JDK 21+ and Maven 3.9+ installed
3. Build the project:

```bash
./mvnw clean install
```

## Code Style

This project uses [Spotless](https://github.com/diffplug/spotless) for code formatting. Please run the following before submitting a pull request:

```bash
./mvnw spotless:apply
```

To check for formatting issues without applying fixes:

```bash
./mvnw spotless:check
```

## Pull Request Process

1. Create a feature branch from `main`
2. Make your changes with clear, descriptive commit messages
3. Ensure all tests pass: `./mvnw test`
4. Apply code formatting: `./mvnw spotless:apply`
5. Submit a pull request with a description of the changes

## Reporting Issues

If you find a bug or have a feature request, please [open an issue](https://github.com/zennyx/springfield/issues/new) with a clear description and steps to reproduce.
