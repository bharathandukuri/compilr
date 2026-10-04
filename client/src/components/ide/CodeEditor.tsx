import React, { useRef, useEffect } from "react"
import Editor, { type OnMount, type Monaco } from "@monaco-editor/react"
import type * as monaco from "monaco-editor"
import { useTheme } from "@/components/theme-provider"
import type { Language } from "@/types/compiler"
import type { EditorSettings } from "@/utils/storage"
import { FileCode2 } from "lucide-react"

interface CodeEditorProps {
  language: Language | null
  code: string
  onChange: (value: string) => void
  onRun: () => void
  settings: EditorSettings
}

export const CodeEditor: React.FC<CodeEditorProps> = ({
  language,
  code,
  onChange,
  onRun,
  settings,
}) => {
  const { theme } = useTheme()
  const editorRef = useRef<monaco.editor.IStandaloneCodeEditor | null>(null)

  // Map backend language id to Monaco language id
  const getMonacoLanguage = (langId?: string): string => {
    if (!langId) return "plaintext"
    const lower = langId.toLowerCase()
    if (lower.includes("java-") || lower === "java") return "java"
    if (lower.includes("python") || lower === "py") return "python"
    if (lower.includes("cpp") || lower === "c++") return "cpp"
    if (lower.includes("c-") || lower === "c") return "c"
    if (lower.includes("node") || lower.includes("javascript") || lower === "js") return "javascript"
    if (lower.includes("sql") || lower.includes("postgres") || lower.includes("mysql")) return "sql"
    return "plaintext"
  }

  const getFileName = (lang?: Language | null): string => {
    if (!lang) return "main.txt"
    if (lang.id === "java-21") return "Solution.java"
    return `solution${lang.fileExtension || ""}`
  }

  const handleEditorMount: OnMount = (editor, monacoInstance: Monaco) => {
    editorRef.current = editor

    // Add command for Cmd+Enter / Ctrl+Enter to trigger execution
    editor.addCommand(
      monacoInstance.KeyMod.CtrlCmd | monacoInstance.KeyCode.Enter,
      () => {
        onRun()
      }
    )

    // Format document shortcut
    editor.addCommand(
      monacoInstance.KeyMod.CtrlCmd | monacoInstance.KeyMod.Shift | monacoInstance.KeyCode.KeyF,
      () => {
        editor.getAction("editor.action.formatDocument")?.run()
      }
    )
  }

  // Update editor settings dynamically
  useEffect(() => {
    if (editorRef.current) {
      editorRef.current.updateOptions({
        fontSize: settings.fontSize,
        tabSize: settings.tabSize,
        wordWrap: settings.wordWrap,
        minimap: { enabled: settings.minimap },
      })
    }
  }, [settings])

  const monacoLang = getMonacoLanguage(language?.id)
  const isDark = theme === "dark" || (theme === "system" && window.matchMedia("(prefers-color-scheme: dark)").matches)

  return (
    <div className="flex h-full w-full flex-col bg-background overflow-hidden">
      {/* Editor Tab Bar */}
      <div className="flex h-9 shrink-0 items-center justify-between border-b bg-muted/40 px-3">
        <div className="flex items-center gap-2">
          <div className="flex items-center gap-1.5 rounded-t bg-background px-3 py-1 text-xs font-medium border-t border-x border-primary/40 shadow-xs">
            <FileCode2 className="h-3.5 w-3.5 text-primary" />
            <span>{getFileName(language)}</span>
          </div>
        </div>

        <div className="flex items-center gap-3 text-[11px] text-muted-foreground font-mono">
          <span>{language?.name || "No Language"}</span>
          <span className="hidden sm:inline">UTF-8</span>
          <span className="hidden md:inline">Spaces: {settings.tabSize}</span>
        </div>
      </div>

      {/* Monaco Editor Container */}
      <div className="relative flex-1 w-full overflow-hidden">
        <Editor
          height="100%"
          width="100%"
          language={monacoLang}
          theme={isDark ? "vs-dark" : "light"}
          value={code}
          onChange={(val) => onChange(val || "")}
          onMount={handleEditorMount}
          loading={
            <div className="flex h-full w-full items-center justify-center text-xs text-muted-foreground">
              Loading editor environment...
            </div>
          }
          options={{
            fontSize: settings.fontSize,
            tabSize: settings.tabSize,
            wordWrap: settings.wordWrap,
            minimap: { enabled: settings.minimap },
            fontFamily: "'JetBrains Mono', 'Fira Code', 'Cascadia Code', Consolas, monospace",
            fontLigatures: true,
            scrollBeyondLastLine: false,
            automaticLayout: true,
            padding: { top: 12, bottom: 12 },
            lineNumbers: "on",
            renderLineHighlight: "all",
            cursorBlinking: "smooth",
            cursorSmoothCaretAnimation: "on",
            smoothScrolling: true,
            bracketPairColorization: { enabled: true },
          }}
        />
      </div>
    </div>
  )
}
