import React from "react"
import {
  Terminal,
  CornerDownLeft,
  Trash2,
  Loader2,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Clock,
  Columns,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  Tooltip,
  TooltipTrigger,
  TooltipContent,
} from "@/components/ui/tooltip"
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
      case "RATE_LIMITED":
        return (
          <span className="flex items-center gap-1 text-[11px] font-medium text-amber-600 dark:text-amber-400">
            <Clock className="h-3 w-3" />
            <span>Rate Limited</span>
          </span>
        )
      case "CAPACITY_EXCEEDED":
        return (
          <span className="flex items-center gap-1 text-[11px] font-medium text-amber-600 dark:text-amber-400">
            <AlertTriangle className="h-3 w-3" />
            <span>Server Busy</span>
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

  // Render Output Console content
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

  // Render Stdin Input Textarea
  const renderStdinTextarea = () => (
    <div className="h-full w-full p-2 bg-background">
      <textarea
        value={stdin}
        onChange={(e) => onStdinChange(e.target.value)}
        disabled={isRunning}
        placeholder="Enter standard input here..."
        className="h-full w-full resize-none rounded bg-transparent p-2.5 font-mono text-xs text-foreground placeholder:text-muted-foreground/60 focus:outline-hidden focus:ring-1 focus:ring-primary/40 border border-border/40"
        spellCheck={false}
      />
    </div>
  )

  // =========================================================================
  // Render Split View Mode
  // =========================================================================
  if (isSplitView) {
    return (
      <div className="flex h-full w-full flex-col bg-background overflow-hidden border-l">
        {/* Top Half: Output Pane with its dedicated Output Header */}
        <div className="flex h-1/2 flex-col overflow-hidden border-b">
          <div className="flex h-8 shrink-0 items-center justify-between border-b bg-muted/20 px-2 sm:px-3 select-none">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-foreground">
              <Terminal className="h-3.5 w-3.5 text-primary" />
              <span>Output</span>
            </div>

            <div className="flex items-center gap-2">
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

              {/* Split View Toggle */}
              <Tooltip>
                <TooltipTrigger
                  render={
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={toggleSplitView}
                      className="h-6 w-6 text-foreground bg-muted hover:bg-muted/80 hidden sm:flex cursor-pointer"
                    >
                      <Columns className="h-3 w-3" />
                    </Button>
                  }
                />
                <TooltipContent>Switch to single view</TooltipContent>
              </Tooltip>

              {/* Clear Output Button */}
              <Tooltip>
                <TooltipTrigger
                  render={
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={onClear}
                      disabled={isRunning || !result}
                      className="h-6 w-6 text-muted-foreground hover:text-foreground cursor-pointer"
                    >
                      <Trash2 className="h-3 w-3" />
                    </Button>
                  }
                />
                <TooltipContent>Clear output</TooltipContent>
              </Tooltip>
            </div>
          </div>

          <div className="flex-1 overflow-hidden">
            {renderOutputConsole()}
          </div>
        </div>

        {/* Bottom Half: Input Pane with its dedicated Input Header */}
        <div className="flex h-1/2 flex-col overflow-hidden">
          <div className="flex h-8 shrink-0 items-center justify-between border-b bg-muted/20 px-2 sm:px-3 select-none">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-foreground">
              <CornerDownLeft className="h-3.5 w-3.5 text-primary" />
              <span>Standard Input</span>
              <span className="text-[11px] font-normal text-muted-foreground hidden sm:inline">
                (one argument per line)
              </span>
            </div>

            <div className="flex items-center gap-2">
              {hasStdin && (
                <button
                  type="button"
                  onClick={() => onStdinChange("")}
                  disabled={isRunning}
                  className="text-[11px] text-muted-foreground hover:text-foreground cursor-pointer font-medium"
                >
                  Clear input
                </button>
              )}
            </div>
          </div>

          <div className="flex-1 overflow-hidden">
            {renderStdinTextarea()}
          </div>
        </div>
      </div>
    )
  }

  // =========================================================================
  // Render Single (Tabbed) Mode
  // =========================================================================
  return (
    <div className="flex h-full w-full flex-col bg-background overflow-hidden border-l">
      {/* Top Bar with Navigation Tabs & Actions */}
      <div className="flex h-8 shrink-0 items-center justify-between border-b bg-muted/20 px-2 sm:px-3 select-none">
        {/* Navigation Tabs */}
        <div className="flex items-center gap-1">
          <button
            type="button"
            onClick={() => setActiveOutputTab("output")}
            className={`flex items-center gap-1.5 px-2.5 py-1 text-xs font-medium rounded transition-colors cursor-pointer ${
              activeTab === "output"
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
              activeTab === "stdin"
                ? "bg-background text-foreground shadow-2xs font-semibold"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            <CornerDownLeft className="h-3 w-3" />
            <span>Input</span>
            {hasStdin && (
              <Tooltip>
                <TooltipTrigger
                  render={
                    <span className="h-1.5 w-1.5 rounded-full bg-primary" />
                  }
                />
                <TooltipContent>Stdin provided</TooltipContent>
              </Tooltip>
            )}
          </button>
        </div>

        {/* Right Actions & Status */}
        <div className="flex items-center gap-2">
          {/* Status summary */}
          {!isRunning && result && activeTab === "output" && (
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

          {/* Split View Toggle */}
          <Tooltip>
            <TooltipTrigger
              render={
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={toggleSplitView}
                  className="h-6 w-6 text-muted-foreground hover:text-foreground hidden sm:flex cursor-pointer"
                >
                  <Columns className="h-3 w-3" />
                </Button>
              }
            />
            <TooltipContent>Split output & stdin</TooltipContent>
          </Tooltip>

          {/* Clear Button */}
          {activeTab === "output" ? (
            <Tooltip>
              <TooltipTrigger
                render={
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={onClear}
                    disabled={isRunning || !result}
                    className="h-6 w-6 text-muted-foreground hover:text-foreground cursor-pointer"
                  >
                    <Trash2 className="h-3 w-3" />
                  </Button>
                }
              />
              <TooltipContent>Clear output</TooltipContent>
            </Tooltip>
          ) : (
            hasStdin && (
              <Tooltip>
                <TooltipTrigger
                  render={
                    <button
                      type="button"
                      onClick={() => onStdinChange("")}
                      disabled={isRunning}
                      className="text-[11px] text-muted-foreground hover:text-foreground cursor-pointer font-medium"
                    >
                      Clear input
                    </button>
                  }
                />
                <TooltipContent>Clear stdin</TooltipContent>
              </Tooltip>
            )
          )}
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 overflow-hidden">
        {activeTab === "output" ? renderOutputConsole() : renderStdinTextarea()}
      </div>
    </div>
  )
}
