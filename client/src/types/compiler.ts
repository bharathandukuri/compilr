export type ExecutionStatus =
  | "SUCCESS"
  | "COMPILATION_ERROR"
  | "RUNTIME_ERROR"
  | "TIME_LIMIT_EXCEEDED"
  | "MEMORY_LIMIT_EXCEEDED"
  | "OUTPUT_LIMIT_EXCEEDED"
  | "SYSTEM_ERROR"
  | "RATE_LIMITED"
  | "CAPACITY_EXCEEDED"

export interface Language {
  id: string
  name: string
  version: string
  type: "COMPILED" | "INTERPRETED" | "DATABASE"
  fileExtension: string
  compiled: boolean
  defaultStarterCode: string
  aliases: string[]
}

export interface CompilerOptions {
  timeLimitMs?: number
  memoryLimitKb?: number
}

export interface ExecuteRequest {
  language: string
  sourceCode: string
  stdin?: string
  options?: CompilerOptions
}

export interface ExecuteResponse {
  executionId: string
  language: string
  status: ExecutionStatus
  stdout?: string
  stderr?: string
  exitCode?: number
  exitSignal?: number
  executionTimeMs?: number
  memoryUsageKb?: number
  logs?: string[]
  error?: string
}

export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  executionId?: string
}
