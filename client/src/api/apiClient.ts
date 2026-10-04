import axios, { AxiosError } from "axios"
import type { ApiError } from "@/types/compiler"

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api/v1",
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 30000, // 30s network timeout
})

export function normalizeError(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const axiosErr = error as AxiosError<ApiError>
    if (axiosErr.response?.data?.message) {
      return axiosErr.response.data.message
    }
    if (axiosErr.code === "ECONNABORTED") {
      return "Request timed out while waiting for server response."
    }
    if (axiosErr.message) {
      return axiosErr.message
    }
  }
  if (error instanceof Error) {
    return error.message
  }
  return "An unexpected error occurred while communicating with the compiler server."
}
