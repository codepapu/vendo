import { useState, useRef } from 'react';
import { Bold, Italic, Underline, List, Link, Image, Eye, Edit } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Card } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { MediaLibrary } from './media-library';
import { markdownToHtml } from '@/lib/markdown';

interface RichTextEditorProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  showPreview?: boolean;
}

/**
 * Rich text editor component with markdown support
 * Supports markdown formatting with live preview
 */
export function RichTextEditor({ value, onChange, placeholder, showPreview = true }: RichTextEditorProps) {
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const [activeTab, setActiveTab] = useState<'edit' | 'preview'>('edit');

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
  const insertImageFromUrl = (url: string, alt: string = '') => {
    insertText(`![${alt}](${url})`, '');
  };

  return (
    <Card className="overflow-hidden">
      <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as 'edit' | 'preview')}>
        {/* Toolbar */}
        <div className="flex items-center justify-between p-2 border-b bg-muted/50">
          <div className="flex items-center gap-1">
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
            <MediaLibrary
              onSelect={(url) => insertImageFromUrl(url)}
            />
          </div>
          {showPreview && (
            <TabsList className="h-8">
              <TabsTrigger value="edit" className="h-8">
                <Edit className="h-3 w-3 mr-1" />
                Edit
              </TabsTrigger>
              <TabsTrigger value="preview" className="h-8">
                <Eye className="h-3 w-3 mr-1" />
                Preview
              </TabsTrigger>
            </TabsList>
          )}
        </div>

        {/* Editor/Preview */}
        {showPreview ? (
          <>
            <TabsContent value="edit" className="mt-0">
              <Textarea
                ref={textareaRef}
                value={value}
                onChange={(e) => onChange(e.target.value)}
                placeholder={placeholder || 'Start writing...'}
                className="min-h-[300px] border-0 focus-visible:ring-0 resize-none"
              />
            </TabsContent>
            <TabsContent value="preview" className="mt-0">
              <div
                className="min-h-[300px] p-4 prose prose-sm max-w-none"
                dangerouslySetInnerHTML={{ __html: markdownToHtml(value) || '<p class="text-muted-foreground">No content to preview</p>' }}
              />
            </TabsContent>
          </>
        ) : (
          <Textarea
            ref={textareaRef}
            value={value}
            onChange={(e) => onChange(e.target.value)}
            placeholder={placeholder || 'Start writing...'}
            className="min-h-[300px] border-0 focus-visible:ring-0 resize-none"
          />
        )}
      </Tabs>

      {/* Preview hint */}
      <div className="p-2 text-xs text-muted-foreground bg-muted/30 border-t">
        Supports Markdown formatting. Use **bold**, *italic*, [links](url), ![images](url), and more.
      </div>
    </Card>
  );
}
