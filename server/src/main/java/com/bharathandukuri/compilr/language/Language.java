package com.bharathandukuri.compilr.language;

import com.bharathandukuri.compilr.execution.dto.DockerImageDetails;
import com.bharathandukuri.compilr.execution.registry.DockerImageRegistry;

import java.util.List;

public interface Language {

    String id();

    String name();

    LanguageType type();

    String fileExtension();

    DockerImageRegistry dockerImage();

    default DockerImageDetails dockerImageDetails() {
        DockerImageRegistry registry = dockerImage();
        return registry != null ? registry.dockerImage() : null;
    }

    default String dockerImageReference() {
        DockerImageDetails details = dockerImageDetails();
        return details != null ? details.reference() : null;
    }

    default String version() {
        return switch (id().toLowerCase()) {
            case "java-21" -> "21 (OpenJDK)";
            case "c-17" -> "17 (GCC 14)";
            case "cpp-23" -> "23 (GCC 14)";
            case "python-3.12" -> "3.12 (CPython)";
            case "javascript-node-20" -> "20 (Node.js)";
            case "mysql-8.0" -> "8.0";
            case "postgresql-16" -> "16";
            default -> "latest";
        };
    }

    default List<String> aliases() {
        return switch (id().toLowerCase()) {
            case "java-21" -> List.of("java", "java21");
            case "c-17" -> List.of("c", "c17");
            case "cpp-23" -> List.of("cpp", "c++", "cpp23");
            case "python-3.12" -> List.of("python", "py", "python3", "python312");
            case "javascript-node-20" -> List.of("javascript", "js", "node", "nodejs");
            case "mysql-8.0" -> List.of("mysql", "sql");
            case "postgresql-16" -> List.of("postgres", "postgresql", "psql");
            default -> List.of();
        };
    }

    default String defaultStarterCode() {
        return switch (id().toLowerCase()) {
            case "java-21" -> """
                import java.util.Scanner;

                public class Main {
                    public static void main(String[] args) {
                        Scanner scanner = new Scanner(System.in);
                        if (scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            System.out.println("Hello, " + line + "!");
                        } else {
                            System.out.println("Hello, World!");
                        }
                    }
                }
                """;
            case "c-17" -> """
                #include <stdio.h>

                int main() {
                    char name[100];
                    if (fgets(name, sizeof(name), stdin)) {
                        printf("Hello, %s", name);
                    } else {
                        printf("Hello, World!\\n");
                    }
                    return 0;
                }
                """;
            case "cpp-23" -> """
                #include <iostream>
                #include <string>

                int main() {
                    std::string input;
                    if (std::getline(std::cin, input) && !input.empty()) {
                        std::cout << "Hello, " << input << "!" << std::endl;
                    } else {
                        std::cout << "Hello, World!" << std::endl;
                    }
                    return 0;
                }
                """;
            case "python-3.12" -> """
                import sys

                line = sys.stdin.readline().strip()
                if line:
                    print(f"Hello, {line}!")
                else:
                    print("Hello, World!")
                """;
            case "javascript-node-20" -> """
                const fs = require('fs');

                const input = fs.readFileSync(0, 'utf-8').trim();
                if (input) {
                    console.log(`Hello, ${input}!`);
                } else {
                    console.log('Hello, World!');
                }
                """;
            case "mysql-8.0" -> """
                SELECT 'Hello, World!' AS message;
                """;
            case "postgresql-16" -> """
                SELECT 'Hello, World!' AS message;
                """;
            default -> "// Write your code here\n";
        };
    }

    List<String> run(List<String> fileNames);

    default List<String> run(String fileName) {
        return run(fileName != null ? List.of(fileName) : List.of());
    }

    default List<String> run(String... fileNames) {
        return run(fileNames != null ? List.of(fileNames) : List.of());
    }
}
