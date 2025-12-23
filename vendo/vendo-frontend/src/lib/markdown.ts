/**
 * Simple markdown to HTML converter
 * For production, consider using a library like marked or markdown-it
 */

export function markdownToHtml(markdown: string): string {
  if (!markdown) return '';
  
  let html = markdown;
  
  // Headers (must be done first, before other formatting)
  html = html.replace(/^###### (.*$)/gim, '<h6>$1</h6>');
  html = html.replace(/^##### (.*$)/gim, '<h5>$1</h5>');
  html = html.replace(/^#### (.*$)/gim, '<h4>$1</h4>');
  html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>');
  html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>');
  html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>');
  
  // Code blocks (before inline code)
  html = html.replace(/```([\s\S]*?)```/gim, '<pre><code>$1</code></pre>');
  
  // Inline code
  html = html.replace(/`([^`]+)`/gim, '<code>$1</code>');
  
  // Bold (must be before italic to avoid conflicts)
  html = html.replace(/\*\*(.*?)\*\*/gim, '<strong>$1</strong>');
  html = html.replace(/__(.*?)__/gim, '<strong>$1</strong>');
  
  // Italic
  html = html.replace(/\*(.*?)\*/gim, '<em>$1</em>');
  html = html.replace(/_(.*?)_/gim, '<em>$1</em>');
  
  // Underline (HTML tags)
  html = html.replace(/<u>(.*?)<\/u>/gim, '<u>$1</u>');
  
  // Links
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/gim, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>');
  
  // Images
  html = html.replace(/!\[([^\]]*)\]\(([^)]+)\)/gim, '<img src="$2" alt="$1" class="max-w-full h-auto rounded-lg" />');
  
  // Horizontal rule
  html = html.replace(/^---$/gim, '<hr />');
  html = html.replace(/^\*\*\*$/gim, '<hr />');
  
  // Blockquotes
  html = html.replace(/^> (.*$)/gim, '<blockquote>$1</blockquote>');
  
  // Unordered lists (handle multi-line)
  const lines = html.split('\n');
  let inList = false;
  const processedLines: string[] = [];
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    if (/^[-*+] (.+)$/.test(line)) {
      if (!inList) {
        processedLines.push('<ul>');
        inList = true;
      }
      processedLines.push(line.replace(/^[-*+] (.+)$/, '<li>$1</li>'));
    } else {
      if (inList) {
        processedLines.push('</ul>');
        inList = false;
      }
      processedLines.push(line);
    }
  }
  if (inList) {
    processedLines.push('</ul>');
  }
  html = processedLines.join('\n');
  
  // Ordered lists
  html = html.replace(/^(\d+)\. (.+)$/gim, '<ol start="$1"><li>$2</li></ol>');
  // Clean up nested ordered lists
  html = html.replace(/<\/ol>\s*<ol start="(\d+)">/gim, '');
  
  // Paragraphs (wrap consecutive non-empty lines)
  html = html.split('\n\n').map(para => {
    para = para.trim();
    if (!para) return '';
    if (para.startsWith('<') && (para.startsWith('<h') || para.startsWith('<ul') || para.startsWith('<ol') || para.startsWith('<pre') || para.startsWith('<blockquote'))) {
      return para;
    }
    return `<p>${para}</p>`;
  }).join('\n\n');
  
  // Line breaks
  html = html.replace(/\n/gim, '<br />');
  
  return html;
}
