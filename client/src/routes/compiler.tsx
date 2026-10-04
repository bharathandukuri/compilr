import { useState, useEffect } from "react"
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
import type { ExecuteResponse, ExecutionStatus } from "@/types/compiler"

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
  const codeMap = useCompilerStore((s) => s.codeMap)
  const stdinMap = useCompilerStore((s) => s.stdinMap)
  const {
    setSelectedLanguageId,
    setCode,
    setStdin,
    resetCode,
    setActiveOutputTab,
  } = useCompilerActions()

  // Sync route search param with Zustand store
  useEffect(() => {
    if (search.lang && search.lang !== selectedLanguageId) {
      const exists = languages.some((l) => l.id === search.lang)
      if (exists) {
        setSelectedLanguageId(search.lang)
      }
    }
  }, [search.lang, selectedLanguageId, languages, setSelectedLanguageId])

  const activeLanguage =
    languages.find((l) => l.id === selectedLanguageId) || languages[0]

  const currentCode =
    codeMap[activeLanguage.id] !== undefined
      ? codeMap[activeLanguage.id]
      : activeLanguage.defaultStarterCode

  const currentStdin = stdinMap[activeLanguage.id] || ""

  const handleRun = () => {
    setActiveOutputTab("output")
    if (isMobile) {
      setMobileTab("output")
    }
    executeMutation.mutate({
      language: activeLanguage.id,
      sourceCode: currentCode,
      stdin: currentStdin,
      options: {
        timeLimitMs: 5000,
        memoryLimitKb: 262144,
      },
    })
  }

  // Global keyboard shortcut: Ctrl+Enter or Cmd+Enter to run code
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === "Enter") {
        e.preventDefault()
        handleRun()
      }
    }
    window.addEventListener("keydown", handleKeyDown)
    return () => window.removeEventListener("keydown", handleKeyDown)
  })

  const handleSelectLanguage = (langId: string) => {
    setSelectedLanguageId(langId)
    navigate({
      to: "/compiler",
      search: { lang: langId },
    })
  }

  const mutationErr = executeMutation.error
  const errorStatus: ExecutionStatus =
    mutationErr instanceof CompilerApiError
      ? mutationErr.status === 429
        ? "RATE_LIMITED"
        : mutationErr.status === 503
        ? "CAPACITY_EXCEEDED"
        : "SYSTEM_ERROR"
      : "SYSTEM_ERROR"

  const result: ExecuteResponse | null =
    executeMutation.data ??
    (mutationErr
      ? {
          executionId: "err-" + (mutationErr instanceof CompilerApiError ? mutationErr.status : "system"),
          language: activeLanguage.id,
          status: errorStatus,
          stderr: mutationErr.message,
          error: mutationErr.message,
        }
      : null)

  return (
    <div className="flex h-screen w-screen flex-col overflow-hidden bg-background text-foreground antialiased font-sans">
      {/* Top Header */}
      <Header
        languages={languages}
        selectedLanguage={activeLanguage}
        onSelectLanguage={handleSelectLanguage}
        onRun={handleRun}
        onResetCode={() => resetCode(activeLanguage.id, activeLanguage.defaultStarterCode)}
        isRunning={executeMutation.isPending}
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
            {result && (
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
                onChange={(newCode) => setCode(activeLanguage.id, newCode)}
                onRun={handleRun}
              />
            ) : (
              <OutputPanel
                result={result}
                isRunning={executeMutation.isPending}
                onClear={() => executeMutation.reset()}
                stdin={currentStdin}
                onStdinChange={(newStdin) => setStdin(activeLanguage.id, newStdin)}
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
                onChange={(newCode) => setCode(activeLanguage.id, newCode)}
                onRun={handleRun}
              />
            </ResizablePanel>

            <ResizableHandle withHandle />

            {/* Right Panel: Output & Stdin Panel */}
            <ResizablePanel defaultSize={40} minSize={25}>
              <OutputPanel
                result={result}
                isRunning={executeMutation.isPending}
                onClear={() => executeMutation.reset()}
                stdin={currentStdin}
                onStdinChange={(newStdin) => setStdin(activeLanguage.id, newStdin)}
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
