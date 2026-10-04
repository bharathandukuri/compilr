import React from "react"
import { Play, RotateCcw, Moon, Sun, Terminal, Settings, Loader2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Badge } from "@/components/ui/badge"
import { useTheme } from "@/components/theme-provider"
import type { Language } from "@/types/compiler"
import type { EditorSettings } from "@/utils/storage"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Switch } from "@/components/ui/switch"

interface HeaderProps {
  languages: Language[]
  selectedLanguage: Language | null
  onSelectLanguage: (langId: string) => void
  onRun: () => void
  onResetCode: () => void
  isRunning: boolean
  isBackendHealthy: boolean
  editorSettings: EditorSettings
  onUpdateEditorSettings: (settings: EditorSettings) => void
}

export const Header: React.FC<HeaderProps> = ({
  languages,
  selectedLanguage,
  onSelectLanguage,
  onRun,
  onResetCode,
  isRunning,
  isBackendHealthy,
  editorSettings,
  onUpdateEditorSettings,
}) => {
  const { theme, setTheme } = useTheme()

  const toggleTheme = () => {
    setTheme(theme === "dark" ? "light" : "dark")
  }

  return (
    <header className="flex h-14 items-center justify-between border-b bg-card px-4 shrink-0 select-none">
      {/* Left: Branding & Language Selector */}
      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2 font-bold tracking-tight text-foreground">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground shadow-sm">
            <Terminal className="h-4.5 w-4.5" />
          </div>
          <div className="flex flex-col leading-none">
            <span className="text-base font-extrabold tracking-tight">Compilr</span>
            <span className="text-[10px] text-muted-foreground uppercase font-mono tracking-wider">Online IDE</span>
          </div>
        </div>

        {/* Backend health status indicator */}
        <div className="hidden sm:flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs bg-muted/60 text-muted-foreground">
          {isBackendHealthy ? (
            <>
              <span className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
              <span className="text-[11px] font-medium text-emerald-600 dark:text-emerald-400">Sandbox Ready</span>
            </>
          ) : (
            <>
              <span className="h-2 w-2 rounded-full bg-rose-500" />
              <span className="text-[11px] font-medium text-rose-500">Daemon Offline</span>
            </>
          )}
        </div>

        {/* Language Selector */}
        <div className="w-56">
          <Select
            value={selectedLanguage?.id || ""}
            onValueChange={(val) => {
              if (val) onSelectLanguage(val)
            }}
            disabled={isRunning || languages.length === 0}
          >
            <SelectTrigger className="h-9 w-full bg-background text-xs font-medium">
              <SelectValue placeholder="Select Language..." />
            </SelectTrigger>
            <SelectContent>
              {languages.map((lang) => (
                <SelectItem key={lang.id} value={lang.id} className="text-xs">
                  <div className="flex items-center justify-between w-full gap-2">
                    <span className="font-medium">{lang.name}</span>
                    <Badge variant="outline" className="text-[10px] px-1 py-0 h-4">
                      {lang.type}
                    </Badge>
                  </div>
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Right: Actions (Run, Reset, Settings, Theme) */}
      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="sm"
          onClick={onResetCode}
          disabled={isRunning}
          title="Reset to starter template"
          className="h-9 px-3 text-xs gap-1.5 text-muted-foreground hover:text-foreground"
        >
          <RotateCcw className="h-3.5 w-3.5" />
          <span className="hidden sm:inline">Reset</span>
        </Button>

        {/* Editor Settings Dialog */}
        <Dialog>
          <DialogTrigger
            render={
              <Button
                variant="outline"
                size="icon"
                className="h-9 w-9 text-muted-foreground hover:text-foreground"
                title="Editor Settings"
              >
                <Settings className="h-4 w-4" />
              </Button>
            }
          />
          <DialogContent className="sm:max-w-md">
            <DialogHeader>
              <DialogTitle className="text-sm font-semibold">Editor Preferences</DialogTitle>
            </DialogHeader>
            <div className="space-y-4 py-2 text-xs">
              <div className="flex items-center justify-between">
                <Label htmlFor="minimap-toggle" className="text-xs font-medium">
                  Minimap
                </Label>
                <Switch
                  id="minimap-toggle"
                  checked={editorSettings.minimap}
                  onCheckedChange={(checked) =>
                    onUpdateEditorSettings({ ...editorSettings, minimap: checked })
                  }
                />
              </div>

              <div className="flex items-center justify-between">
                <Label htmlFor="wordwrap-toggle" className="text-xs font-medium">
                  Word Wrap
                </Label>
                <Switch
                  id="wordwrap-toggle"
                  checked={editorSettings.wordWrap === "on"}
                  onCheckedChange={(checked) =>
                    onUpdateEditorSettings({
                      ...editorSettings,
                      wordWrap: checked ? "on" : "off",
                    })
                  }
                />
              </div>

              <div className="flex items-center justify-between">
                <Label className="text-xs font-medium">Font Size ({editorSettings.fontSize}px)</Label>
                <div className="flex items-center gap-1">
                  <Button
                    size="sm"
                    variant="outline"
                    className="h-7 w-7 text-xs p-0"
                    onClick={() =>
                      onUpdateEditorSettings({
                        ...editorSettings,
                        fontSize: Math.max(12, editorSettings.fontSize - 1),
                      })
                    }
                  >
                    -
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    className="h-7 w-7 text-xs p-0"
                    onClick={() =>
                      onUpdateEditorSettings({
                        ...editorSettings,
                        fontSize: Math.min(22, editorSettings.fontSize + 1),
                      })
                    }
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
                      className="h-7 px-2 text-xs"
                      onClick={() =>
                        onUpdateEditorSettings({
                          ...editorSettings,
                          tabSize: size,
                        })
                      }
                    >
                      {size} spaces
                    </Button>
                  ))}
                </div>
              </div>
            </div>
          </DialogContent>
        </Dialog>

        {/* Theme Toggle */}
        <Button
          variant="outline"
          size="icon"
          onClick={toggleTheme}
          title={`Switch to ${theme === "dark" ? "light" : "dark"} mode`}
          className="h-9 w-9 text-muted-foreground hover:text-foreground"
        >
          {theme === "dark" ? (
            <Sun className="h-4 w-4 text-amber-400" />
          ) : (
            <Moon className="h-4 w-4 text-slate-700" />
          )}
        </Button>

        {/* Run Button */}
        <Button
          onClick={onRun}
          disabled={isRunning || !selectedLanguage}
          size="sm"
          className="h-9 px-4 gap-2 font-semibold shadow-sm text-xs bg-emerald-600 hover:bg-emerald-500 text-white dark:bg-emerald-600 dark:hover:bg-emerald-500"
        >
          {isRunning ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              <span>Running...</span>
            </>
          ) : (
            <>
              <Play className="h-3.5 w-3.5 fill-current" />
              <span>Run</span>
              <kbd className="hidden sm:inline-block ml-1 rounded bg-black/20 px-1 py-0.2 text-[10px] font-mono text-white/90">
                ⌘↵
              </kbd>
            </>
          )}
        </Button>
      </div>
    </header>
  )
}
