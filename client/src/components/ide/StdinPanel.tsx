import React from "react"
import { Trash2, CornerDownLeft } from "lucide-react"
import { Button } from "@/components/ui/button"

interface StdinPanelProps {
  value: string
  onChange: (value: string) => void
  disabled?: boolean
}

export const StdinPanel: React.FC<StdinPanelProps> = ({
  value,
  onChange,
  disabled = false,
}) => {
  return (
    <div className="flex h-full w-full flex-col bg-background border-t">
      {/* Panel Header */}
      <div className="flex h-8 shrink-0 items-center justify-between border-b bg-muted/40 px-3">
        <div className="flex items-center gap-1.5 text-xs font-semibold text-muted-foreground">
          <CornerDownLeft className="h-3.5 w-3.5" />
          <span>Standard Input (stdin)</span>
        </div>
        <div className="flex items-center gap-2">
          {value.length > 0 && (
            <span className="text-[10px] text-muted-foreground font-mono">
              {value.length} chars
            </span>
          )}
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onChange("")}
            disabled={disabled || value.length === 0}
            className="h-6 px-1.5 text-[11px] text-muted-foreground hover:text-foreground"
            title="Clear stdin"
          >
            <Trash2 className="h-3 w-3 mr-1" />
            Clear
          </Button>
        </div>
      </div>

      {/* Input Textarea */}
      <div className="flex-1 p-2">
        <textarea
          value={value}
          onChange={(e) => onChange(e.target.value)}
          disabled={disabled}
          placeholder="Enter input to pass to program via standard input (one line per argument/input)..."
          className="h-full w-full resize-none rounded-md bg-muted/20 p-2 font-mono text-xs focus:outline-hidden focus:ring-1 focus:ring-primary border border-border/50 text-foreground"
          spellCheck={false}
        />
      </div>
    </div>
  )
}
