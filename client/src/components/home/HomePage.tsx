import React, { useState } from "react"
import { Link } from "@tanstack/react-router"
import { Play, Sun, Moon, Search, Terminal, Code2, X } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  Tooltip,
  TooltipTrigger,
  TooltipContent,
} from "@/components/ui/tooltip"
import { useTheme } from "@/components/theme-provider"
import { SUPPORTED_LANGUAGES, type LanguageMeta } from "@/constants/languages"
import { LanguageIcon } from "@/components/icons/LanguageIcons"
import { useCompilerActions } from "@/stores/compilerStore"

export const HomePage: React.FC = () => {
  const { theme, setTheme } = useTheme()
  const { setSelectedLanguageId } = useCompilerActions()
  const [search, setSearch] = useState("")

  const toggleTheme = () => {
    setTheme(theme === "dark" ? "light" : "dark")
  }

  const filteredLanguages = SUPPORTED_LANGUAGES.filter((lang) => {
    const q = search.trim().toLowerCase()
    if (!q) return true
    return (
      lang.shortName.toLowerCase().includes(q) ||
      lang.name.toLowerCase().includes(q) ||
      lang.aliases.some((a) => a.toLowerCase().includes(q))
    )
  })

  // Separate programming languages and databases cleanly
  const programmingLanguages = SUPPORTED_LANGUAGES.filter(
    (l) => l.type !== "DATABASE"
  )
  const databaseLanguages = SUPPORTED_LANGUAGES.filter(
    (l) => l.type === "DATABASE"
  )

  const renderLanguageItem = (lang: LanguageMeta) => (
    <Link
      key={lang.id}
      to="/compiler"
      search={{ lang: lang.id }}
      target="_blank"
      rel="noopener noreferrer"
      onClick={() => setSelectedLanguageId(lang.id)}
      className="group flex cursor-pointer items-center justify-between rounded-xl border border-border/70 bg-card p-3.5 sm:p-4 text-left transition-all duration-150 hover:border-primary/50 hover:bg-accent/40 hover:shadow-xs focus:ring-2 focus:ring-primary/40 focus:outline-hidden"
    >
      <div className="flex items-center gap-3.5 min-w-0">
        <LanguageIcon
          languageId={lang.id}
          className="h-10 w-10 sm:h-11 sm:w-11 shrink-0 transition-transform duration-150 group-hover:scale-105"
        />
        <span className="text-sm font-semibold text-foreground transition-colors group-hover:text-primary truncate">
          {lang.shortName}
        </span>
      </div>

      <span className="shrink-0 rounded-md border border-border/40 bg-muted/60 px-2.5 py-1 font-mono text-[11px] text-muted-foreground">
        {lang.tag || lang.version}
      </span>
    </Link>
  )

  return (
    <div className="flex min-h-screen w-full flex-col bg-background font-sans text-foreground antialiased">
      {/* Lightweight Header */}
      <header className="flex h-12 shrink-0 items-center justify-between border-b bg-background/80 px-4 backdrop-blur-xs select-none sm:px-8">
        <Link to="/" className="flex items-center gap-2.5">
          <div className="flex h-7 w-7 items-center justify-center rounded-md bg-primary text-primary-foreground">
            <Code2 className="h-4 w-4" />
          </div>
          <span className="text-base font-bold tracking-tight text-foreground">
            Compilr
          </span>
        </Link>

        <div className="flex items-center gap-2">
          <Tooltip>
            <TooltipTrigger
              render={
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={toggleTheme}
                  className="h-8 w-8 text-muted-foreground hover:text-foreground cursor-pointer"
                >
                  {theme === "dark" ? (
                    <Sun className="h-4 w-4 text-amber-400" />
                  ) : (
                    <Moon className="h-4 w-4" />
                  )}
                </Button>
              }
            />
            <TooltipContent>
              Switch to {theme === "dark" ? "light" : "dark"} mode
            </TooltipContent>
          </Tooltip>
        </div>
      </header>

      {/* Main Section */}
      <main className="flex flex-1 flex-col items-center justify-center px-4 py-12 sm:py-16">
        <div className="flex w-full max-w-6xl flex-col items-center space-y-8 text-center">
          {/* Central Introduction */}
          <div className="space-y-2">
            <h1 className="text-3xl font-extrabold tracking-tight text-foreground sm:text-4xl">
              Online Compiler
            </h1>
            <p className="mx-auto max-w-lg text-sm text-muted-foreground sm:text-base">
              Write, compile, and run code instantly in your browser. Choose a
              language below to start coding.
            </p>
          </div>

          {/* Primary Action Button */}
          <div>
            <Link to="/compiler">
              <Button
                size="lg"
                className="h-10 gap-2 bg-primary px-6 text-xs font-semibold text-primary-foreground shadow-xs hover:opacity-90 sm:text-sm cursor-pointer"
              >
                <Play className="h-3.5 w-3.5 fill-current" />
                <span>Start Coding</span>
              </Button>
            </Link>
          </div>

          {/* Language Selection */}
          <div className="w-full space-y-6 pt-2 text-left">
            {/* Full-width clean Search Bar */}
            <div className="relative w-full">
              <Search className="absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-muted-foreground/70" />
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search languages & databases (e.g. Python, Java, C#, Go, Rust, SQLite, MongoDB)..."
                className="w-full rounded-xl border border-border/80 bg-muted/30 py-3 pr-10 pl-10 text-xs sm:text-sm text-foreground placeholder:text-muted-foreground/60 focus:border-primary/50 focus:bg-background focus:ring-2 focus:ring-primary/20 focus:outline-hidden transition-all shadow-2xs"
              />
              {search && (
                <Tooltip>
                  <TooltipTrigger
                    render={
                      <button
                        type="button"
                        onClick={() => setSearch("")}
                        className="absolute top-1/2 right-3 -translate-y-1/2 text-muted-foreground hover:text-foreground cursor-pointer p-1 rounded-full hover:bg-muted"
                      >
                        <X className="h-4 w-4" />
                      </button>
                    }
                  />
                  <TooltipContent>Clear search</TooltipContent>
                </Tooltip>
              )}
            </div>

            {search.trim() ? (
              <div className="space-y-2">
                <div className="flex items-center justify-between pb-1">
                  <span className="text-xs font-semibold tracking-wider text-muted-foreground uppercase">
                    Search Results
                  </span>
                  <span className="text-[11px] text-muted-foreground font-mono">
                    {filteredLanguages.length} found
                  </span>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3.5">
                  {filteredLanguages.length > 0 ? (
                    filteredLanguages.map(renderLanguageItem)
                  ) : (
                    <div className="col-span-full py-12 text-center text-sm text-muted-foreground">
                      No language matching &quot;{search}&quot;
                    </div>
                  )}
                </div>
              </div>
            ) : (
              <div className="space-y-8">
                {/* Programming Languages */}
                <div className="space-y-3">
                  <div className="flex items-center justify-between pb-1">
                    <span className="text-xs font-semibold tracking-wider text-muted-foreground uppercase">
                      Programming Languages
                    </span>
                    <span className="text-[11px] text-muted-foreground font-mono">
                      {programmingLanguages.length} available
                    </span>
                  </div>
                  <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3.5">
                    {programmingLanguages.map(renderLanguageItem)}
                  </div>
                </div>

                {/* Databases */}
                {databaseLanguages.length > 0 && (
                  <div className="space-y-3 pt-2">
                    <div className="flex items-center justify-between pb-1">
                      <span className="text-xs font-semibold tracking-wider text-muted-foreground uppercase">
                        Databases
                      </span>
                      <span className="text-[11px] text-muted-foreground font-mono">
                        {databaseLanguages.length} available
                      </span>
                    </div>
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3.5">
                      {databaseLanguages.map(renderLanguageItem)}
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </main>

      {/* Minimal Developer Tool Footer */}
      <footer className="flex h-11 shrink-0 items-center justify-center border-t px-4 text-xs text-muted-foreground">
        <div className="flex items-center gap-1.5 font-mono text-[11px]">
          <Terminal className="h-3 w-3" />
          <span>Compilr · Fast isolated sandbox execution</span>
        </div>
      </footer>
    </div>
  )
}
