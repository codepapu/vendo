import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Plus, Edit, Trash2, Eye, Search } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card } from '@/components/ui/card';
import { contentApi } from '@/services/content-api';
import { formatDate } from '@/lib/utils';

/**
 * Admin page for managing pages
 */
export function AdminPages() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [searchKeyword, setSearchKeyword] = useState('');

  // Fetch pages
  const { data: pagesData, isLoading } = useQuery({
    queryKey: ['pages', searchKeyword],
    queryFn: () => contentApi.getAllPages({
      keyword: searchKeyword || undefined,
      page: 0,
      size: 50,
    }),
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (id: number) => contentApi.deletePage(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pages'] });
    },
  });

  const handleDelete = (id: number) => {
    if (confirm('Are you sure you want to delete this page? This action cannot be undone.')) {
      deleteMutation.mutate(id);
    }
  };

  const pages = pagesData?.content || [];

  return (
    <div className="container mx-auto py-8 px-4">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold">Pages</h1>
          <p className="text-muted-foreground mt-1">Manage your pages</p>
        </div>
        <Button onClick={() => navigate('/admin/content/pages/new')}>
          <Plus className="mr-2 h-4 w-4" />
          New Page
        </Button>
      </div>

      {/* Search */}
      <Card className="p-4 mb-6">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search pages..."
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            className="pl-10"
          />
        </div>
      </Card>

      {/* Pages List */}
      {isLoading ? (
        <div className="text-center py-12">Loading...</div>
      ) : pages.length === 0 ? (
        <Card className="p-12 text-center">
          <p className="text-muted-foreground">No pages found.</p>
        </Card>
      ) : (
        <div className="space-y-4">
          {pages.map((page) => (
            <Card key={page.id} className="p-6">
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <h3 className="text-xl font-semibold">{page.title}</h3>
                    <span className={`px-2 py-1 rounded text-xs ${
                      page.status === 'published' ? 'bg-green-100 text-green-800' :
                      page.status === 'draft' ? 'bg-gray-100 text-gray-800' :
                      'bg-red-100 text-red-800'
                    }`}>
                      {page.status}
                    </span>
                  </div>
                  <div className="flex items-center gap-4 text-sm text-muted-foreground">
                    <span>Slug: {page.slug}</span>
                    <span>Views: {page.viewCount}</span>
                    <span>Created: {formatDate(page.createdAt)}</span>
                  </div>
                </div>
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => navigate(`/pages/${page.slug}`)}
                  >
                    <Eye className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => navigate(`/admin/content/pages/${page.id}`)}
                  >
                    <Edit className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleDelete(page.id)}
                    disabled={deleteMutation.isPending}
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
