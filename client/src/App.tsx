import { useEffect } from "react"
import { ThemeProvider } from "@/components/theme-provider"
import { Header } from "@/components/ide/Header"
import { CodeEditor } from "@/components/ide/CodeEditor"
import { StdinPanel } from "@/components/ide/StdinPanel"
import { OutputPanel } from "@/components/ide/OutputPanel"
import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from "@/components/ui/resizable"
import { useCompiler } from "@/hooks/useCompiler"

function CompilerIDE() {
  const {
    languages,
    selectedLanguage,
    code,
    stdin,
    isRunning,
    result,
    isBackendHealthy,
    editorSettings,
    selectLanguage,
    setCode,
    setStdin,
    resetCode,
    runCode,
    clearResult,
    updateEditorSettings,
  } = useCompiler()

  // Global keyboard shortcut: Ctrl+Enter or Cmd+Enter to run code
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === "Enter") {
        e.preventDefault()
        runCode()
      }
    }
    window.addEventListener("keydown", handleKeyDown)
    return () => window.removeEventListener("keydown", handleKeyDown)
  }, [runCode])

  return (
    <div className="flex h-screen w-screen flex-col overflow-hidden bg-background text-foreground antialiased font-sans">
      {/* Top Navigation Bar */}
      <Header
        languages={languages}
        selectedLanguage={selectedLanguage}
        onSelectLanguage={selectLanguage}
        onRun={runCode}
        onResetCode={resetCode}
        isRunning={isRunning}
        isBackendHealthy={isBackendHealthy}
        editorSettings={editorSettings}
        onUpdateEditorSettings={updateEditorSettings}
      />

      {/* Main Workbench Area */}
      <main className="flex-1 overflow-hidden">
        <ResizablePanelGroup orientation="horizontal" className="h-full w-full">
          {/* Left Panel: Code Editor + Stdin */}
          <ResizablePanel defaultSize={60} minSize={30}>
            <ResizablePanelGroup orientation="vertical" className="h-full w-full">
              {/* Code Editor */}
              <ResizablePanel defaultSize={75} minSize={40}>
                <CodeEditor
                  language={selectedLanguage}
                  code={code}
                  onChange={setCode}
                  onRun={runCode}
                  settings={editorSettings}
                />
              </ResizablePanel>

              <ResizableHandle withHandle />

              {/* Standard Input (stdin) Panel */}
              <ResizablePanel defaultSize={25} minSize={15}>
                <StdinPanel
                  value={stdin}
                  onChange={setStdin}
                  disabled={isRunning}
                />
              </ResizablePanel>
            </ResizablePanelGroup>
          </ResizablePanel>

          <ResizableHandle withHandle />

          {/* Right Panel: Output & Diagnostics Terminal */}
          <ResizablePanel defaultSize={40} minSize={25}>
            <OutputPanel
              result={result}
              isRunning={isRunning}
              onClear={clearResult}
            />
          </ResizablePanel>
        </ResizablePanelGroup>
      </main>
    </div>
  )
}

export function App() {
  return (
    <ThemeProvider defaultTheme="dark" storageKey="compilr_theme">
      <CompilerIDE />
    </ThemeProvider>
  )
}

export default App
