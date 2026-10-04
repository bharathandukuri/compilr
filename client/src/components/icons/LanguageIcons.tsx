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
import mysqlSvg from "devicon/icons/mysql/mysql-original.svg"
import postgresSvg from "devicon/icons/postgresql/postgresql-original.svg"
import sqliteSvg from "devicon/icons/sqlite/sqlite-original.svg"

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

  // 6. C
  if (normalized === "c" || normalized.startsWith("c-") || normalized === "gcc") {
    return DEVICON_MAP.c
  }

  // 7. SQLite
  if (normalized.includes("sqlite") || normalized.includes("sqlite3")) {
    return DEVICON_MAP.sqlite
  }

  // 8. PostgreSQL
  if (normalized.includes("postgres") || normalized.includes("psql")) {
    return DEVICON_MAP.postgresql
  }

  // 9. MySQL
  if (normalized.includes("mysql") || normalized === "sql") {
    return DEVICON_MAP.mysql
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
