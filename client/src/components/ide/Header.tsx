import React from "react"
import { Link } from "@tanstack/react-router"
import { Play, RotateCcw, Moon, Sun, Settings, Loader2, Code2, AlertCircle } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { useTheme } from "@/components/theme-provider"
import type { Language } from "@/types/compiler"
import { getLanguageMeta } from "@/constants/languages"
import { LanguageIcon } from "@/components/icons/LanguageIcons"
import {
  useEditorSettings,
  useEditorSettingsActions,
} from "@/stores/editorSettingsStore"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Switch } from "@/components/ui/switch"
import {
  Tooltip,
  TooltipTrigger,
  TooltipContent,
} from "@/components/ui/tooltip"

interface HeaderProps {
  languages: Language[]
  selectedLanguage: Language | null
  onSelectLanguage: (langId: string) => void
  onRun: () => void
  onResetCode: () => void
  isRunning: boolean
  isBackendHealthy: boolean
}

export const Header: React.FC<HeaderProps> = ({
  languages,
  selectedLanguage,
  onSelectLanguage,
  onRun,
  onResetCode,
  isRunning,
  isBackendHealthy,
}) => {
  const { theme, setTheme } = useTheme()
  const editorSettings = useEditorSettings()
  const { setFontSize, setTabSize, toggleMinimap, toggleWordWrap } =
    useEditorSettingsActions()

  const toggleTheme = () => {
    setTheme(theme === "dark" ? "light" : "dark")
  }

  const currentMeta = getLanguageMeta(selectedLanguage)

  return (
    <header className="flex h-12 items-center justify-between border-b bg-card px-3 sm:px-4 shrink-0 select-none">
      {/* Left: Branding & Language Selector */}
      <div className="flex items-center gap-3">
        {/* Brand / Home link via TanStack Router */}
        <Tooltip>
          <TooltipTrigger
            render={
              <Link
                to="/"
                className="flex items-center gap-2 hover:opacity-85 transition-opacity cursor-pointer group"
              >
                <div className="flex h-7 w-7 items-center justify-center rounded-md bg-primary text-primary-foreground shadow-2xs">
                  <Code2 className="h-4 w-4" />
                </div>
                <span className="text-sm font-bold tracking-tight text-foreground">
                  Compilr
                </span>
              </Link>
            }
          />
          <TooltipContent>Return to Home</TooltipContent>
        </Tooltip>

        <span className="text-border h-4 w-px bg-border hidden sm:block" />

        {/* Offline indicator only if backend is down */}
        {!isBackendHealthy && (
          <Tooltip>
            <TooltipTrigger
              render={
                <div className="flex items-center gap-1 text-[11px] text-rose-500 font-medium px-2 py-0.5 rounded-full bg-rose-500/10 cursor-help">
                  <AlertCircle className="h-3 w-3" />
                  <span className="hidden md:inline">Offline</span>
                </div>
              }
            />
            <TooltipContent>Compiler backend is offline</TooltipContent>
          </Tooltip>
        )}

        {/* Compact Language Selector with Icon */}
        <div className="w-40 sm:w-48">
          <Select
            value={selectedLanguage?.id || ""}
            onValueChange={(val) => {
              if (val) onSelectLanguage(val)
            }}
            disabled={isRunning || languages.length === 0}
          >
            <SelectTrigger className="h-8 w-full bg-background text-xs font-medium">
              <SelectValue placeholder="Language">
                <div className="flex items-center gap-2 truncate">
                  <LanguageIcon languageId={selectedLanguage?.id} className="h-3.5 w-3.5 shrink-0" />
                  <span className="truncate">{currentMeta.shortName}</span>
                </div>
              </SelectValue>
            </SelectTrigger>
            <SelectContent>
              {languages.map((lang) => {
                const meta = getLanguageMeta(lang)
                return (
                  <SelectItem key={lang.id} value={lang.id} className="text-xs">
                    <div className="flex items-center justify-between w-full gap-3">
                      <div className="flex items-center gap-2">
                        <LanguageIcon languageId={lang.id} className="h-3.5 w-3.5 shrink-0" />
                        <span className="font-medium">{meta.shortName}</span>
                      </div>
                      <span className="text-[10px] text-muted-foreground font-mono">
                        {meta.tag || lang.version}
                      </span>
                    </div>
                  </SelectItem>
                )
              })}
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Right: Actions (Reset, Settings, Theme, Run) */}
      <div className="flex items-center gap-1.5 sm:gap-2">
        {/* Reset Code Button */}
        <Tooltip>
          <TooltipTrigger
            render={
              <Button
                variant="ghost"
                size="icon"
                onClick={onResetCode}
                disabled={isRunning}
                className="h-8 w-8 text-muted-foreground hover:text-foreground cursor-pointer"
              >
                <RotateCcw className="h-3.5 w-3.5" />
              </Button>
            }
          />
          <TooltipContent>Reset code to template</TooltipContent>
        </Tooltip>

        {/* Editor Settings Dialog */}
        <Dialog>
          <Tooltip>
            <TooltipTrigger
              render={
                <DialogTrigger
                  render={
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-8 w-8 text-muted-foreground hover:text-foreground cursor-pointer"
                    >
                      <Settings className="h-3.5 w-3.5" />
                    </Button>
                  }
                />
              }
            />
            <TooltipContent>Editor settings</TooltipContent>
          </Tooltip>
          <DialogContent className="sm:max-w-xs">
            <DialogHeader>
              <DialogTitle className="text-sm font-semibold">Editor Settings</DialogTitle>
            </DialogHeader>
            <div className="space-y-3.5 py-1 text-xs">
              <div className="flex items-center justify-between">
                <Label htmlFor="font-size" className="text-xs font-medium">
                  Font Size ({editorSettings.fontSize}px)
                </Label>
                <div className="flex items-center gap-1">
                  <Button
                    size="sm"
                    variant="outline"
                    className="h-6 w-6 text-xs p-0"
                    onClick={() => setFontSize(editorSettings.fontSize - 1)}
                  >
                    -
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    className="h-6 w-6 text-xs p-0"
                    onClick={() => setFontSize(editorSettings.fontSize + 1)}
                  >
                    +
                  </Button>
                </div>
              </div>

              <div className="flex items-center justify-between">
                <Label className="text-xs font-medium">Tab Size</Label>
                <div className="flex items-center gap-1">
                  {[2, 4].map((size) => (
                    <Button
                      key={size}
                      size="sm"
                      variant={editorSettings.tabSize === size ? "default" : "outline"}
                      className="h-6 px-2 text-xs"
                      onClick={() => setTabSize(size)}
                    >
                      {size}
                    </Button>
                  ))}
                </div>
              </div>

              <div className="flex items-center justify-between">
                <Label htmlFor="wordwrap-toggle" className="text-xs font-medium">
                  Word Wrap
                </Label>
                <Switch
                  id="wordwrap-toggle"
                  checked={editorSettings.wordWrap === "on"}
                  onCheckedChange={toggleWordWrap}
                />
              </div>

              <div className="flex items-center justify-between">
                <Label htmlFor="minimap-toggle" className="text-xs font-medium">
                  Minimap
                </Label>
                <Switch
                  id="minimap-toggle"
                  checked={editorSettings.minimap}
                  onCheckedChange={toggleMinimap}
                />
              </div>
            </div>
          </DialogContent>
        </Dialog>

        {/* Theme Toggle */}
        <Tooltip>
          <TooltipTrigger
            render={
              <Button
                variant="ghost"
                size="icon"
                onClick={toggleTheme}
                className="h-8 w-8 text-muted-foreground hover:text-foreground cursor-pointer"
              >
                {theme === "dark" ? (
                  <Sun className="h-3.5 w-3.5 text-amber-400" />
                ) : (
                  <Moon className="h-3.5 w-3.5" />
                )}
              </Button>
            }
          />
          <TooltipContent>
            Switch to {theme === "dark" ? "light" : "dark"} mode
          </TooltipContent>
        </Tooltip>

        {/* Run Button */}
        <Tooltip>
          <TooltipTrigger
            render={
              <Button
                onClick={onRun}
                disabled={isRunning || !selectedLanguage}
                size="sm"
                className="h-8 px-3.5 gap-1.5 font-semibold text-xs bg-emerald-600 hover:bg-emerald-500 text-white shadow-2xs cursor-pointer"
              >
                {isRunning ? (
                  <>
                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                    <span>Running...</span>
                  </>
                ) : (
                  <>
                    <Play className="h-3 w-3 fill-current" />
                    <span>Run</span>
                    <kbd className="hidden md:inline-block ml-0.5 rounded bg-black/20 px-1 text-[10px] font-mono text-white/90">
                      ⌘↵
                    </kbd>
                  </>
                )}
              </Button>
            }
          />
          <TooltipContent>Run code (⌘+Enter / Ctrl+Enter)</TooltipContent>
        </Tooltip>
      </div>
    </header>
  )
}
