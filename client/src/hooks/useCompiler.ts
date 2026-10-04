import { useState, useEffect, useCallback, useRef } from "react"
import { compilerApi } from "@/api/compilerApi"
import type { Language, ExecuteResponse, ExecuteRequest } from "@/types/compiler"
import { storage, type EditorSettings } from "@/utils/storage"

export function useCompiler() {
  const [languages, setLanguages] = useState<Language[]>([])
  const [selectedLanguage, setSelectedLanguage] = useState<Language | null>(null)
  const [code, setCode] = useState<string>("")
  const [stdin, setStdin] = useState<string>("")
  const [isRunning, setIsRunning] = useState<boolean>(false)
  const [result, setResult] = useState<ExecuteResponse | null>(null)
  const [isBackendHealthy, setIsBackendHealthy] = useState<boolean>(true)
  const [editorSettings, setEditorSettings] = useState<EditorSettings>(storage.getEditorSettings())

  const languagesRef = useRef<Language[]>([])
  languagesRef.current = languages

  // 1. Fetch supported languages on mount
  useEffect(() => {
    let mounted = true

    async function init() {
      try {
        const langs = await compilerApi.getLanguages()
        if (!mounted) return

        setLanguages(langs)
        setIsBackendHealthy(true)

        // Find saved language or default to java-21
        const savedLangId = storage.getActiveLanguage("java-21")
        const activeLang = langs.find((l) => l.id === savedLangId) || langs[0] || null

        if (activeLang) {
          setSelectedLanguage(activeLang)
          const savedCode = storage.getCode(activeLang.id, activeLang.defaultStarterCode)
          const savedStdin = storage.getStdin(activeLang.id)
          setCode(savedCode)
          setStdin(savedStdin)
        }
      } catch (err) {
        console.error("Failed to load languages:", err)
        if (mounted) setIsBackendHealthy(false)
      }
    }

    init()

    // Poll backend health check every 15 seconds
    const interval = setInterval(async () => {
      const healthy = await compilerApi.checkHealth()
      if (mounted) setIsBackendHealthy(healthy)
    }, 15000)

    return () => {
      mounted = false
      clearInterval(interval)
    }
  }, [])

  // 2. Select Language handler
  const handleSelectLanguage = useCallback((langId: string) => {
    const nextLang = languagesRef.current.find((l) => l.id === langId)
    if (!nextLang) return

    setSelectedLanguage((prev) => {
      // Save current code before switching
      if (prev) {
        storage.setCode(prev.id, code)
        storage.setStdin(prev.id, stdin)
      }

      // Load new language code
      const nextCode = storage.getCode(nextLang.id, nextLang.defaultStarterCode)
      const nextStdin = storage.getStdin(nextLang.id)
      setCode(nextCode)
      setStdin(nextStdin)
      storage.setActiveLanguage(nextLang.id)

      return nextLang
    })
  }, [code, stdin])

  // 3. Update Code handler
  const handleCodeChange = useCallback((newCode: string) => {
    setCode(newCode)
    if (selectedLanguage) {
      storage.setCode(selectedLanguage.id, newCode)
    }
  }, [selectedLanguage])

  // 4. Update Stdin handler
  const handleStdinChange = useCallback((newStdin: string) => {
    setStdin(newStdin)
    if (selectedLanguage) {
      storage.setStdin(selectedLanguage.id, newStdin)
    }
  }, [selectedLanguage])

  // 5. Reset code to starter template
  const handleResetCode = useCallback(() => {
    if (!selectedLanguage) return
    const defaultCode = selectedLanguage.defaultStarterCode
    setCode(defaultCode)
    storage.setCode(selectedLanguage.id, defaultCode)
  }, [selectedLanguage])

  // 6. Execute Code handler
  const handleRun = useCallback(async () => {
    if (!selectedLanguage || isRunning) return

    setIsRunning(true)
    setResult(null)

    const request: ExecuteRequest = {
      language: selectedLanguage.id,
      sourceCode: code,
      stdin: stdin,
      options: {
        timeLimitMs: 5000,
        memoryLimitKb: 262144,
      },
    }

    try {
      const response = await compilerApi.execute(request)
      setResult(response)
      setIsBackendHealthy(true)
    } catch (error) {
      console.error("Execution error:", error)
      const errorMessage = error instanceof Error ? error.message : "Execution failed"
      setResult({
        executionId: "err-" + Date.now(),
        language: selectedLanguage.id,
        status: "SYSTEM_ERROR",
        stderr: errorMessage,
        error: errorMessage,
      })
    } finally {
      setIsRunning(false)
    }
  }, [selectedLanguage, code, stdin, isRunning])

  // 7. Update Editor Settings handler
  const handleUpdateEditorSettings = useCallback((newSettings: EditorSettings) => {
    setEditorSettings(newSettings)
    storage.setEditorSettings(newSettings)
  }, [])

  return {
    languages,
    selectedLanguage,
    code,
    stdin,
    isRunning,
    result,
    isBackendHealthy,
    editorSettings,
    selectLanguage: handleSelectLanguage,
    setCode: handleCodeChange,
    setStdin: handleStdinChange,
    resetCode: handleResetCode,
    runCode: handleRun,
    clearResult: () => setResult(null),
    updateEditorSettings: handleUpdateEditorSettings,
  }
}
