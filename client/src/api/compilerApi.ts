import { apiClient, normalizeError } from "./apiClient"
import type { ExecuteRequest, ExecuteResponse, Language } from "@/types/compiler"

export const compilerApi = {
  async execute(request: ExecuteRequest): Promise<ExecuteResponse> {
    try {
      const response = await apiClient.post<ExecuteResponse>("/compiler/execute", request)
      return response.data
    } catch (error) {
      const message = normalizeError(error)
      throw new Error(message)
    }
  },

  async getLanguages(): Promise<Language[]> {
    try {
      const response = await apiClient.get<Language[]>("/compiler/languages")
      return response.data
    } catch (error) {
      const message = normalizeError(error)
      throw new Error(message)
    }
  },

  async getLanguage(id: string): Promise<Language> {
    try {
      const response = await apiClient.get<Language>(`/compiler/languages/${id}`)
      return response.data
    } catch (error) {
      const message = normalizeError(error)
      throw new Error(message)
    }
  },

  async checkHealth(): Promise<boolean> {
    try {
      const response = await apiClient.get<{ status: string }>("/compiler/health")
      return response.data.status === "UP"
    } catch {
      return false
    }
  },
}
