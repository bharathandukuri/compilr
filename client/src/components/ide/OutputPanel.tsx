import React, { useState, useEffect } from "react"
import {
  Terminal,
  AlertTriangle,
  Info,
  Copy,
  Check,
  Trash2,
  Clock,
  Cpu,
  Loader2,
  CheckCircle2,
  XCircle,
} from "lucide-react"
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import type { ExecuteResponse, ExecutionStatus } from "@/types/compiler"

interface OutputPanelProps {
  result: ExecuteResponse | null
  isRunning: boolean
  onClear: () => void
}

export const OutputPanel: React.FC<OutputPanelProps> = ({
  result,
  isRunning,
  onClear,
}) => {
  const [activeTab, setActiveTab] = useState<string>("stdout")
  const [copied, setCopied] = useState<boolean>(false)

  // Automatically switch to diagnostics if compilation or runtime error occurs
  useEffect(() => {
    if (result) {
      if (
        result.status === "COMPILATION_ERROR" ||
        result.status === "RUNTIME_ERROR" ||
        (result.stderr && result.stderr.trim().length > 0 && !result.stdout)
      ) {
        setActiveTab("stderr")
      } else {
        setActiveTab("stdout")
      }
    }
  }, [result])

  const copyToClipboard = () => {
    const textToCopy =
      activeTab === "stderr"
        ? result?.stderr || ""
        : activeTab === "metrics"
        ? JSON.stringify(result, null, 2)
        : result?.stdout || ""

    if (textToCopy) {
      navigator.clipboard.writeText(textToCopy)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    }
  }

  const getStatusBadge = (status?: ExecutionStatus) => {
    if (!status) return null

    switch (status) {
      case "SUCCESS":
        return (
          <Badge className="bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border-emerald-500/30 gap-1 font-semibold text-[11px]">
            <CheckCircle2 className="h-3 w-3" />
            SUCCESS
          </Badge>
        )
      case "COMPILATION_ERROR":
        return (
          <Badge className="bg-amber-500/15 text-amber-600 dark:text-amber-400 border-amber-500/30 gap-1 font-semibold text-[11px]">
            <AlertTriangle className="h-3 w-3" />
            COMPILATION ERROR
          </Badge>
        )
      case "RUNTIME_ERROR":
        return (
          <Badge className="bg-rose-500/15 text-rose-600 dark:text-rose-400 border-rose-500/30 gap-1 font-semibold text-[11px]">
            <XCircle className="h-3 w-3" />
            RUNTIME ERROR
          </Badge>
        )
      case "TIME_LIMIT_EXCEEDED":
        return (
          <Badge className="bg-rose-500/15 text-rose-600 dark:text-rose-400 border-rose-500/30 gap-1 font-semibold text-[11px]">
            <Clock className="h-3 w-3" />
            TIME LIMIT EXCEEDED
          </Badge>
        )
      case "MEMORY_LIMIT_EXCEEDED":
        return (
          <Badge className="bg-purple-500/15 text-purple-600 dark:text-purple-400 border-purple-500/30 gap-1 font-semibold text-[11px]">
            <Cpu className="h-3 w-3" />
            MEMORY LIMIT EXCEEDED
          </Badge>
        )
      default:
        return (
          <Badge variant="destructive" className="gap-1 font-semibold text-[11px]">
            <AlertTriangle className="h-3 w-3" />
            {status}
          </Badge>
        )
    }
  }

  const formatMemory = (kb?: number) => {
    if (!kb) return null
    if (kb >= 1024) {
      return `${(kb / 1024).toFixed(1)} MB`
    }
    return `${kb} KB`
  }

  const hasStderr = Boolean(result?.stderr && result.stderr.trim().length > 0)

  return (
    <div className="flex h-full w-full flex-col bg-background overflow-hidden border-l">
      {/* Top Bar with Tabs and Metrics */}
      <Tabs
        value={activeTab}
        onValueChange={setActiveTab}
        className="flex h-full w-full flex-col"
      >
        <div className="flex h-10 shrink-0 items-center justify-between border-b bg-muted/40 px-3">
          {/* Navigation Tabs */}
          <TabsList className="h-7 bg-muted/70 p-0.5">
            <TabsTrigger
              value="stdout"
              className="h-6 px-2.5 text-xs gap-1.5 data-[state=active]:bg-background"
            >
              <Terminal className="h-3.5 w-3.5 text-emerald-500" />
              <span>Standard Output</span>
            </TabsTrigger>

            <TabsTrigger
              value="stderr"
              className="h-6 px-2.5 text-xs gap-1.5 data-[state=active]:bg-background"
            >
              <AlertTriangle className={`h-3.5 w-3.5 ${hasStderr ? "text-amber-500" : "text-muted-foreground"}`} />
              <span>Diagnostics</span>
              {hasStderr && (
                <span className="h-1.5 w-1.5 rounded-full bg-amber-500" />
              )}
            </TabsTrigger>

            <TabsTrigger
              value="metrics"
              className="h-6 px-2.5 text-xs gap-1.5 data-[state=active]:bg-background"
            >
              <Info className="h-3.5 w-3.5 text-blue-500" />
              <span>Metrics</span>
            </TabsTrigger>
          </TabsList>

          {/* Right: Metrics & Actions */}
          <div className="flex items-center gap-2">
            {isRunning && (
              <div className="flex items-center gap-1.5 text-xs text-muted-foreground font-mono">
                <Loader2 className="h-3.5 w-3.5 animate-spin text-primary" />
                <span>Executing...</span>
              </div>
            )}

            {!isRunning && result && (
              <div className="flex items-center gap-2">
                {getStatusBadge(result.status)}

                {result.executionTimeMs !== undefined && (
                  <Badge variant="outline" className="font-mono text-[10px] h-5 px-1.5 text-muted-foreground">
                    <Clock className="h-2.5 w-2.5 mr-1" />
                    {result.executionTimeMs} ms
                  </Badge>
                )}

                {result.memoryUsageKb && (
                  <Badge variant="outline" className="font-mono text-[10px] h-5 px-1.5 text-muted-foreground">
                    <Cpu className="h-2.5 w-2.5 mr-1" />
                    {formatMemory(result.memoryUsageKb)}
                  </Badge>
                )}

                {result.exitCode !== undefined && (
                  <Badge variant="outline" className="font-mono text-[10px] h-5 px-1.5 text-muted-foreground">
                    Exit {result.exitCode}
                  </Badge>
                )}
              </div>
            )}

            <Button
              variant="ghost"
              size="icon"
              onClick={copyToClipboard}
              disabled={isRunning || !result}
              className="h-7 w-7 text-muted-foreground hover:text-foreground"
              title="Copy output"
            >
              {copied ? <Check className="h-3.5 w-3.5 text-emerald-500" /> : <Copy className="h-3.5 w-3.5" />}
            </Button>

            <Button
              variant="ghost"
              size="icon"
              onClick={onClear}
              disabled={isRunning || !result}
              className="h-7 w-7 text-muted-foreground hover:text-foreground"
              title="Clear output"
            >
              <Trash2 className="h-3.5 w-3.5" />
            </Button>
          </div>
        </div>

        {/* Tab 1: Standard Output */}
        <TabsContent value="stdout" className="flex-1 p-0 m-0 overflow-hidden">
          <div className="h-full w-full overflow-auto p-4 font-mono text-xs text-foreground bg-muted/10 selection:bg-primary/20">
            {isRunning ? (
              <div className="flex h-full flex-col items-center justify-center gap-2 text-muted-foreground">
                <Loader2 className="h-6 w-6 animate-spin text-primary" />
                <p className="text-xs">Executing user code in isolated Linux sandbox...</p>
              </div>
            ) : result?.stdout ? (
              <pre className="whitespace-pre-wrap break-words leading-relaxed">
                {result.stdout}
              </pre>
            ) : result ? (
              <div className="flex h-full items-center justify-center text-xs text-muted-foreground italic">
                {hasStderr
                  ? "No standard output. Check the Diagnostics tab for error details."
                  : "Program finished with no standard output."}
              </div>
            ) : (
              <div className="flex h-full flex-col items-center justify-center gap-2 text-muted-foreground">
                <Terminal className="h-8 w-8 text-muted-foreground/40" />
                <p className="text-xs">Click <span className="font-semibold text-foreground">Run</span> or press <kbd className="rounded bg-muted px-1.5 py-0.5 text-[10px] font-mono">⌘↵</kbd> to execute.</p>
              </div>
            )}
          </div>
        </TabsContent>

        {/* Tab 2: Diagnostics / Stderr */}
        <TabsContent value="stderr" className="flex-1 p-0 m-0 overflow-hidden">
          <div className="h-full w-full overflow-auto p-4 font-mono text-xs bg-rose-950/10 dark:bg-rose-950/20 text-rose-600 dark:text-rose-400 selection:bg-rose-500/20">
            {result?.stderr ? (
              <pre className="whitespace-pre-wrap break-words leading-relaxed font-mono">
                {result.stderr}
              </pre>
            ) : result?.error ? (
              <div className="p-3 rounded bg-rose-500/10 border border-rose-500/20 text-rose-500">
                <p className="font-semibold">Execution Error:</p>
                <p className="mt-1">{result.error}</p>
              </div>
            ) : (
              <div className="flex h-full items-center justify-center text-xs text-muted-foreground italic">
                No errors or compiler diagnostics reported. Clean execution!
              </div>
            )}
          </div>
        </TabsContent>

        {/* Tab 3: Detailed Metrics & Logs */}
        <TabsContent value="metrics" className="flex-1 p-0 m-0 overflow-hidden">
          <div className="h-full w-full overflow-auto p-4 text-xs font-mono">
            {result ? (
              <div className="space-y-4 max-w-lg">
                <div className="rounded-lg border bg-card p-3 space-y-2">
                  <div className="font-semibold text-foreground border-b pb-1">Execution Summary</div>
                  <div className="grid grid-cols-2 gap-2 text-[11px]">
                    <div className="text-muted-foreground">Execution ID:</div>
                    <div className="text-foreground truncate">{result.executionId}</div>
                    <div className="text-muted-foreground">Language:</div>
                    <div className="text-foreground">{result.language}</div>
                    <div className="text-muted-foreground">Status:</div>
                    <div>{getStatusBadge(result.status)}</div>
                    <div className="text-muted-foreground">Execution Time:</div>
                    <div className="text-foreground font-semibold">{result.executionTimeMs ?? 0} ms</div>
                    <div className="text-muted-foreground">Peak Memory:</div>
                    <div className="text-foreground">{formatMemory(result.memoryUsageKb) || "N/A"}</div>
                    <div className="text-muted-foreground">Exit Code:</div>
                    <div className="text-foreground">{result.exitCode ?? "N/A"}</div>
                    <div className="text-muted-foreground">Exit Signal:</div>
                    <div className="text-foreground">{result.exitSignal ?? 0}</div>
                  </div>
                </div>

                {result.logs && result.logs.length > 0 && (
                  <div className="rounded-lg border bg-card p-3 space-y-2">
                    <div className="font-semibold text-foreground border-b pb-1">Sandbox Telemetry Logs</div>
                    <ul className="space-y-1 text-[11px] text-muted-foreground">
                      {result.logs.map((logLine, idx) => (
                        <li key={idx} className="font-mono flex items-center gap-1.5">
                          <span className="text-primary">•</span>
                          <span>{logLine}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            ) : (
              <div className="flex h-full items-center justify-center text-xs text-muted-foreground italic">
                Execution metrics will appear after running code.
              </div>
            )}
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}
