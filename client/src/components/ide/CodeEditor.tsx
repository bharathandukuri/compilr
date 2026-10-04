import React, { useRef, useEffect, useState } from "react"
import Editor, { type OnMount, type Monaco } from "@monaco-editor/react"
import type * as monaco from "monaco-editor"
import { useTheme } from "@/components/theme-provider"
import type { Language } from "@/types/compiler"
import { getLanguageMeta } from "@/constants/languages"
import { LanguageIcon } from "@/components/icons/LanguageIcons"
import { useEditorSettings } from "@/stores/editorSettingsStore"
import { Copy, Check } from "lucide-react"
import { Button } from "@/components/ui/button"

interface CodeEditorProps {
  language: Language | null
  code: string
  onChange: (value: string) => void
  onRun: () => void
}

export const CodeEditor: React.FC<CodeEditorProps> = ({
  language,
  code,
  onChange,
  onRun,
}) => {
  const { theme } = useTheme()
  const settings = useEditorSettings()
  const editorRef = useRef<monaco.editor.IStandaloneCodeEditor | null>(null)
  const [copied, setCopied] = useState(false)

  const langMeta = getLanguageMeta(language)

  const handleCopy = () => {
    if (code) {
      navigator.clipboard.writeText(code)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    }
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

    // Format document shortcut (Cmd+Shift+F / Ctrl+Shift+F)
    editor.addCommand(
      monacoInstance.KeyMod.CtrlCmd | monacoInstance.KeyMod.Shift | monacoInstance.KeyCode.KeyF,
      () => {
        editor.getAction("editor.action.formatDocument")?.run()
      }
    )
  }

  // Update editor settings dynamically from Zustand store
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

  const isDark =
    theme === "dark" ||
    (theme === "system" &&
      typeof window !== "undefined" &&
      window.matchMedia("(prefers-color-scheme: dark)").matches)

  return (
    <div className="flex h-full w-full flex-col bg-background overflow-hidden">
      {/* Compact Editor Header */}
      <div className="flex h-8 shrink-0 items-center justify-between border-b bg-muted/20 px-3 select-none">
        <div className="flex items-center gap-2 text-xs font-mono text-muted-foreground">
          <LanguageIcon languageId={language?.id} className="h-3.5 w-3.5 shrink-0" />
          <span className="font-medium text-foreground">{langMeta.fileName}</span>
        </div>

        <div className="flex items-center gap-1">
          <Button
            variant="ghost"
            size="sm"
            onClick={handleCopy}
            className="h-6 px-2 text-[11px] text-muted-foreground hover:text-foreground gap-1"
            title="Copy code"
          >
            {copied ? (
              <>
                <Check className="h-3 w-3 text-emerald-500" />
                <span className="text-emerald-500">Copied</span>
              </>
            ) : (
              <>
                <Copy className="h-3 w-3" />
                <span>Copy</span>
              </>
            )}
          </Button>
        </div>
      </div>

      {/* Monaco Editor Canvas */}
      <div className="relative flex-1 w-full overflow-hidden">
        <Editor
          height="100%"
          width="100%"
          language={langMeta.monacoLanguage}
          theme={isDark ? "vs-dark" : "light"}
          value={code}
          onChange={(val) => onChange(val || "")}
          onMount={handleEditorMount}
          loading={
            <div className="flex h-full w-full items-center justify-center text-xs text-muted-foreground">
              Loading editor...
            </div>
          }
          options={{
            fontSize: settings.fontSize,
            tabSize: settings.tabSize,
            wordWrap: settings.wordWrap,
            minimap: { enabled: settings.minimap },
            fontFamily:
              "'JetBrains Mono', 'Fira Code', 'Cascadia Code', Menlo, Monaco, Consolas, monospace",
            fontLigatures: true,
            scrollBeyondLastLine: false,
            automaticLayout: true,
            padding: { top: 8, bottom: 8 },
            lineNumbers: "on",
            renderLineHighlight: "line",
            cursorBlinking: "smooth",
            smoothScrolling: true,
            bracketPairColorization: { enabled: true },
          }}
        />
      </div>
    </div>
  )
}
