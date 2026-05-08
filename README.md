# Springfield

A modular extension pack for Spring Framework, providing enhanced support for data access, security, web development, and more.

## Modules

| Module | Description |
|--------|-------------|
| springfield-commons | Core utilities and shared abstractions |
| springfield-data | Data access extensions (MyBatis integration, code generation) |
| springfield-extensions | General-purpose extensions (Validation, Jackson) |
| springfield-security | Spring Security extensions (authentication, authorization) |
| springfield-web | Web-layer utilities and Spring MVC enhancements |
| springfield-playground | Experimental features and sandbox |

## Requirements

- Java 21+
- Spring Boot 4.0.6
- Maven 3.9+

## Getting Started

Clone the repository and build:

```bash
git clone https://github.com/zennyx/springfield.git
cd springfield
./mvnw clean install
```

To use Springfield in your project, add the desired modules as dependencies:

```xml
<dependency>
  <groupId>zenny.toybox</groupId>
  <artifactId>springfield-data-mybatis</artifactId>
  <version>1.0.0</version>
</dependency>
```

## License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT).
