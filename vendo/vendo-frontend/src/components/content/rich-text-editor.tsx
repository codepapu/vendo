import { useState, useRef, useEffect } from 'react';
import { Bold, Italic, Underline, List, Link, Image } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Card } from '@/components/ui/card';

interface RichTextEditorProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}

/**
 * Simple rich text editor component
 * For a production app, consider using a library like TipTap, Quill, or Draft.js
 */
export function RichTextEditor({ value, onChange, placeholder }: RichTextEditorProps) {
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const insertText = (before: string, after: string = '') => {
    const textarea = textareaRef.current;
    if (!textarea) return;

    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const selectedText = value.substring(start, end);
    const newText = value.substring(0, start) + before + selectedText + after + value.substring(end);
    
    onChange(newText);
    
    // Restore cursor position
    setTimeout(() => {
      textarea.focus();
      const newPosition = start + before.length + selectedText.length;
      textarea.setSelectionRange(newPosition, newPosition);
    }, 0);
  };

  const formatBold = () => insertText('**', '**');
  const formatItalic = () => insertText('*', '*');
  const formatUnderline = () => insertText('<u>', '</u>');
  const insertList = () => insertText('- ', '\n');
  const insertLink = () => {
    const url = prompt('Enter URL:');
    if (url) {
      const text = prompt('Enter link text:', url);
      insertText(`[${text || url}](${url})`, '');
    }
  };
  const insertImage = () => {
    const url = prompt('Enter image URL:');
    if (url) {
      const alt = prompt('Enter alt text:', '');
      insertText(`![${alt || ''}](${url})`, '');
    }
  };

  return (
    <Card className="overflow-hidden">
      {/* Toolbar */}
      <div className="flex items-center gap-1 p-2 border-b bg-muted/50">
        <Button
          type="button"
          variant="ghost"
          size="sm"
          onClick={formatBold}
          title="Bold"
        >
          <Bold className="h-4 w-4" />
        </Button>
        <Button
          type="button"
          variant="ghost"
          size="sm"
          onClick={formatItalic}
          title="Italic"
        >
          <Italic className="h-4 w-4" />
        </Button>
        <Button
          type="button"
          variant="ghost"
          size="sm"
          onClick={formatUnderline}
          title="Underline"
        >
          <Underline className="h-4 w-4" />
        </Button>
        <div className="w-px h-6 bg-border mx-1" />
        <Button
          type="button"
          variant="ghost"
          size="sm"
          onClick={insertList}
          title="List"
        >
          <List className="h-4 w-4" />
        </Button>
        <Button
          type="button"
          variant="ghost"
          size="sm"
          onClick={insertLink}
          title="Link"
        >
          <Link className="h-4 w-4" />
        </Button>
        <Button
          type="button"
          variant="ghost"
          size="sm"
          onClick={insertImage}
          title="Image"
        >
          <Image className="h-4 w-4" />
        </Button>
      </div>

      {/* Editor */}
      <Textarea
        ref={textareaRef}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder || 'Start writing...'}
        className="min-h-[300px] border-0 focus-visible:ring-0 resize-none"
      />

      {/* Preview hint */}
      <div className="p-2 text-xs text-muted-foreground bg-muted/30 border-t">
        Supports Markdown formatting. Use **bold**, *italic*, [links](url), and more.
      </div>
    </Card>
  );
}
