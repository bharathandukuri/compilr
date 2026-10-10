import { create } from "zustand"
import { persist } from "zustand/middleware"
import type { ExecuteResponse } from "@/types/compiler"

interface CompilerState {
  selectedLanguageId: string
  codeMap: Record<string, string>
  stdinMap: Record<string, string>
  resultMap: Record<string, ExecuteResponse | null>
  activeOutputTab: "output" | "stdin"
  isSplitView: boolean
  actions: {
    setSelectedLanguageId: (id: string) => void
    setCode: (langId: string, code: string) => void
    setStdin: (langId: string, stdin: string) => void
    resetCode: (langId: string, defaultStarterCode: string) => void
    setResult: (langId: string, result: ExecuteResponse | null) => void
    clearResult: (langId: string) => void
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
      resultMap: {},
      activeOutputTab: "output",
      isSplitView: false,
      actions: {
        setSelectedLanguageId: (id) =>
          set((state) => (state.selectedLanguageId === id ? state : { selectedLanguageId: id })),
        setCode: (langId, code) =>
          set((state) => {
            if (state.codeMap[langId] === code) return state
            return {
              codeMap: { ...state.codeMap, [langId]: code },
            }
          }),
        setStdin: (langId, stdin) =>
          set((state) => {
            if (state.stdinMap[langId] === stdin) return state
            return {
              stdinMap: { ...state.stdinMap, [langId]: stdin },
            }
          }),
        resetCode: (langId, defaultStarterCode) =>
          set((state) => ({
            codeMap: { ...state.codeMap, [langId]: defaultStarterCode },
          })),
        setResult: (langId, result) =>
          set((state) => ({
            resultMap: { ...state.resultMap, [langId]: result },
          })),
        clearResult: (langId) =>
          set((state) => {
            if (!state.resultMap[langId]) return state
            return {
              resultMap: { ...state.resultMap, [langId]: null },
            }
          }),
        setActiveOutputTab: (tab) =>
          set((state) => (state.activeOutputTab === tab ? state : { activeOutputTab: tab })),
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
export const useLanguageResult = (langId: string) =>
  useCompilerStore((state) => state.resultMap[langId] ?? null)
export const useLanguageCode = (langId: string, defaultCode = "") =>
  useCompilerStore((state) => state.codeMap[langId] ?? defaultCode)
export const useLanguageStdin = (langId: string) =>
  useCompilerStore((state) => state.stdinMap[langId] ?? "")
