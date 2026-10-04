import React, { useState } from "react"
import {
  Terminal,
  CornerDownLeft,
  Copy,
  Check,
  Trash2,
  Loader2,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Clock,
  Columns,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import type { ExecuteResponse, ExecutionStatus } from "@/types/compiler"
import {
  useActiveOutputTab,
  useIsSplitView,
  useCompilerActions,
} from "@/stores/compilerStore"

interface OutputPanelProps {
  result: ExecuteResponse | null
  isRunning: boolean
  onClear: () => void
  stdin: string
  onStdinChange: (val: string) => void
}

export const OutputPanel: React.FC<OutputPanelProps> = ({
  result,
  isRunning,
  onClear,
  stdin,
  onStdinChange,
}) => {
  const activeTab = useActiveOutputTab()
  const isSplitView = useIsSplitView()
  const { setActiveOutputTab, toggleSplitView } = useCompilerActions()
  const [copied, setCopied] = useState<boolean>(false)

  const copyToClipboard = () => {
    const textToCopy = result?.stdout || result?.stderr || result?.error || ""
    if (textToCopy) {
      navigator.clipboard.writeText(textToCopy)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    }
  }

  const formatMemory = (kb?: number) => {
    if (!kb) return null
    if (kb >= 1024) return `${(kb / 1024).toFixed(1)} MB`
    return `${kb} KB`
  }

  const getStatusBadge = (status?: ExecutionStatus) => {
    if (!status) return null

    switch (status) {
      case "SUCCESS":
        return (
          <span className="flex items-center gap-1 text-[11px] font-medium text-emerald-600 dark:text-emerald-400">
            <CheckCircle2 className="h-3 w-3" />
            <span>Success</span>
          </span>
        )
      case "COMPILATION_ERROR":
        return (
          <span className="flex items-center gap-1 text-[11px] font-medium text-amber-600 dark:text-amber-400">
            <AlertTriangle className="h-3 w-3" />
            <span>Compile Error</span>
          </span>
        )
      case "RUNTIME_ERROR":
        return (
          <span className="flex items-center gap-1 text-[11px] font-medium text-rose-600 dark:text-rose-400">
            <XCircle className="h-3 w-3" />
            <span>Runtime Error</span>
          </span>
        )
      case "TIME_LIMIT_EXCEEDED":
        return (
          <span className="flex items-center gap-1 text-[11px] font-medium text-rose-600 dark:text-rose-400">
            <Clock className="h-3 w-3" />
            <span>Timeout</span>
          </span>
        )
      default:
        return (
          <span className="flex items-center gap-1 text-[11px] font-medium text-rose-500">
            <AlertTriangle className="h-3 w-3" />
            <span>{status.replace(/_/g, " ")}</span>
          </span>
        )
    }
  }

  const hasStderr = Boolean(result?.stderr && result.stderr.trim().length > 0)
  const hasStdout = Boolean(result?.stdout && result.stdout.length > 0)
  const hasStdin = Boolean(stdin && stdin.trim().length > 0)

  // Render Stdin component
  const renderStdinEditor = () => (
    <div className="flex h-full w-full flex-col bg-background">
      <div className="flex h-7 shrink-0 items-center justify-between border-b px-3 text-[11px] text-muted-foreground bg-muted/20">
        <span>Standard Input (one argument per line)</span>
        {stdin.length > 0 && (
          <button
            type="button"
            onClick={() => onStdinChange("")}
            className="hover:text-foreground text-[10px] cursor-pointer"
          >
            Clear input
          </button>
        )}
      </div>
      <div className="flex-1 p-2">
        <textarea
          value={stdin}
          onChange={(e) => onStdinChange(e.target.value)}
          disabled={isRunning}
          placeholder="Enter input here..."
          className="h-full w-full resize-none rounded bg-transparent p-2 font-mono text-xs text-foreground placeholder:text-muted-foreground/60 focus:outline-hidden focus:ring-1 focus:ring-primary/40 border border-border/40"
          spellCheck={false}
        />
      </div>
    </div>
  )

  // Render Output Console
  const renderOutputConsole = () => (
    <div className="h-full w-full overflow-auto p-3 font-mono text-xs leading-relaxed select-text">
      {isRunning ? (
        <div className="flex h-full flex-col items-center justify-center gap-2 text-muted-foreground">
          <Loader2 className="h-5 w-5 animate-spin text-primary" />
          <span className="text-xs">Running code...</span>
        </div>
      ) : result ? (
        <div className="space-y-3">
          {/* Main Stdout */}
          {hasStdout && (
            <pre className="whitespace-pre-wrap break-words text-foreground font-mono">
              {result.stdout}
            </pre>
          )}

          {/* Stderr / Errors */}
          {hasStderr && (
            <div className="rounded border border-rose-500/30 bg-rose-500/10 p-2.5 text-rose-600 dark:text-rose-400">
              <div className="text-[11px] font-semibold uppercase tracking-wider mb-1">
                {result.status === "COMPILATION_ERROR"
                  ? "Compilation Error"
                  : result.status === "RUNTIME_ERROR"
                  ? `Runtime Error (exit code ${result.exitCode ?? 1})`
                  : "Error Output"}
              </div>
              <pre className="whitespace-pre-wrap break-words font-mono text-xs">
                {result.stderr}
              </pre>
            </div>
          )}

          {/* General Error (e.g. system or network) */}
          {result.error && !hasStderr && (
            <div className="rounded border border-rose-500/30 bg-rose-500/10 p-2.5 text-rose-500">
              <pre className="whitespace-pre-wrap break-words font-mono text-xs">
                {result.error}
              </pre>
            </div>
          )}

          {/* Empty output case */}
          {!hasStdout && !hasStderr && !result.error && (
            <div className="text-muted-foreground italic text-xs py-4 text-center">
              Program executed with no output.
            </div>
          )}
        </div>
      ) : (
        <div className="flex h-full flex-col items-center justify-center text-muted-foreground text-xs py-10">
          <Terminal className="h-6 w-6 mb-2 opacity-40" />
          <span>Click Run to execute code</span>
        </div>
      )}
    </div>
  )

  return (
    <div className="flex h-full w-full flex-col bg-background overflow-hidden border-l">
      {/* Top Bar with Navigation & Actions */}
      <div className="flex h-8 shrink-0 items-center justify-between border-b bg-muted/20 px-2 sm:px-3 select-none">
        {/* Navigation Tabs */}
        <div className="flex items-center gap-1">
          <button
            type="button"
            onClick={() => setActiveOutputTab("output")}
            className={`flex items-center gap-1.5 px-2.5 py-1 text-xs font-medium rounded transition-colors cursor-pointer ${
              activeTab === "output" && !isSplitView
                ? "bg-background text-foreground shadow-2xs font-semibold"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            <Terminal className="h-3 w-3" />
            <span>Output</span>
          </button>

          <button
            type="button"
            onClick={() => setActiveOutputTab("stdin")}
            className={`flex items-center gap-1.5 px-2.5 py-1 text-xs font-medium rounded transition-colors cursor-pointer ${
              activeTab === "stdin" && !isSplitView
                ? "bg-background text-foreground shadow-2xs font-semibold"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            <CornerDownLeft className="h-3 w-3" />
            <span>Input</span>
            {hasStdin && (
              <span className="h-1.5 w-1.5 rounded-full bg-primary" title="Stdin provided" />
            )}
          </button>
        </div>

        {/* Right Actions & Status */}
        <div className="flex items-center gap-2">
          {/* Status summary */}
          {!isRunning && result && (
            <div className="flex items-center gap-2 text-[11px] font-mono text-muted-foreground">
              {getStatusBadge(result.status)}
              {result.executionTimeMs !== undefined && (
                <span>{result.executionTimeMs}ms</span>
              )}
              {result.memoryUsageKb && (
                <span className="hidden sm:inline">· {formatMemory(result.memoryUsageKb)}</span>
              )}
            </div>
          )}

          {/* Split view toggle */}
          <Button
            variant="ghost"
            size="icon"
            onClick={toggleSplitView}
            className={`h-6 w-6 text-muted-foreground hover:text-foreground hidden sm:flex ${
              isSplitView ? "bg-muted text-foreground" : ""
            }`}
            title={isSplitView ? "Switch to single view" : "Split Output & Input"}
          >
            <Columns className="h-3 w-3" />
          </Button>

          {/* Copy Button */}
          <Button
            variant="ghost"
            size="icon"
            onClick={copyToClipboard}
            disabled={isRunning || !result}
            className="h-6 w-6 text-muted-foreground hover:text-foreground"
            title="Copy output"
          >
            {copied ? (
              <Check className="h-3 w-3 text-emerald-500" />
            ) : (
              <Copy className="h-3 w-3" />
            )}
          </Button>

          {/* Clear Button */}
          <Button
            variant="ghost"
            size="icon"
            onClick={onClear}
            disabled={isRunning || !result}
            className="h-6 w-6 text-muted-foreground hover:text-foreground"
            title="Clear output"
          >
            <Trash2 className="h-3 w-3" />
          </Button>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 overflow-hidden">
        {isSplitView ? (
          <div className="flex h-full flex-col">
            <div className="h-1/2 overflow-hidden border-b">
              {renderOutputConsole()}
            </div>
            <div className="h-1/2 overflow-hidden">
              {renderStdinEditor()}
            </div>
          </div>
        ) : activeTab === "output" ? (
          renderOutputConsole()
        ) : (
          renderStdinEditor()
        )}
      </div>
    </div>
  )
}
