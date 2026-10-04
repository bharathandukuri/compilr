const STORAGE_KEYS = {
  ACTIVE_LANGUAGE: "compilr_active_language",
  CODE_PREFIX: "compilr_code_",
  STDIN_PREFIX: "compilr_stdin_",
  THEME: "compilr_theme",
  EDITOR_SETTINGS: "compilr_editor_settings",
}

export interface EditorSettings {
  fontSize: number
  tabSize: number
  minimap: boolean
  wordWrap: "on" | "off"
}

export const DEFAULT_EDITOR_SETTINGS: EditorSettings = {
  fontSize: 14,
  tabSize: 4,
  minimap: false,
  wordWrap: "on",
}

export const storage = {
  getActiveLanguage(fallback: string = "java-21"): string {
    return localStorage.getItem(STORAGE_KEYS.ACTIVE_LANGUAGE) || fallback
  },

  setActiveLanguage(langId: string): void {
    localStorage.setItem(STORAGE_KEYS.ACTIVE_LANGUAGE, langId)
  },

  getCode(langId: string, defaultCode: string): string {
    const saved = localStorage.getItem(`${STORAGE_KEYS.CODE_PREFIX}${langId}`)
    return saved !== null ? saved : defaultCode
  },

  setCode(langId: string, code: string): void {
    localStorage.setItem(`${STORAGE_KEYS.CODE_PREFIX}${langId}`, code)
  },

  clearCode(langId: string): void {
    localStorage.removeItem(`${STORAGE_KEYS.CODE_PREFIX}${langId}`)
  },

  getStdin(langId: string): string {
    return localStorage.getItem(`${STORAGE_KEYS.STDIN_PREFIX}${langId}`) || ""
  },

  setStdin(langId: string, stdin: string): void {
    localStorage.setItem(`${STORAGE_KEYS.STDIN_PREFIX}${langId}`, stdin)
  },

  getEditorSettings(): EditorSettings {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.EDITOR_SETTINGS)
      return data ? { ...DEFAULT_EDITOR_SETTINGS, ...JSON.parse(data) } : DEFAULT_EDITOR_SETTINGS
    } catch {
      return DEFAULT_EDITOR_SETTINGS
    }
  },

  setEditorSettings(settings: EditorSettings): void {
    localStorage.setItem(STORAGE_KEYS.EDITOR_SETTINGS, JSON.stringify(settings))
  },
}
