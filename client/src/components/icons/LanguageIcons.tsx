import React from "react"
import { Code2 } from "lucide-react"

interface IconProps extends React.SVGProps<SVGSVGElement> {
  className?: string
}

// 1. Python (Classic Blue & Yellow)
export const PythonIcon: React.FC<IconProps> = ({ className = "h-4 w-4", ...props }) => (
  <svg
    viewBox="0 0 24 24"
    fill="currentColor"
    className={className}
    aria-label="Python"
    {...props}
  >
    <path
      fill="#3776AB"
      d="M11.91 2c-5.06 0-4.74 2.19-4.74 2.19l.01 2.27h4.82v.69H5.16S2 6.8 2 11.95c0 5.16 2.76 4.98 2.76 4.98h1.65v-2.33s-.09-2.76 2.72-2.76h4.69v-.71h-4.7V6.52s.1-2.48 4.79-2.48h2.98s2.61.1 2.61-2.04H11.91zM9.46 3.65a.69.69 0 1 1 0 1.38.69.69 0 0 1 0-1.38z"
    />
    <path
      fill="#FFD43B"
      d="M12.09 22c5.06 0 4.74-2.19 4.74-2.19l-.01-2.27h-4.82v-.69h6.84S22 17.2 22 12.05c0-5.16-2.76-4.98-2.76-4.98h-1.65v2.33s.09 2.76-2.72 2.76h-4.69v.71h4.7v2.61s-.1 2.48-4.79 2.48h-2.98s-2.61-.1-2.61 2.04H12.09zM14.54 20.35a.69.69 0 1 1 0-1.38.69.69 0 0 1 0 1.38z"
    />
  </svg>
)

// 2. JavaScript (Official JS Shield/Tile)
export const JavaScriptIcon: React.FC<IconProps> = ({ className = "h-4 w-4", ...props }) => (
  <svg
    viewBox="0 0 24 24"
    className={className}
    aria-label="JavaScript"
    {...props}
  >
    <rect width="24" height="24" rx="3" fill="#F7DF1E" />
    <path
      fill="#000000"
      d="M6.5 17.5l2.2-.6c.2.6.5 1 .9 1.3.4.2 1 .4 1.7.4.8 0 1.4-.2 1.8-.5.4-.3.6-.8.6-1.3 0-.4-.1-.7-.4-.9-.2-.2-.7-.4-1.4-.6l-1.3-.4c-1.3-.4-2-1-2.3-1.4-.3-.5-.5-1.1-.5-1.8 0-1 .4-1.9 1.1-2.5.7-.6 1.7-.9 3-.9 1.3 0 2.3.3 3 .9.7.6 1.1 1.4 1.2 2.3l-2.2.5c-.1-.5-.4-.9-.7-1.1-.3-.2-.8-.4-1.3-.4-.6 0-1.1.1-1.4.4-.3.2-.5.6-.5 1 0 .3.1.6.3.8.2.2.7.4 1.5.7l1.1.4c1.4.5 2.2 1 2.6 1.6.4.6.6 1.3.6 2.1 0 1.1-.4 2-1.2 2.7-.8.7-1.9 1-3.3 1-1.7 0-3-.4-3.8-1.2-.8-.9-1.2-1.9-1.2-3.1zm10.5-9.3v8.5c0 1.1-.3 1.9-.8 2.4-.6.5-1.4.8-2.6.8-.7 0-1.4-.1-2-.4v-2c.4.2.9.3 1.3.3.6 0 1-.1 1.2-.4.2-.2.3-.6.3-1.1V8.2h2.6z"
    />
  </svg>
)

// 3. Java (Classic Duke/Coffee Cup with steam)
export const JavaIcon: React.FC<IconProps> = ({ className = "h-4 w-4", ...props }) => (
  <svg
    viewBox="0 0 24 24"
    fill="currentColor"
    className={className}
    aria-label="Java"
    {...props}
  >
    <path
      fill="#E76F00"
      d="M10.9 2.5c.3 1.2-.3 2.4-1.2 3.1-.7.6-1.5.9-2.3 1.4-.4.2-.7.6-.8 1.1-.2 1.1.5 2.1 1.6 2.3.2 0 .4 0 .6-.1 1.2-.4 2.2-1.2 2.9-2.2.7-1 1-2.3.8-3.6 0-.7-.3-1.4-.7-2-.3-.3-.7 0-.9 0zm4.2 3.8c-.3.7-.8 1.4-1.4 1.9-.6.5-1.3.9-2 1.3-.4.2-.7.6-.8 1.1-.1.9.4 1.8 1.3 2.1.2 0 .3 0 .5-.1 1-.4 1.8-1.1 2.4-1.9.6-.8.8-1.9.7-2.9 0-.6-.2-1.1-.5-1.5h-.2z"
    />
    <path
      fill="#5382A1"
      d="M18.8 16.5c-.1-.1-.7.4-1.6.7-1.3.5-3.3.9-5.5.9s-4.2-.4-5.5-.9c-.7-.3-1.4-.7-1.5-.7-.3 0-.4.3-.2.5.7 1 2.8 1.9 5.2 2.1-.8.6-1.9 1.4-1.9 1.9 0 .4.4.7 1.1.7 1.6 0 3.7-1 5.3-2.2 1.9-.3 3.6-.9 4.3-1.8.2-.4.1-.9-.2-1.2z"
    />
    <path
      fill="#5382A1"
      d="M18.5 14.3c-2 .7-4.4 1.1-6.8 1.1s-4.8-.4-6.8-1.1c-1.1-.4-1.2-.8-1.2-.9 0-.2.4-.4.9-.6 1.8-.7 4.3-1.1 7.1-1.1s5.3.4 7.1 1.1c.6.2.9.4.9.6 0 .1-.2.5-1.2.9z"
    />
  </svg>
)

// 4. C++ (Hexagonal C++ Blue)
export const CppIcon: React.FC<IconProps> = ({ className = "h-4 w-4", ...props }) => (
  <svg
    viewBox="0 0 24 24"
    className={className}
    aria-label="C++"
    {...props}
  >
    <path
      fill="#00599C"
      d="M12 2L2 7.7v11.6L12 25l10-5.7V7.7L12 2zm0 2.3l8 4.6v9.2l-8 4.6-8-4.6V8.9l8-4.6z"
    />
    <path
      fill="#004482"
      d="M11 9.5a4.5 4.5 0 0 0-3.2 1.3 4.5 4.5 0 0 0 0 6.4 4.5 4.5 0 0 0 3.2 1.3c1.2 0 2.2-.4 3-1.2l-1.4-1.4c-.4.4-1 .6-1.6.6-1.4 0-2.5-1.1-2.5-2.5s1.1-2.5 2.5-2.5c.6 0 1.2.2 1.6.6l1.4-1.4c-.8-.8-1.8-1.2-3-1.2zm5 3.5h-1v1h-1v1h1v1h1v-1h1v-1h-1v-1zm3 0h-1v1h-1v1h1v1h1v-1h1v-1h-1v-1z"
    />
  </svg>
)

// 5. C (Hexagonal C Blue/Indigo)
export const CIcon: React.FC<IconProps> = ({ className = "h-4 w-4", ...props }) => (
  <svg
    viewBox="0 0 24 24"
    className={className}
    aria-label="C"
    {...props}
  >
    <path
      fill="#A8B9CC"
      d="M12 2L2 7.7v11.6L12 25l10-5.7V7.7L12 2zm0 2.3l8 4.6v9.2l-8 4.6-8-4.6V8.9l8-4.6z"
    />
    <path
      fill="#283593"
      d="M12.5 8.5c-2.8 0-5 2.2-5 5s2.2 5 5 5c1.8 0 3.3-.9 4.2-2.3l-2.1-1.2c-.5.7-1.3 1.2-2.1 1.2-1.5 0-2.7-1.2-2.7-2.7s1.2-2.7 2.7-2.7c.8 0 1.6.5 2.1 1.2l2.1-1.2c-.9-1.4-2.4-2.3-4.2-2.3z"
    />
  </svg>
)

// 6. PostgreSQL (Elephant Slon Silhouette)
export const PostgresIcon: React.FC<IconProps> = ({ className = "h-4 w-4", ...props }) => (
  <svg
    viewBox="0 0 24 24"
    fill="currentColor"
    className={className}
    aria-label="PostgreSQL"
    {...props}
  >
    <path
      fill="#336791"
      d="M12.2 2c-3.1 0-5.8 2.2-6.5 5.3-.2 1.1-.2 2.2 0 3.3-.6.6-1.2 1.4-1.6 2.3-.4 1-.5 2-.4 3.1.2 1.3 1 2.5 2.2 3.1 1.2.7 2.7.7 4 .2.4-.2.8-.4 1.2-.7 1.4.9 3.1 1.4 4.8 1.4 4.4 0 8-3.6 8-8s-3.6-8-8-8c-1.2 0-2.5.3-3.7.8z"
    />
    <path
      fill="#FFFFFF"
      d="M15.5 10c.8 0 1.5.7 1.5 1.5s-.7 1.5-1.5 1.5-1.5-.7-1.5-1.5.7-1.5 1.5-1.5zm-6.2 3.5c-.3 0-.6-.3-.6-.6 0-.3.3-.6.6-.6.3 0 .6.3.6.6 0 .3-.3.6-.6.6z"
    />
  </svg>
)

// 7. MySQL (MySQL Dolphin & Waves)
export const MySqlIcon: React.FC<IconProps> = ({ className = "h-4 w-4", ...props }) => (
  <svg
    viewBox="0 0 24 24"
    className={className}
    aria-label="MySQL"
    {...props}
  >
    <path
      fill="#00758F"
      d="M21.5 14.5c-.5-.3-1.1-.4-1.7-.3-.6.1-1.2.4-1.6.8-.7.7-1.5 1.3-2.4 1.8-1.5.8-3.2 1.2-4.9 1.2-3.1 0-6-1.5-7.8-4-.2-.3-.5-.4-.8-.4-.4 0-.8.3-.9.7-.1.4.1.8.5 1.1 2.1 2.8 5.4 4.6 9 4.6 2 0 3.9-.5 5.7-1.4 1-.5 1.9-1.2 2.7-2 .3-.3.7-.4 1.1-.5.5-.1 1 0 1.4.2.3.2.7.1.9-.2.2-.3.1-.7-.2-.9z"
    />
    <path
      fill="#F29111"
      d="M14.2 3.5c-2.3 0-4.4 1-5.9 2.6-.9 1-1.6 2.3-1.9 3.6-.1.6-.2 1.2-.1 1.8.1.9.5 1.7 1.1 2.3.6.6 1.4 1 2.3 1.1 1.2.1 2.3-.3 3.2-1 1-1 1.6-2.3 1.7-3.7.1-1.3-.3-2.6-1.1-3.6-.9-1.2-2.1-2-3.5-2.5.3-.4.8-.6 1.3-.6 1.3 0 2.5.7 3.1 1.8.2.4.7.5 1.1.3.4-.2.5-.7.3-1.1-.9-1.6-2.6-2.7-4.5-2.7z"
    />
  </svg>
)

interface LanguageIconProps {
  languageId?: string | null
  className?: string
}

export const LanguageIcon: React.FC<LanguageIconProps> = ({
  languageId,
  className = "h-4 w-4",
}) => {
  if (!languageId) return <Code2 className={className} />
  const lower = languageId.toLowerCase()

  if (lower.includes("python") || lower === "py") {
    return <PythonIcon className={className} />
  }
  if (lower.includes("javascript") || lower.includes("node") || lower === "js") {
    return <JavaScriptIcon className={className} />
  }
  if (lower.includes("java-") || lower === "java") {
    return <JavaIcon className={className} />
  }
  if (lower.includes("cpp") || lower.includes("c++")) {
    return <CppIcon className={className} />
  }
  if (lower.includes("c-") || lower === "c") {
    return <CIcon className={className} />
  }
  if (lower.includes("postgres") || lower.includes("psql")) {
    return <PostgresIcon className={className} />
  }
  if (lower.includes("mysql") || lower.includes("sql")) {
    return <MySqlIcon className={className} />
  }

  return <Code2 className={className} />
}
