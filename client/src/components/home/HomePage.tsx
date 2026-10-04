import React, { useState } from "react"
import { Link } from "@tanstack/react-router"
import { Play, ArrowRight, Sun, Moon, Search, Terminal, Code2, X } from "lucide-react"
import { Button } from "@/components/ui/button"
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

  // Group languages or highlight popular ones
  const popularLanguages = SUPPORTED_LANGUAGES.filter((l) => l.popular)
  const otherLanguages = SUPPORTED_LANGUAGES.filter((l) => !l.popular)

  const renderLanguageItem = (lang: LanguageMeta) => (
    <Link
      key={lang.id}
      to="/compiler"
      search={{ lang: lang.id }}
      onClick={() => setSelectedLanguageId(lang.id)}
      className="group flex cursor-pointer items-center justify-between rounded-lg border bg-card p-3 text-left transition-all duration-150 hover:border-primary/50 hover:bg-accent/40 focus:ring-2 focus:ring-primary/40 focus:outline-hidden"
    >
      <div className="flex items-center gap-3">
        <div
          className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-md border ${lang.borderColor} ${lang.bgLight} transition-transform group-hover:scale-105`}
        >
          <LanguageIcon languageId={lang.id} className="h-5 w-5" />
        </div>
        <div>
          <div className="text-sm font-semibold text-foreground transition-colors group-hover:text-primary">
            {lang.shortName}
          </div>
          <div className="font-mono text-[11px] text-muted-foreground">
            {lang.version}
          </div>
        </div>
      </div>

      <div className="flex items-center gap-2">
        <span className="rounded bg-muted/60 px-2 py-0.5 font-mono text-[10px] text-muted-foreground">
          {lang.tag}
        </span>
        <ArrowRight className="h-4 w-4 text-muted-foreground transition-all group-hover:translate-x-0.5 group-hover:text-primary" />
      </div>
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
          <Link to="/compiler">
            <Button
              variant="ghost"
              size="sm"
              className="h-8 text-xs font-medium text-muted-foreground hover:text-foreground cursor-pointer"
            >
              Online Compiler
            </Button>
          </Link>

          <Button
            variant="ghost"
            size="icon"
            onClick={toggleTheme}
            title={`Switch to ${theme === "dark" ? "light" : "dark"} mode`}
            className="h-8 w-8 text-muted-foreground hover:text-foreground cursor-pointer"
          >
            {theme === "dark" ? (
              <Sun className="h-4 w-4 text-amber-400" />
            ) : (
              <Moon className="h-4 w-4" />
            )}
          </Button>
        </div>
      </header>

      {/* Main Section */}
      <main className="flex flex-1 flex-col items-center justify-center px-4 py-12 sm:py-16">
        <div className="flex w-full max-w-2xl flex-col items-center space-y-6 text-center">
          {/* Central Introduction */}
          <div className="space-y-2">
            <h1 className="text-3xl font-extrabold tracking-tight text-foreground sm:text-4xl">
              Online Compiler
            </h1>
            <p className="mx-auto max-w-md text-sm text-muted-foreground sm:text-base">
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
          <div className="w-full space-y-4 pt-6 text-left">
            <div className="flex items-center justify-between pb-0.5">
              <span className="text-xs font-semibold tracking-wider text-muted-foreground uppercase">
                Languages
              </span>
              <span className="text-[11px] text-muted-foreground font-mono">
                {SUPPORTED_LANGUAGES.length} available
              </span>
            </div>

            {/* Full-width clean Search Bar */}
            <div className="relative w-full">
              <Search className="absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2 text-muted-foreground/70" />
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search languages (e.g. Python, Java, C++, TypeScript, SQL)..."
                className="w-full rounded-lg border border-border/80 bg-muted/30 py-2.5 pr-9 pl-9 text-xs sm:text-sm text-foreground placeholder:text-muted-foreground/60 focus:border-primary/50 focus:bg-background focus:ring-2 focus:ring-primary/20 focus:outline-hidden transition-all shadow-2xs"
              />
              {search && (
                <button
                  type="button"
                  onClick={() => setSearch("")}
                  className="absolute top-1/2 right-2.5 -translate-y-1/2 text-muted-foreground hover:text-foreground cursor-pointer p-1 rounded-full hover:bg-muted"
                  title="Clear search"
                >
                  <X className="h-3.5 w-3.5" />
                </button>
              )}
            </div>

            {search.trim() ? (
              <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-2">
                {filteredLanguages.length > 0 ? (
                  filteredLanguages.map(renderLanguageItem)
                ) : (
                  <div className="col-span-2 py-6 text-center text-xs text-muted-foreground">
                    No language matching &quot;{search}&quot;
                  </div>
                )}
              </div>
            ) : (
              <div className="space-y-4">
                {/* Popular Languages */}
                <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-2">
                  {popularLanguages.map(renderLanguageItem)}
                </div>

                {/* Other Languages */}
                {otherLanguages.length > 0 && (
                  <div className="space-y-2 pt-2">
                    <span className="text-[11px] font-medium tracking-wider text-muted-foreground uppercase">
                      Databases
                    </span>
                    <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-2">
                      {otherLanguages.map(renderLanguageItem)}
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
