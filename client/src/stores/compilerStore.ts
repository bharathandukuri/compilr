import { create } from "zustand"
import { persist } from "zustand/middleware"

interface CompilerState {
  selectedLanguageId: string
  codeMap: Record<string, string>
  stdinMap: Record<string, string>
  activeOutputTab: "output" | "stdin"
  isSplitView: boolean
  actions: {
    setSelectedLanguageId: (id: string) => void
    setCode: (langId: string, code: string) => void
    setStdin: (langId: string, stdin: string) => void
    resetCode: (langId: string, defaultStarterCode: string) => void
    setActiveOutputTab: (tab: "output" | "stdin") => void
    toggleSplitView: () => void
  }
}

export const useCompilerStore = create<CompilerState>()(
  persist(
    (set) => ({
      selectedLanguageId: "python-3.12",
      codeMap: {},
      stdinMap: {},
      activeOutputTab: "output",
      isSplitView: false,
      actions: {
        setSelectedLanguageId: (id) =>
          set({ selectedLanguageId: id }),
        setCode: (langId, code) =>
          set((state) => ({
            codeMap: { ...state.codeMap, [langId]: code },
          })),
        setStdin: (langId, stdin) =>
          set((state) => ({
            stdinMap: { ...state.stdinMap, [langId]: stdin },
          })),
        resetCode: (langId, defaultStarterCode) =>
          set((state) => ({
            codeMap: { ...state.codeMap, [langId]: defaultStarterCode },
          })),
        setActiveOutputTab: (tab) =>
          set({ activeOutputTab: tab }),
        toggleSplitView: () =>
          set((state) => ({ isSplitView: !state.isSplitView })),
      },
    }),
    {
      name: "compilr_editor_state",
      partialize: (state) => ({
        selectedLanguageId: state.selectedLanguageId,
        codeMap: state.codeMap,
        stdinMap: state.stdinMap,
      }),
    }
  )
)

export const useSelectedLanguageId = () =>
  useCompilerStore((state) => state.selectedLanguageId)
export const useActiveOutputTab = () =>
  useCompilerStore((state) => state.activeOutputTab)
export const useIsSplitView = () =>
  useCompilerStore((state) => state.isSplitView)
export const useCompilerActions = () =>
  useCompilerStore((state) => state.actions)
