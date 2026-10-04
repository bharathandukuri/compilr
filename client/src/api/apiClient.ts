import axios, { AxiosError } from "axios"
import type { ApiError } from "@/types/compiler"

export class CompilerApiError extends Error {
  status: number
  retryAfter?: number

  constructor(message: string, status: number, retryAfter?: number) {
    super(message)
    this.name = "CompilerApiError"
    this.status = status
    this.retryAfter = retryAfter
  }
}

export function getOrCreateClientId(): string {
  const KEY = "compilr_client_id"
  try {
    let id = localStorage.getItem(KEY)
    if (!id) {
      id = "client-" + (typeof crypto !== "undefined" && crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2, 11))
      localStorage.setItem(KEY, id)
    }
    return id
  } catch {
    return "client-anon"
  }
}

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api/v1",
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 30000, // 30s network timeout
})

// Attach client identifier header to all requests for sliding window rate limiting
apiClient.interceptors.request.use((config) => {
  config.headers = config.headers || {}
  config.headers["X-Client-Id"] = getOrCreateClientId()
  return config
})

export function parseCompilerError(error: unknown): CompilerApiError {
  if (axios.isAxiosError(error)) {
    const axiosErr = error as AxiosError<ApiError>
    const status = axiosErr.response?.status ?? 500
    const retryAfterHeader = axiosErr.response?.headers?.["retry-after"]
    const retryAfter = retryAfterHeader ? parseInt(String(retryAfterHeader), 10) : undefined

    if (status === 429) {
      const waitMsg = retryAfter ? ` Please wait ${retryAfter} seconds before trying again.` : " Please slow down your requests."
      const serverMsg = axiosErr.response?.data?.message || `Rate limit exceeded.${waitMsg}`
      return new CompilerApiError(serverMsg, 429, retryAfter)
    }

    if (status === 503) {
      const serverMsg = axiosErr.response?.data?.message || "Compiler execution capacity is temporarily exhausted. The server is busy, please retry in a moment."
      return new CompilerApiError(serverMsg, 503, retryAfter)
    }

    if (status === 504) {
      const serverMsg = axiosErr.response?.data?.message || "Execution request timed out waiting in the environment queue. Please try again."
      return new CompilerApiError(serverMsg, 504)
    }

    if (axiosErr.response?.data?.message) {
      return new CompilerApiError(axiosErr.response.data.message, status)
    }

    if (axiosErr.code === "ECONNABORTED") {
      return new CompilerApiError("Request timed out while waiting for compiler server response.", 504)
    }

    if (axiosErr.message) {
      return new CompilerApiError(axiosErr.message, status)
    }
  }

  if (error instanceof CompilerApiError) {
    return error
  }

  if (error instanceof Error) {
    return new CompilerApiError(error.message, 500)
  }

  return new CompilerApiError("An unexpected error occurred while communicating with the compiler server.", 500)
}

export function normalizeError(error: unknown): string {
  return parseCompilerError(error).message
}
