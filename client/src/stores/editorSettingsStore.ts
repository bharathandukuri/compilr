import { create } from "zustand"
import { persist } from "zustand/middleware"

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

interface EditorSettingsState {
  settings: EditorSettings
  actions: {
    setFontSize: (fontSize: number) => void
    setTabSize: (tabSize: number) => void
    toggleMinimap: () => void
    toggleWordWrap: () => void
    updateSettings: (partial: Partial<EditorSettings>) => void
  }
}

export const useEditorSettingsStore = create<EditorSettingsState>()(
  persist(
    (set) => ({
      settings: DEFAULT_EDITOR_SETTINGS,
      actions: {
        setFontSize: (fontSize) =>
          set((state) => ({
            settings: {
              ...state.settings,
              fontSize: Math.min(24, Math.max(10, fontSize)),
            },
          })),
        setTabSize: (tabSize) =>
          set((state) => ({
            settings: { ...state.settings, tabSize },
          })),
        toggleMinimap: () =>
          set((state) => ({
            settings: { ...state.settings, minimap: !state.settings.minimap },
          })),
        toggleWordWrap: () =>
          set((state) => ({
            settings: {
              ...state.settings,
              wordWrap: state.settings.wordWrap === "on" ? "off" : "on",
            },
          })),
        updateSettings: (partial) =>
          set((state) => ({
            settings: { ...state.settings, ...partial },
          })),
      },
    }),
    {
      name: "compilr_editor_settings",
      partialize: (state) => ({ settings: state.settings }),
    }
  )
)

export const useEditorSettings = () =>
  useEditorSettingsStore((state) => state.settings)
export const useEditorSettingsActions = () =>
  useEditorSettingsStore((state) => state.actions)
