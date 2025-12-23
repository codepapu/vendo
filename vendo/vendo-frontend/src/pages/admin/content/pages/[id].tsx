import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Save, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Card } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { contentApi, type PageRequest } from '@/services/content-api';
import { RichTextEditor } from '@/components/content/rich-text-editor';

/**
 * Admin page for creating/editing pages
 */
export function AdminPageEditor() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const isEdit = id !== 'new';

  const [formData, setFormData] = useState<PageRequest>({
    title: '',
    slug: '',
    content: '',
    template: '',
    status: 'draft',
    metaTitle: '',
    metaDescription: '',
    metaKeywords: '',
  });

  // Fetch page if editing
  const { data: page, isLoading } = useQuery({
    queryKey: ['page', id],
    queryFn: () => contentApi.getPageById(Number(id!)),
    enabled: isEdit,
  });

  useEffect(() => {
    if (page) {
      setFormData({
        title: page.title,
        slug: page.slug,
        content: page.content || '',
        template: page.template || '',
        status: page.status as 'draft' | 'published' | 'archived',
        metaTitle: page.metaTitle || '',
        metaDescription: page.metaDescription || '',
        metaKeywords: page.metaKeywords || '',
      });
    }
  }, [page]);

  // Generate slug from title
  const generateSlug = (title: string) => {
    return title
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/(^-|-$)/g, '');
  };

  const handleTitleChange = (title: string) => {
    setFormData(prev => ({
      ...prev,
      title,
      slug: prev.slug || generateSlug(title),
    }));
  };

  // Create/Update mutation
  const saveMutation = useMutation({
    mutationFn: (data: PageRequest) => {
      if (isEdit) {
        return contentApi.updatePage(Number(id!), data);
      } else {
        return contentApi.createPage(data);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pages'] });
      navigate('/admin/content/pages');
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    saveMutation.mutate(formData);
  };

  if (isEdit && isLoading) {
    return <div className="container mx-auto py-8 px-4">Loading...</div>;
  }

  return (
    <div className="container mx-auto py-8 px-4 max-w-5xl">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">
          {isEdit ? 'Edit Page' : 'New Page'}
        </h1>
        <Button variant="outline" onClick={() => navigate('/admin/content/pages')}>
          <X className="mr-2 h-4 w-4" />
          Cancel
        </Button>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="space-y-6">
          {/* Basic Info */}
          <Card className="p-6">
            <h2 className="text-xl font-semibold mb-4">Basic Information</h2>
            <div className="space-y-4">
              <div>
                <Label htmlFor="title">Title *</Label>
                <Input
                  id="title"
                  value={formData.title}
                  onChange={(e) => handleTitleChange(e.target.value)}
                  required
                />
              </div>
              <div>
                <Label htmlFor="slug">Slug *</Label>
                <Input
                  id="slug"
                  value={formData.slug}
                  onChange={(e) => setFormData(prev => ({ ...prev, slug: e.target.value }))}
                  required
                />
              </div>
              <div>
                <Label htmlFor="template">Template</Label>
                <Input
                  id="template"
                  value={formData.template}
                  onChange={(e) => setFormData(prev => ({ ...prev, template: e.target.value }))}
                  placeholder="Optional template name"
                />
              </div>
              <div>
                <Label htmlFor="status">Status</Label>
                <select
                  id="status"
                  value={formData.status}
                  onChange={(e) => setFormData(prev => ({ ...prev, status: e.target.value as any }))}
                  className="w-full px-4 py-2 border rounded-md"
                >
                  <option value="draft">Draft</option>
                  <option value="published">Published</option>
                  <option value="archived">Archived</option>
                </select>
              </div>
            </div>
          </Card>

          {/* Content */}
          <Card className="p-6">
            <h2 className="text-xl font-semibold mb-4">Content</h2>
            <div>
              <Label htmlFor="content">Content</Label>
              <RichTextEditor
                value={formData.content || ''}
                onChange={(content) => setFormData(prev => ({ ...prev, content }))}
              />
            </div>
          </Card>

          {/* SEO */}
          <Card className="p-6">
            <h2 className="text-xl font-semibold mb-4">SEO Settings</h2>
            <div className="space-y-4">
              <div>
                <Label htmlFor="metaTitle">Meta Title</Label>
                <Input
                  id="metaTitle"
                  value={formData.metaTitle}
                  onChange={(e) => setFormData(prev => ({ ...prev, metaTitle: e.target.value }))}
                />
              </div>
              <div>
                <Label htmlFor="metaDescription">Meta Description</Label>
                <Textarea
                  id="metaDescription"
                  value={formData.metaDescription}
                  onChange={(e) => setFormData(prev => ({ ...prev, metaDescription: e.target.value }))}
                  rows={3}
                />
              </div>
              <div>
                <Label htmlFor="metaKeywords">Meta Keywords</Label>
                <Input
                  id="metaKeywords"
                  value={formData.metaKeywords}
                  onChange={(e) => setFormData(prev => ({ ...prev, metaKeywords: e.target.value }))}
                  placeholder="Comma-separated keywords"
                />
              </div>
            </div>
          </Card>

          {/* Actions */}
          <div className="flex justify-end gap-4">
            <Button
              type="button"
              variant="outline"
              onClick={() => navigate('/admin/content/pages')}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={saveMutation.isPending}>
              <Save className="mr-2 h-4 w-4" />
              {saveMutation.isPending ? 'Saving...' : 'Save Page'}
            </Button>
          </div>
        </div>
      </form>
    </div>
  );
}
