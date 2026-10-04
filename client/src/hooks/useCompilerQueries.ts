import { useQuery, useMutation } from "@tanstack/react-query"
import { compilerApi } from "@/api/compilerApi"
import { SUPPORTED_LANGUAGES } from "@/constants/languages"
import type { ExecuteRequest, ExecuteResponse, Language } from "@/types/compiler"

export const compilerQueryKeys = {
  all: ["compiler"] as const,
  languages: () => [...compilerQueryKeys.all, "languages"] as const,
  health: () => [...compilerQueryKeys.all, "health"] as const,
}

export function useLanguagesQuery() {
  return useQuery<Language[]>({
    queryKey: compilerQueryKeys.languages(),
    queryFn: async () => {
      try {
        const langs = await compilerApi.getLanguages()
        return langs && langs.length > 0 ? langs : SUPPORTED_LANGUAGES
      } catch (err) {
        console.warn("Failed to fetch languages from backend, using fallbacks:", err)
        return SUPPORTED_LANGUAGES
      }
    },
    initialData: SUPPORTED_LANGUAGES,
    staleTime: 1000 * 60 * 5,
  })
}

export function useBackendHealthQuery() {
  return useQuery<boolean>({
    queryKey: compilerQueryKeys.health(),
    queryFn: () => compilerApi.checkHealth(),
    refetchInterval: 20000,
    staleTime: 10000,
  })
}

export function useExecuteMutation() {
  return useMutation<ExecuteResponse, Error, ExecuteRequest>({
    mutationFn: (request) => compilerApi.execute(request),
  })
}
