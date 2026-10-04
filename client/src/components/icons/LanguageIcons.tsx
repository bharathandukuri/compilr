import React from "react"
import { Code2 } from "lucide-react"
import { cn } from "@/lib/utils"

// Official Devicon colored SVG icon imports from the installed devicon package
import javaSvg from "devicon/icons/java/java-original.svg"
import cSvg from "devicon/icons/c/c-original.svg"
import cppSvg from "devicon/icons/cplusplus/cplusplus-original.svg"
import pythonSvg from "devicon/icons/python/python-original.svg"
import jsSvg from "devicon/icons/javascript/javascript-original.svg"
import tsSvg from "devicon/icons/typescript/typescript-original.svg"
import csharpSvg from "devicon/icons/csharp/csharp-original.svg"
import kotlinSvg from "devicon/icons/kotlin/kotlin-original.svg"
import goSvg from "devicon/icons/go/go-original.svg"
import rustSvg from "devicon/icons/rust/rust-original.svg"
import dartSvg from "devicon/icons/dart/dart-original.svg"
import phpSvg from "devicon/icons/php/php-original.svg"
import mysqlSvg from "devicon/icons/mysql/mysql-original.svg"
import postgresSvg from "devicon/icons/postgresql/postgresql-original.svg"
import sqliteSvg from "devicon/icons/sqlite/sqlite-original.svg"
import mongodbSvg from "@/assets/mongodb.svg"

export interface DeviconDetails {
  name: string
  svg: string
  deviconClass: string
}

export const DEVICON_MAP: Record<string, DeviconDetails> = {
  python: {
    name: "Python",
    svg: pythonSvg,
    deviconClass: "devicon-python-plain colored",
  },
  javascript: {
    name: "JavaScript",
    svg: jsSvg,
    deviconClass: "devicon-javascript-plain colored",
  },
  typescript: {
    name: "TypeScript",
    svg: tsSvg,
    deviconClass: "devicon-typescript-plain colored",
  },
  java: {
    name: "Java",
    svg: javaSvg,
    deviconClass: "devicon-java-plain colored",
  },
  cpp: {
    name: "C++",
    svg: cppSvg,
    deviconClass: "devicon-cplusplus-plain colored",
  },
  c: {
    name: "C",
    svg: cSvg,
    deviconClass: "devicon-c-plain colored",
  },
  csharp: {
    name: "C#",
    svg: csharpSvg,
    deviconClass: "devicon-csharp-plain colored",
  },
  kotlin: {
    name: "Kotlin",
    svg: kotlinSvg,
    deviconClass: "devicon-kotlin-plain colored",
  },
  go: {
    name: "Go",
    svg: goSvg,
    deviconClass: "devicon-go-original-wordmark colored",
  },
  rust: {
    name: "Rust",
    svg: rustSvg,
    deviconClass: "devicon-rust-plain colored",
  },
  dart: {
    name: "Dart",
    svg: dartSvg,
    deviconClass: "devicon-dart-plain colored",
  },
  php: {
    name: "PHP",
    svg: phpSvg,
    deviconClass: "devicon-php-plain colored",
  },
  mysql: {
    name: "MySQL",
    svg: mysqlSvg,
    deviconClass: "devicon-mysql-original colored",
  },
  postgresql: {
    name: "PostgreSQL",
    svg: postgresSvg,
    deviconClass: "devicon-postgresql-plain colored",
  },
  sqlite: {
    name: "SQLite",
    svg: sqliteSvg,
    deviconClass: "devicon-sqlite-plain colored",
  },
  mongodb: {
    name: "MongoDB",
    svg: mongodbSvg,
    deviconClass: "devicon-mongodb-plain colored",
  },
}

/**
 * Centrally maps any language identifier, alias, or display name to its official Devicon definition.
 */
export function resolveDevicon(language?: string | null): DeviconDetails | null {
  if (!language) return null
  const normalized = language.trim().toLowerCase()

  // 1. Python
  if (normalized.includes("python") || normalized === "py") {
    return DEVICON_MAP.python
  }

  // 2. TypeScript
  if (normalized.includes("typescript") || normalized === "ts") {
    return DEVICON_MAP.typescript
  }

  // 3. JavaScript
  if (
    normalized.includes("javascript") ||
    normalized.includes("node") ||
    normalized === "js"
  ) {
    return DEVICON_MAP.javascript
  }

  // 4. Java
  if (
    normalized.startsWith("java-") ||
    normalized === "java" ||
    normalized.includes("openjdk")
  ) {
    return DEVICON_MAP.java
  }

  // 5. C++
  if (
    normalized.includes("cpp") ||
    normalized.includes("c++") ||
    normalized.includes("cplusplus")
  ) {
    return DEVICON_MAP.cpp
  }

  // 6. C#
  if (
    normalized.includes("csharp") ||
    normalized.includes("c#") ||
    normalized.includes("dotnet") ||
    normalized === "cs"
  ) {
    return DEVICON_MAP.csharp
  }

  // 7. C
  if (normalized === "c" || normalized.startsWith("c-") || normalized === "gcc") {
    return DEVICON_MAP.c
  }

  // 8. Kotlin
  if (normalized.includes("kotlin") || normalized === "kt") {
    return DEVICON_MAP.kotlin
  }

  // 9. Go
  if (normalized.includes("go") || normalized.includes("golang")) {
    return DEVICON_MAP.go
  }

  // 10. Rust
  if (normalized.includes("rust") || normalized === "rs") {
    return DEVICON_MAP.rust
  }

  // 11. Dart
  if (normalized.includes("dart")) {
    return DEVICON_MAP.dart
  }

  // 12. PHP
  if (normalized.includes("php")) {
    return DEVICON_MAP.php
  }

  // 13. SQLite
  if (normalized.includes("sqlite") || normalized.includes("sqlite3")) {
    return DEVICON_MAP.sqlite
  }

  // 14. PostgreSQL
  if (normalized.includes("postgres") || normalized.includes("psql")) {
    return DEVICON_MAP.postgresql
  }

  // 15. MySQL
  if (normalized.includes("mysql") || normalized === "sql") {
    return DEVICON_MAP.mysql
  }

  // 16. MongoDB
  if (
    normalized.includes("mongo") ||
    normalized.includes("mongodb") ||
    normalized === "nosql"
  ) {
    return DEVICON_MAP.mongodb
  }

  return null
}

export interface LanguageIconProps extends React.ImgHTMLAttributes<HTMLImageElement> {
  languageId?: string | null
  language?: string | null
  className?: string
  alt?: string
}

/**
 * Centrally renders official colored Devicon icons for programming languages and databases.
 * Accepts either `languageId` (e.g. "python-3.12") or `language` (e.g. "JAVA").
 */
export const LanguageIcon: React.FC<LanguageIconProps> = ({
  languageId,
  language,
  className = "h-4 w-4",
  alt,
  ...props
}) => {
  const target = language || languageId
  const normalized = (target || "").trim().toLowerCase()

  // Directly render the official modern two-tone MongoDB leaf SVG
  if (
    normalized.includes("mongo") ||
    normalized.includes("mongodb") ||
    normalized === "nosql"
  ) {
    return <MongoDbIcon className={className} {...(props as React.SVGProps<SVGSVGElement>)} />
  }

  const devicon = resolveDevicon(target)

  if (!devicon) {
    return (
      <Code2
        className={cn("h-4 w-4 shrink-0 text-muted-foreground", className)}
        aria-hidden="true"
      />
    )
  }

  return (
    <img
      src={devicon.svg}
      alt={alt || `${devicon.name} icon`}
      className={cn(
        "inline-block shrink-0 select-none object-contain",
        devicon.deviconClass,
        className
      )}
      loading="lazy"
      draggable={false}
      {...props}
    />
  )
}

// Individual language icon wrappers for backwards-compatibility
export const PythonIcon: React.FC<Omit<LanguageIconProps, "language" | "languageId">> = (props) => (
  <LanguageIcon language="python" {...props} />
)
export const JavaIcon: React.FC<Omit<LanguageIconProps, "language" | "languageId">> = (props) => (
  <LanguageIcon language="java" {...props} />
)
export const JavaScriptIcon: React.FC<Omit<LanguageIconProps, "language" | "languageId">> = (props) => (
  <LanguageIcon language="javascript" {...props} />
)
export const TypeScriptIcon: React.FC<Omit<LanguageIconProps, "language" | "languageId">> = (props) => (
  <LanguageIcon language="typescript" {...props} />
)
export const CppIcon: React.FC<Omit<LanguageIconProps, "language" | "languageId">> = (props) => (
  <LanguageIcon language="cpp" {...props} />
)
export const CIcon: React.FC<Omit<LanguageIconProps, "language" | "languageId">> = (props) => (
  <LanguageIcon language="c" {...props} />
)
export const MySqlIcon: React.FC<Omit<LanguageIconProps, "language" | "languageId">> = (props) => (
  <LanguageIcon language="mysql" {...props} />
)
export const PostgresIcon: React.FC<Omit<LanguageIconProps, "language" | "languageId">> = (props) => (
  <LanguageIcon language="postgresql" {...props} />
)
export const SqliteIcon: React.FC<Omit<LanguageIconProps, "language" | "languageId">> = (props) => (
  <LanguageIcon language="sqlite" {...props} />
)
export const MongoDbIcon: React.FC<React.SVGProps<SVGSVGElement>> = ({
  className = "h-4 w-4",
  ...props
}) => (
  <svg
    viewBox="0 0 32 32"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    className={cn("inline-block shrink-0 select-none object-contain", className)}
    aria-label="MongoDB"
    {...props}
  >
    {/* Left leaf lobe (Vibrant Spring Green) */}
    <path
      d="M15.9 0.034c-.035-.07-.07-.017-.105.017.017.35-.105.662-.296.96-.21.296-.488.523-.767.767-1.55 1.342-2.77 2.963-3.747 4.776-1.3 2.44-1.97 5.055-2.16 7.808-.087.993.314 4.497.627 5.508.854 2.684 2.388 4.933 4.375 6.885.488.47 1.01.906 1.55 1.325.157 0 .174-.14.21-.244a4.78 4.78 0 0 0 .157-.68l.35-2.614L15.9.034z"
      fill="#00ED64"
    />
    {/* Right leaf lobe (Forest Green) */}
    <path
      d="M15.9 0.087l.854 1.604c.192.296.4.558.645.802.715.715 1.394 1.464 2.004 2.266 1.447 1.9 2.423 4.01 3.12 6.292.418 1.394.645 2.824.662 4.27.07 4.323-1.412 8.035-4.4 11.12-.488.488-1.01.94-1.57 1.342-.296 0-.436-.227-.558-.436-.227-.383-.366-.82-.436-1.255-.105-.523-.174-1.046-.14-1.586v-.244C16.057 24.21 15.796.21 15.9.087z"
      fill="#00684A"
    />
    {/* Center stem / root accent */}
    <path
      d="M16.754 28.845c.035-.4.227-.732.436-1.063-.21-.087-.366-.26-.488-.453-.105-.174-.192-.383-.26-.575-.244-.732-.296-1.5-.366-2.248v-.453c-.087.07-.105.662-.105.75a17.37 17.37 0 0 1-.314 2.353c-.052.314-.087.627-.28.906 0 .035 0 .07.017.122.314.924.4 1.865.453 2.824v.35c0 .418-.017.33.33.47.14.052.296.07.436.174.105 0 .122-.087.122-.157l-.052-.575v-1.604c-.017-.28.035-.558.07-.82z"
      fill="#A4ACB0"
    />
  </svg>
)
export const MongodbIcon = MongoDbIcon
export const GoIcon: React.FC<Omit<LanguageIconProps, "language" | "languageId">> = (props) => (
  <LanguageIcon language="go" {...props} />
)
export const RustIcon: React.FC<Omit<LanguageIconProps, "language" | "languageId">> = (props) => (
  <LanguageIcon language="rust" {...props} />
)
export const DartIcon: React.FC<Omit<LanguageIconProps, "language" | "languageId">> = (props) => (
  <LanguageIcon language="dart" {...props} />
)
export const PhpIcon: React.FC<Omit<LanguageIconProps, "language" | "languageId">> = (props) => (
  <LanguageIcon language="php" {...props} />
)
export const CsharpIcon: React.FC<Omit<LanguageIconProps, "language" | "languageId">> = (props) => (
  <LanguageIcon language="csharp" {...props} />
)
export const KotlinIcon: React.FC<Omit<LanguageIconProps, "language" | "languageId">> = (props) => (
  <LanguageIcon language="kotlin" {...props} />
)
