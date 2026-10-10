import { useState, useEffect, useMemo, useCallback } from "react"
import { createRoute, useSearch, useNavigate } from "@tanstack/react-router"
import { rootRoute } from "./__root"
import { Header } from "@/components/ide/Header"
import { CodeEditor } from "@/components/ide/CodeEditor"
import { OutputPanel } from "@/components/ide/OutputPanel"
import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from "@/components/ui/resizable"
import { useLanguagesQuery, useBackendHealthQuery, useExecuteMutation } from "@/hooks/useCompilerQueries"
import {
  useCompilerStore,
  useCompilerActions,
} from "@/stores/compilerStore"
import { useIsMobile } from "@/hooks/use-mobile"
import { Terminal, Code2 } from "lucide-react"
import { SUPPORTED_LANGUAGES } from "@/constants/languages"
import { CompilerApiError } from "@/api/apiClient"
import type { ExecutionStatus } from "@/types/compiler"

interface CompilerSearchParams {
  lang?: string
}

function CompilerPage() {
  const search = useSearch({ from: "/compiler" })
  const navigate = useNavigate()
  const isMobile = useIsMobile()
  const [mobileTab, setMobileTab] = useState<"code" | "output">("code")

  const { data: languages = SUPPORTED_LANGUAGES } = useLanguagesQuery()
  const { data: isBackendHealthy = true } = useBackendHealthQuery()
  const executeMutation = useExecuteMutation()

  const selectedLanguageId = useCompilerStore((s) => s.selectedLanguageId)
  const {
    setSelectedLanguageId,
    setCode,
    setStdin,
    resetCode,
    setResult,
    clearResult,
    setActiveOutputTab,
  } = useCompilerActions()

  // 1. Resolve active language: URL search param is primary source of truth,
  // falling back to persisted Zustand store, and lastly to languages[0].
  const activeLanguage = useMemo(() => {
    if (search.lang) {
      const match = languages.find((l) => l.id === search.lang)
      if (match) return match
    }
    const storeMatch = languages.find((l) => l.id === selectedLanguageId)
    if (storeMatch) return storeMatch
    return languages[0]
  }, [search.lang, selectedLanguageId, languages])

  const activeLanguageId = activeLanguage.id

  // 2. Normalize URL on initial mount if ?lang= is omitted
  useEffect(() => {
    if (!search.lang && activeLanguageId) {
      navigate({
        to: "/compiler",
        search: { lang: activeLanguageId },
        replace: true,
      })
    }
  }, [search.lang, activeLanguageId, navigate])

  // 3. Sync browser URL changes (e.g. back/forward navigation) into Zustand
  useEffect(() => {
    if (search.lang && search.lang !== useCompilerStore.getState().selectedLanguageId) {
      const exists = languages.some((l) => l.id === search.lang)
      if (exists) {
        setSelectedLanguageId(search.lang)
      }
    }
  }, [search.lang, languages, setSelectedLanguageId])

  // 4. Atomic selectors for active language state (eliminates full re-renders on keystrokes)
  const currentCode = useCompilerStore(
    (s) => s.codeMap[activeLanguageId] ?? activeLanguage.defaultStarterCode
  )
  const currentStdin = useCompilerStore(
    (s) => s.stdinMap[activeLanguageId] ?? ""
  )
  const currentResult = useCompilerStore(
    (s) => s.resultMap[activeLanguageId] ?? null
  )

  const isCurrentLanguageRunning =
    executeMutation.isPending &&
    executeMutation.variables?.language === activeLanguageId

  // 5. Execution handler reading latest synchronous store state to prevent stale closures
  const handleRun = useCallback(
    (overrideCode?: string) => {
      setActiveOutputTab("output")
      if (isMobile) {
        setMobileTab("output")
      }

      const storeState = useCompilerStore.getState()
      const codeToRun =
        overrideCode !== undefined
          ? overrideCode
          : storeState.codeMap[activeLanguageId] !== undefined
            ? storeState.codeMap[activeLanguageId]
            : activeLanguage.defaultStarterCode

      const stdinToRun = storeState.stdinMap[activeLanguageId] ?? ""
      const isDatabase = activeLanguage.type === "DATABASE"

      executeMutation.mutate(
        {
          language: activeLanguageId,
          sourceCode: codeToRun,
          stdin: stdinToRun,
          options: {
            timeLimitMs: isDatabase ? 10000 : 5000,
            memoryLimitKb: isDatabase ? 524288 : 262144,
          },
        },
        {
          onSuccess: (data) => {
            setResult(data.language, data)
          },
          onError: (err, variables) => {
            const errorStatus: ExecutionStatus =
              err instanceof CompilerApiError
                ? err.status === 429
                  ? "RATE_LIMITED"
                  : err.status === 503
                  ? "CAPACITY_EXCEEDED"
                  : "SYSTEM_ERROR"
                : "SYSTEM_ERROR"

            setResult(variables.language, {
              executionId:
                "err-" + (err instanceof CompilerApiError ? err.status : "system"),
              language: variables.language,
              status: errorStatus,
              stderr: err.message,
              error: err.message,
            })
          },
        }
      )
    },
    [
      activeLanguage,
      activeLanguageId,
      executeMutation,
      isMobile,
      setActiveOutputTab,
      setResult,
    ]
  )

  // 6. Global keyboard shortcut: Ctrl+Enter or Cmd+Enter to run code
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === "Enter") {
        e.preventDefault()
        handleRun()
      }
    }
    window.addEventListener("keydown", handleKeyDown)
    return () => window.removeEventListener("keydown", handleKeyDown)
  }, [handleRun])

  // 7. Explicit language selection handler syncing URL and Zustand in harmony
  const handleSelectLanguage = useCallback(
    (langId: string) => {
      if (langId === activeLanguageId) return
      setSelectedLanguageId(langId)
      navigate({
        to: "/compiler",
        search: { lang: langId },
      })
    },
    [activeLanguageId, setSelectedLanguageId, navigate]
  )

  return (
    <div className="flex h-screen w-screen flex-col overflow-hidden bg-background text-foreground antialiased font-sans">
      {/* Top Header */}
      <Header
        languages={languages}
        selectedLanguage={activeLanguage}
        onSelectLanguage={handleSelectLanguage}
        onRun={() => handleRun()}
        onResetCode={() => resetCode(activeLanguageId, activeLanguage.defaultStarterCode)}
        isRunning={isCurrentLanguageRunning}
        isBackendHealthy={isBackendHealthy}
      />

      {/* Mobile Tab Switcher */}
      {isMobile && (
        <div className="flex h-9 shrink-0 items-center border-b bg-muted/40 px-3 justify-center gap-1">
          <button
            type="button"
            onClick={() => setMobileTab("code")}
            className={`flex items-center gap-1.5 px-3 py-1 rounded text-xs font-medium cursor-pointer ${
              mobileTab === "code"
                ? "bg-background text-foreground shadow-2xs font-semibold"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            <Code2 className="h-3.5 w-3.5" />
            <span>Editor</span>
          </button>
          <button
            type="button"
            onClick={() => setMobileTab("output")}
            className={`flex items-center gap-1.5 px-3 py-1 rounded text-xs font-medium cursor-pointer ${
              mobileTab === "output"
                ? "bg-background text-foreground shadow-2xs font-semibold"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            <Terminal className="h-3.5 w-3.5" />
            <span>Output</span>
            {currentResult && (
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
            )}
          </button>
        </div>
      )}

      {/* Main Workbench Area */}
      <main className="flex-1 overflow-hidden">
        {isMobile ? (
          <div className="h-full w-full">
            {mobileTab === "code" ? (
              <CodeEditor
                language={activeLanguage}
                code={currentCode}
                onChange={(newCode) => setCode(activeLanguageId, newCode)}
                onRun={handleRun}
              />
            ) : (
              <OutputPanel
                result={currentResult}
                isRunning={isCurrentLanguageRunning}
                onClear={() => clearResult(activeLanguageId)}
                stdin={currentStdin}
                onStdinChange={(newStdin) => setStdin(activeLanguageId, newStdin)}
              />
            )}
          </div>
        ) : (
          <ResizablePanelGroup orientation="horizontal" className="h-full w-full">
            {/* Left Panel: Code Editor (Maximizes usable editor space) */}
            <ResizablePanel defaultSize={60} minSize={35}>
              <CodeEditor
                language={activeLanguage}
                code={currentCode}
                onChange={(newCode) => setCode(activeLanguageId, newCode)}
                onRun={handleRun}
              />
            </ResizablePanel>

            <ResizableHandle withHandle />

            {/* Right Panel: Output & Stdin Panel */}
            <ResizablePanel defaultSize={40} minSize={25}>
              <OutputPanel
                result={currentResult}
                isRunning={isCurrentLanguageRunning}
                onClear={() => clearResult(activeLanguageId)}
                stdin={currentStdin}
                onStdinChange={(newStdin) => setStdin(activeLanguageId, newStdin)}
              />
            </ResizablePanel>
          </ResizablePanelGroup>
        )}
      </main>
    </div>
  )
}

export const compilerRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/compiler",
  validateSearch: (search: Record<string, unknown>): CompilerSearchParams => {
    return {
      lang: typeof search.lang === "string" ? search.lang : undefined,
    }
  },
  component: CompilerPage,
})
