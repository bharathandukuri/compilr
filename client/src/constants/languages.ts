import type { Language } from "@/types/compiler"

export interface LanguageMeta extends Language {
  shortName: string
  tag: string
  fileName: string
  monacoLanguage: string
  popular?: boolean
  color: string
  bgLight: string
  borderColor: string
}

export const SUPPORTED_LANGUAGES: LanguageMeta[] = [
  {
    id: "python-3.12",
    name: "Python (CPython 3.12)",
    shortName: "Python",
    version: "3.12",
    type: "INTERPRETED",
    fileExtension: ".py",
    fileName: "main.py",
    monacoLanguage: "python",
    compiled: false,
    popular: true,
    tag: "3.12",
    color: "#3776AB",
    bgLight: "bg-blue-500/10",
    borderColor: "border-blue-500/30",
    aliases: ["python", "py", "python3", "python312"],
    defaultStarterCode: `import sys

line = sys.stdin.readline().strip()
if line:
    print(f"Hello, {line}!")
else:
    print("Hello, World!")
`,
  },
  {
    id: "javascript-node-20",
    name: "JavaScript (Node.js 20)",
    shortName: "JavaScript",
    version: "Node.js 20",
    type: "INTERPRETED",
    fileExtension: ".js",
    fileName: "main.js",
    monacoLanguage: "javascript",
    compiled: false,
    popular: true,
    tag: "Node 20",
    color: "#E5A910",
    bgLight: "bg-amber-500/10",
    borderColor: "border-amber-500/30",
    aliases: ["javascript", "js", "node", "nodejs"],
    defaultStarterCode: `const fs = require('fs');

const input = fs.readFileSync(0, 'utf-8').trim();
if (input) {
    console.log(\`Hello, \${input}!\`);
} else {
    console.log('Hello, World!');
}
`,
  },
  {
    id: "java-21",
    name: "Java (OpenJDK 21)",
    shortName: "Java",
    version: "OpenJDK 21",
    type: "COMPILED",
    fileExtension: ".java",
    fileName: "Main.java",
    monacoLanguage: "java",
    compiled: true,
    popular: true,
    tag: "JDK 21",
    color: "#E76F00",
    bgLight: "bg-orange-500/10",
    borderColor: "border-orange-500/30",
    aliases: ["java", "java21"],
    defaultStarterCode: `import java.util.Scanner;

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
`,
  },
  {
    id: "cpp-23",
    name: "C++ (GCC 13.2)",
    shortName: "C++",
    version: "GCC 14",
    type: "COMPILED",
    fileExtension: ".cpp",
    fileName: "main.cpp",
    monacoLanguage: "cpp",
    compiled: true,
    popular: true,
    tag: "GCC 14",
    color: "#00599C",
    bgLight: "bg-sky-500/10",
    borderColor: "border-sky-500/30",
    aliases: ["cpp", "c++", "cpp23"],
    defaultStarterCode: `#include <iostream>
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
`,
  },
  {
    id: "c-17",
    name: "C (GCC 13.2)",
    shortName: "C",
    version: "GCC 14",
    type: "COMPILED",
    fileExtension: ".c",
    fileName: "main.c",
    monacoLanguage: "c",
    compiled: true,
    popular: true,
    tag: "GCC 14",
    color: "#5C6BC0",
    bgLight: "bg-indigo-500/10",
    borderColor: "border-indigo-500/30",
    aliases: ["c", "c17"],
    defaultStarterCode: `#include <stdio.h>

int main() {
    char name[100];
    if (fgets(name, sizeof(name), stdin)) {
        printf("Hello, %s", name);
    } else {
        printf("Hello, World!\\n");
    }
    return 0;
}
`,
  },
  {
    id: "postgresql-16",
    name: "PostgreSQL (16)",
    shortName: "PostgreSQL",
    version: "16",
    type: "DATABASE",
    fileExtension: ".sql",
    fileName: "main.sql",
    monacoLanguage: "sql",
    compiled: false,
    popular: false,
    tag: "v16",
    color: "#336791",
    bgLight: "bg-cyan-500/10",
    borderColor: "border-cyan-500/30",
    aliases: ["postgres", "postgresql", "psql"],
    defaultStarterCode: `SELECT 'Hello, World!' AS message;
`,
  },
  {
    id: "mysql-8.0",
    name: "MySQL (8.0)",
    shortName: "MySQL",
    version: "8.0",
    type: "DATABASE",
    fileExtension: ".sql",
    fileName: "main.sql",
    monacoLanguage: "sql",
    compiled: false,
    popular: false,
    tag: "v8.0",
    color: "#00758F",
    bgLight: "bg-teal-500/10",
    borderColor: "border-teal-500/30",
    aliases: ["mysql", "sql"],
    defaultStarterCode: `SELECT 'Hello, World!' AS message;
`,
  },
  {
    id: "sqlite-3",
    name: "SQLite (3.45)",
    shortName: "SQLite",
    version: "3.45",
    type: "DATABASE",
    fileExtension: ".sql",
    fileName: "main.sql",
    monacoLanguage: "sql",
    compiled: false,
    popular: false,
    tag: "v3.45",
    color: "#003B57",
    bgLight: "bg-sky-600/10",
    borderColor: "border-sky-600/30",
    aliases: ["sqlite", "sqlite3"],
    defaultStarterCode: `CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT);
INSERT INTO users (name) VALUES ('Alice'), ('Bob');
SELECT * FROM users;
`,
  },
  {
    id: "mongodb-8.0",
    name: "MongoDB (8.0)",
    shortName: "MongoDB",
    version: "8.0",
    type: "DATABASE",
    fileExtension: ".js",
    fileName: "main.js",
    monacoLanguage: "javascript",
    compiled: false,
    popular: false,
    tag: "v8.0",
    color: "#47A248",
    bgLight: "bg-emerald-500/10",
    borderColor: "border-emerald-500/30",
    aliases: ["mongodb", "mongo", "nosql"],
    defaultStarterCode: `db.users.insertOne({ name: "Alice", role: "Developer" });
db.users.insertOne({ name: "Bob", role: "Designer" });

printjson(db.users.find().toArray());
`,
  },
  {
    id: "typescript-5.4",
    name: "TypeScript (5.4)",
    shortName: "TypeScript",
    version: "5.4",
    type: "INTERPRETED",
    fileExtension: ".ts",
    fileName: "main.ts",
    monacoLanguage: "typescript",
    compiled: false,
    popular: true,
    tag: "TS 5.4",
    color: "#3178C6",
    bgLight: "bg-blue-600/10",
    borderColor: "border-blue-600/30",
    aliases: ["typescript", "ts"],
    defaultStarterCode: `const greeting: string = "Hello, World!";
console.log(greeting);
`,
  },
  {
    id: "go-1.22",
    name: "Go (1.22)",
    shortName: "Go",
    version: "1.22",
    type: "COMPILED",
    fileExtension: ".go",
    fileName: "main.go",
    monacoLanguage: "go",
    compiled: true,
    popular: true,
    tag: "1.22",
    color: "#00ADD8",
    bgLight: "bg-cyan-500/10",
    borderColor: "border-cyan-500/30",
    aliases: ["go", "golang"],
    defaultStarterCode: `package main

import (
	"bufio"
	"fmt"
	"os"
	"strings"
)

func main() {
	reader := bufio.NewReader(os.Stdin)
	text, err := reader.ReadString('\\n')
	text = strings.TrimSpace(text)
	if err == nil && len(text) > 0 {
		fmt.Printf("Hello, %s!\\n", text)
	} else {
		fmt.Println("Hello, World!")
	}
}
`,
  },
  {
    id: "rust-1.75",
    name: "Rust (1.75)",
    shortName: "Rust",
    version: "1.75",
    type: "COMPILED",
    fileExtension: ".rs",
    fileName: "main.rs",
    monacoLanguage: "rust",
    compiled: true,
    popular: true,
    tag: "1.75",
    color: "#DEA584",
    bgLight: "bg-amber-600/10",
    borderColor: "border-amber-600/30",
    aliases: ["rust", "rs"],
    defaultStarterCode: `use std::io::{self, BufRead};

fn main() {
    let stdin = io::stdin();
    let mut iterator = stdin.lock().lines();
    if let Some(Ok(line)) = iterator.next() {
        if !line.trim().is_empty() {
            println!("Hello, {}!", line.trim());
            return;
        }
    }
    println!("Hello, World!");
}
`,
  },
  {
    id: "kotlin-1.9",
    name: "Kotlin (1.9)",
    shortName: "Kotlin",
    version: "1.9",
    type: "COMPILED",
    fileExtension: ".kt",
    fileName: "Main.kt",
    monacoLanguage: "kotlin",
    compiled: true,
    popular: false,
    tag: "v1.9",
    color: "#7F52FF",
    bgLight: "bg-indigo-500/10",
    borderColor: "border-indigo-500/30",
    aliases: ["kotlin", "kt"],
    defaultStarterCode: `import java.util.Scanner

fun main() {
    val scanner = Scanner(System.\`in\`)
    if (scanner.hasNextLine()) {
        val line = scanner.nextLine()
        println("Hello, $line!")
    } else {
        println("Hello, World!")
    }
}
`,
  },
]

export function getLanguageMeta(
  langOrId?: Language | string | null
): LanguageMeta {
  const id = typeof langOrId === "string" ? langOrId : langOrId?.id
  if (!id) return SUPPORTED_LANGUAGES[0]

  const found = SUPPORTED_LANGUAGES.find(
    (l) =>
      l.id === id ||
      l.shortName.toLowerCase() === id.toLowerCase() ||
      l.aliases.includes(id.toLowerCase())
  )

  if (found) return found

  // Fallback for custom or unknown backend languages
  const isMongo = id.toLowerCase().includes("mongo")
  return {
    id: typeof langOrId === "object" && langOrId ? langOrId.id : id,
    name: typeof langOrId === "object" && langOrId ? langOrId.name : id,
    shortName:
      typeof langOrId === "object" && langOrId
        ? langOrId.name.split(" ")[0]
        : id,
    version: typeof langOrId === "object" && langOrId ? langOrId.version : "",
    type:
      typeof langOrId === "object" && langOrId ? langOrId.type : "INTERPRETED",
    fileExtension:
      typeof langOrId === "object" && langOrId
        ? langOrId.fileExtension
        : isMongo
        ? ".js"
        : ".txt",
    fileName:
      typeof langOrId === "object" && langOrId
        ? `main${langOrId.fileExtension}`
        : isMongo
        ? "main.js"
        : "main.txt",
    monacoLanguage: isMongo ? "javascript" : "plaintext",
    compiled: false,
    tag: "",
    color: isMongo ? "#47A248" : "#64748B",
    bgLight: isMongo ? "bg-emerald-500/10" : "bg-muted/40",
    borderColor: isMongo ? "border-emerald-500/30" : "border-border",
    aliases: [],
    defaultStarterCode:
      typeof langOrId === "object" && langOrId
        ? langOrId.defaultStarterCode
        : "",
  }
}
