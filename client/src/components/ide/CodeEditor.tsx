import React, { useRef, useEffect } from "react"
import Editor, { type OnMount, type BeforeMount, type Monaco } from "@monaco-editor/react"
import type * as monaco from "monaco-editor"
import { useTheme } from "@/components/theme-provider"
import type { Language } from "@/types/compiler"
import { getLanguageMeta } from "@/constants/languages"
import { LanguageIcon } from "@/components/icons/LanguageIcons"
import { useEditorSettings } from "@/stores/editorSettingsStore"

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
  const monacoRef = useRef<Monaco | null>(null)

  const langMeta = getLanguageMeta(language)

  const handleBeforeMount: BeforeMount = (monacoInstance) => {
    monacoRef.current = monacoInstance

    // Configure JavaScript and TypeScript compiler options & diagnostics
    // Disable noisy semantic validation in online compiler sandbox for JS/TS
    // so require(), Node.js modules, and global variables do not produce false error markers
    const tsDefaults = monacoInstance.languages.typescript.typescriptDefaults
    const jsDefaults = monacoInstance.languages.typescript.javascriptDefaults

    tsDefaults.setDiagnosticsOptions({
      noSemanticValidation: true,
      noSyntaxValidation: false,
    })
    jsDefaults.setDiagnosticsOptions({
      noSemanticValidation: true,
      noSyntaxValidation: false,
    })

    const compilerOptions = {
      target: monacoInstance.languages.typescript.ScriptTarget.ES2022,
      allowNonTextExtensions: true,
      moduleResolution: monacoInstance.languages.typescript.ModuleResolutionKind.NodeJs,
      module: monacoInstance.languages.typescript.ModuleKind.CommonJS,
      noEmit: true,
      lib: ["es2022", "dom"],
    }
    tsDefaults.setCompilerOptions(compilerOptions)
    jsDefaults.setCompilerOptions(compilerOptions)
  }

  const handleEditorMount: OnMount = (editor, monacoInstance: Monaco) => {
    editorRef.current = editor
    monacoRef.current = monacoInstance

    // Clear any stale markers from previous sessions
    monacoInstance.editor.removeAllMarkers?.()

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

  // Clear existing markers whenever the active language changes
  // to prevent errors from one language lingering on another
  useEffect(() => {
    if (monacoRef.current) {
      monacoRef.current.editor.removeAllMarkers?.()
      if (editorRef.current) {
        const model = editorRef.current.getModel()
        if (model) {
          const markers = monacoRef.current.editor.getModelMarkers({ resource: model.uri })
          markers.forEach((m: monaco.editor.IMarker) => {
            monacoRef.current?.editor.setModelMarkers(model, m.owner, [])
          })
        }
      }
    }
  }, [language?.id])

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
      </div>

      {/* Monaco Editor Canvas */}
      <div className="relative flex-1 w-full overflow-hidden">
        <Editor
          height="100%"
          width="100%"
          path={`${language?.id || "default"}/${langMeta.fileName}`}
          language={langMeta.monacoLanguage}
          theme={isDark ? "vs-dark" : "light"}
          value={code}
          beforeMount={handleBeforeMount}
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
