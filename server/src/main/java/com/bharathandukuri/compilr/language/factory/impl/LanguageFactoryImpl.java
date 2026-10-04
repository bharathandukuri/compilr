package com.bharathandukuri.compilr.language.factory.impl;

import com.bharathandukuri.compilr.language.CompiledLanguage;
import com.bharathandukuri.compilr.language.DatabaseLanguage;
import com.bharathandukuri.compilr.language.InterpretedLanguage;
import com.bharathandukuri.compilr.language.Language;
import com.bharathandukuri.compilr.language.exception.LanguageNotFoundException;
import com.bharathandukuri.compilr.language.factory.LanguageFactory;
import com.bharathandukuri.compilr.language.impl.DefaultCompiledLanguage;
import com.bharathandukuri.compilr.language.impl.DefaultDatabaseLanguage;
import com.bharathandukuri.compilr.language.impl.DefaultInterpretedLanguage;
import com.bharathandukuri.compilr.execution.registry.DockerImageRegistry;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
public class LanguageFactoryImpl implements LanguageFactory {

    public static final String ID_JAVA_21 = "java-21";
    public static final String ID_C_17 = "c-17";
    public static final String ID_CPP_23 = "cpp-23";
    public static final String ID_PYTHON_3_12 = "python-3.12";
    public static final String ID_JAVASCRIPT_NODE_20 = "javascript-node-20";
    public static final String ID_TYPESCRIPT_5_4 = "typescript-5.4";
    public static final String ID_GO_1_22 = "go-1.22";
    public static final String ID_RUST_1_75 = "rust-1.75";
    public static final String ID_PHP_8_3 = "php-8.3";
    public static final String ID_CSHARP_12 = "csharp-12";
    public static final String ID_KOTLIN_1_9 = "kotlin-1.9";
    public static final String ID_DART_3_4 = "dart-3.4";
    public static final String ID_MYSQL_8_0 = "mysql-8.0";
    public static final String ID_POSTGRESQL_16 = "postgresql-16";
    public static final String ID_SQLITE_3 = "sqlite-3";
    public static final String ID_MONGODB_8_0 = "mongodb-8.0";

    private static final List<String> SUPPORTED_IDS = List.of(
            ID_JAVA_21,
            ID_C_17,
            ID_CPP_23,
            ID_PYTHON_3_12,
            ID_JAVASCRIPT_NODE_20,
            ID_TYPESCRIPT_5_4,
            ID_GO_1_22,
            ID_RUST_1_75,
            ID_PHP_8_3,
            ID_CSHARP_12,
            ID_KOTLIN_1_9,
            ID_DART_3_4,
            ID_MYSQL_8_0,
            ID_POSTGRESQL_16,
            ID_SQLITE_3,
            ID_MONGODB_8_0
    );

    @Override
    public Language create(String languageId) {
        if (languageId == null || languageId.isBlank()) {
            throw new LanguageNotFoundException("Language ID must not be null or blank.");
        }

        return switch (languageId.trim().toLowerCase()) {
            case ID_JAVA_21, "java" -> createJava21();
            case ID_C_17, "c" -> createC17();
            case ID_CPP_23, "cpp", "c++" -> createCpp23();
            case ID_PYTHON_3_12, "python", "py" -> createPython312();
            case ID_JAVASCRIPT_NODE_20, "javascript", "js", "node" -> createJavaScriptNode20();
            case ID_TYPESCRIPT_5_4, "typescript", "ts", "typescript-node" -> createTypeScript54();
            case ID_GO_1_22, "go", "golang" -> createGo122();
            case ID_RUST_1_75, "rust", "rs" -> createRust175();
            case ID_PHP_8_3, "php" -> createPhp83();
            case ID_CSHARP_12, "csharp", "c#", "cs", "dotnet" -> createCsharp12();
            case ID_KOTLIN_1_9, "kotlin", "kt" -> createKotlin19();
            case ID_DART_3_4, "dart" -> createDart34();
            case ID_MYSQL_8_0, "mysql" -> createMySql80();
            case ID_POSTGRESQL_16, "postgresql", "postgres" -> createPostgreSql16();
            case ID_SQLITE_3, "sqlite", "sqlite3" -> createSqlite3();
            case ID_MONGODB_8_0, "mongodb", "mongo" -> createMongoDb80();
            default -> throw new LanguageNotFoundException(languageId);
        };
    }

    @Override
    public CompiledLanguage createCompiled(
            String id,
            String name,
            String fileExtension,
            DockerImageRegistry dockerImage,
            Function<List<String>, List<String>> compileCommandGenerator,
            Function<List<String>, List<String>> runCommandGenerator
    ) {
        return new DefaultCompiledLanguage(
                id,
                name,
                fileExtension,
                dockerImage,
                compileCommandGenerator,
                runCommandGenerator
        );
    }

    @Override
    public InterpretedLanguage createInterpreted(
            String id,
            String name,
            String fileExtension,
            DockerImageRegistry dockerImage,
            Function<List<String>, List<String>> runCommandGenerator
    ) {
        return new DefaultInterpretedLanguage(
                id,
                name,
                fileExtension,
                dockerImage,
                runCommandGenerator
        );
    }

    @Override
    public DatabaseLanguage createDatabase(
            String id,
            String name,
            String fileExtension,
            DockerImageRegistry dockerImage,
            Function<List<String>, List<String>> runCommandGenerator
    ) {
        return new DefaultDatabaseLanguage(
                id,
                name,
                fileExtension,
                dockerImage,
                runCommandGenerator
        );
    }

    @Override
    public List<String> supportedLanguageIds() {
        return SUPPORTED_IDS;
    }

    // =========================================================================
    // Built-in Language Creators
    // =========================================================================

    private CompiledLanguage createJava21() {
        return createCompiled(
                ID_JAVA_21,
                "Java (OpenJDK 21)",
                ".java",
                DockerImageRegistry.JAVA_21,
                files -> {
                    List<String> effectiveFiles = (files == null || files.isEmpty())
                            ? List.of("Main.java")
                            : files;
                    List<String> command = new ArrayList<>();
                    command.add("javac");
                    command.add("-encoding");
                    command.add("UTF-8");
                    command.addAll(effectiveFiles);
                    return command;
                },
                files -> {
                    String mainFile = (files == null || files.isEmpty())
                            ? "Main.java"
                            : files.get(0);
                    String className = extractBaseName(mainFile);
                    return List.of("java", "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8", "-cp", ".", className);
                }
        );
    }

    private CompiledLanguage createC17() {
        return createCompiled(
                ID_C_17,
                "C (GCC 13.2)",
                ".c",
                DockerImageRegistry.C_17,
                files -> {
                    List<String> effectiveFiles = (files == null || files.isEmpty())
                            ? List.of("main.c")
                            : files;
                    List<String> command = new ArrayList<>();
                    command.add("gcc");
                    command.add("-std=c17");
                    command.add("-O2");
                    command.add("-Wall");
                    command.addAll(effectiveFiles);
                    command.add("-o");
                    command.add("a.out");
                    command.add("-lm");
                    return command;
                },
                files -> List.of("./a.out")
        );
    }

    private CompiledLanguage createCpp23() {
        return createCompiled(
                ID_CPP_23,
                "C++ (GCC 13.2)",
                ".cpp",
                DockerImageRegistry.CPP_23,
                files -> {
                    List<String> effectiveFiles = (files == null || files.isEmpty())
                            ? List.of("main.cpp")
                            : files;
                    List<String> command = new ArrayList<>();
                    command.add("g++");
                    command.add("-std=c++23");
                    command.add("-O2");
                    command.add("-Wall");
                    command.addAll(effectiveFiles);
                    command.add("-o");
                    command.add("a.out");
                    return command;
                },
                files -> List.of("./a.out")
        );
    }

    private InterpretedLanguage createPython312() {
        return createInterpreted(
                ID_PYTHON_3_12,
                "Python (CPython 3.12)",
                ".py",
                DockerImageRegistry.PYTHON_3_12,
                files -> {
                    String scriptFile = (files == null || files.isEmpty())
                            ? "main.py"
                            : files.get(0);
                    return List.of("python3", scriptFile);
                }
        );
    }

    private InterpretedLanguage createJavaScriptNode20() {
        return createInterpreted(
                ID_JAVASCRIPT_NODE_20,
                "JavaScript (Node.js 20)",
                ".js",
                DockerImageRegistry.JAVASCRIPT_NODE_20,
                files -> {
                    String scriptFile = (files == null || files.isEmpty())
                            ? "main.js"
                            : files.get(0);
                    return List.of("node", scriptFile);
                }
        );
    }

    private DatabaseLanguage createMySql80() {
        return createDatabase(
                ID_MYSQL_8_0,
                "MySQL (8.0)",
                ".sql",
                DockerImageRegistry.MYSQL_8_0,
                files -> {
                    String scriptFile = (files == null || files.isEmpty())
                            ? "main.sql"
                            : files.get(0);
                    return List.of("mysql", "-S", "/var/run/mysqld/mysqld.sock", "stacked_judge_db", "-e", "source " + scriptFile);
                }
        );
    }

    private DatabaseLanguage createPostgreSql16() {
        return createDatabase(
                ID_POSTGRESQL_16,
                "PostgreSQL (16)",
                ".sql",
                DockerImageRegistry.POSTGRES_16,
                files -> {
                    String scriptFile = (files == null || files.isEmpty())
                            ? "main.sql"
                            : files.get(0);
                    return List.of("psql", "-U", "postgres", "-d", "stacked_judge_db", "-f", scriptFile);
                }
        );
    }

    private InterpretedLanguage createTypeScript54() {
        return createInterpreted(
                ID_TYPESCRIPT_5_4,
                "TypeScript (5.4)",
                ".ts",
                DockerImageRegistry.TYPESCRIPT_5_4,
                files -> {
                    String scriptFile = (files == null || files.isEmpty())
                            ? "main.ts"
                            : files.get(0);
                    return List.of("tsx", scriptFile);
                }
        );
    }

    private CompiledLanguage createGo122() {
        return createCompiled(
                ID_GO_1_22,
                "Go (1.22)",
                ".go",
                DockerImageRegistry.GO_1_22,
                files -> {
                    List<String> effectiveFiles = (files == null || files.isEmpty())
                            ? List.of("main.go")
                            : files;
                    List<String> command = new ArrayList<>();
                    command.add("go");
                    command.add("build");
                    command.add("-o");
                    command.add("main");
                    command.addAll(effectiveFiles);
                    return command;
                },
                files -> List.of("./main")
        );
    }

    private CompiledLanguage createRust175() {
        return createCompiled(
                ID_RUST_1_75,
                "Rust (1.75)",
                ".rs",
                DockerImageRegistry.RUST_1_75,
                files -> {
                    List<String> effectiveFiles = (files == null || files.isEmpty())
                            ? List.of("main.rs")
                            : files;
                    List<String> command = new ArrayList<>();
                    command.add("rustc");
                    command.add("-O");
                    command.addAll(effectiveFiles);
                    command.add("-o");
                    command.add("main");
                    return command;
                },
                files -> List.of("./main")
        );
    }

    private InterpretedLanguage createPhp83() {
        return createInterpreted(
                ID_PHP_8_3,
                "PHP (8.3)",
                ".php",
                DockerImageRegistry.PHP_8_3,
                files -> {
                    String scriptFile = (files == null || files.isEmpty())
                            ? "main.php"
                            : files.get(0);
                    return List.of("php", scriptFile);
                }
        );
    }

    private CompiledLanguage createCsharp12() {
        return createCompiled(
                ID_CSHARP_12,
                "C# (.NET 8.0)",
                ".cs",
                DockerImageRegistry.CSHARP_12,
                files -> {
                    String file = (files == null || files.isEmpty()) ? "Program.cs" : files.get(0);
                    return List.of("bash", "-c", "dotnet new console --force -n App >/dev/null 2>&1 && cp " + file + " App/Program.cs && dotnet build App -c Release -o out --nologo");
                },
                files -> List.of("./out/App")
        );
    }

    private CompiledLanguage createKotlin19() {
        return createCompiled(
                ID_KOTLIN_1_9,
                "Kotlin (1.9)",
                ".kt",
                DockerImageRegistry.KOTLIN_1_9,
                files -> {
                    List<String> effectiveFiles = (files == null || files.isEmpty())
                            ? List.of("Main.kt")
                            : files;
                    List<String> command = new ArrayList<>();
                    command.add("kotlinc");
                    command.addAll(effectiveFiles);
                    command.add("-include-runtime");
                    command.add("-d");
                    command.add("Main.jar");
                    return command;
                },
                files -> List.of("java", "-jar", "Main.jar")
        );
    }

    private InterpretedLanguage createDart34() {
        return createInterpreted(
                ID_DART_3_4,
                "Dart (3.4)",
                ".dart",
                DockerImageRegistry.DART_3_4,
                files -> {
                    String scriptFile = (files == null || files.isEmpty())
                            ? "main.dart"
                            : files.get(0);
                    return List.of("dart", "run", scriptFile);
                }
        );
    }

    private DatabaseLanguage createSqlite3() {
        return createDatabase(
                ID_SQLITE_3,
                "SQLite (3.45)",
                ".sql",
                DockerImageRegistry.SQLITE_3,
                files -> {
                    String scriptFile = (files == null || files.isEmpty())
                            ? "main.sql"
                            : files.get(0);
                    return List.of("sqlite3", "-header", "-column", "/tmp/stacked_judge.db", ".read " + scriptFile);
                }
        );
    }

    private DatabaseLanguage createMongoDb80() {
        return createDatabase(
                ID_MONGODB_8_0,
                "MongoDB (8.0)",
                ".js",
                DockerImageRegistry.MONGODB_8_0,
                files -> {
                    String scriptFile = (files == null || files.isEmpty())
                            ? "main.js"
                            : files.get(0);
                    return List.of("mongosh", "--quiet", "--norc", "mongodb://127.0.0.1:27017/stacked_judge_db", "--file", scriptFile);
                }
        );
    }

    private String extractBaseName(String filePath) {
        String fileName = new File(filePath).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }
}
