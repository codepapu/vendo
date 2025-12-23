import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Calendar, Eye, ArrowRight } from 'lucide-react';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { contentApi, type BlogPost } from '@/services/content-api';
import { formatDate } from '@/lib/utils';

/**
 * Public blog listing page
 */
export function BlogIndex() {
  const navigate = useNavigate();
  const [searchKeyword, setSearchKeyword] = useState('');

  const { data: postsData, isLoading } = useQuery({
    queryKey: ['blog-posts-public', searchKeyword],
    queryFn: () => contentApi.getBlogPosts({
      status: 'published',
      keyword: searchKeyword || undefined,
      page: 0,
      size: 20,
    }),
  });

  const posts = postsData?.content || [];

  return (
    <div className="container mx-auto py-8 px-4">
      <div className="mb-8">
        <h1 className="text-4xl font-bold mb-4">Blog</h1>
        <p className="text-muted-foreground text-lg">
          Read our latest articles and updates
        </p>
      </div>

      {/* Search */}
      <div className="mb-8">
        <Input
          placeholder="Search blog posts..."
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
          className="max-w-md"
        />
      </div>

      {/* Posts Grid */}
      {isLoading ? (
        <div className="text-center py-12">Loading...</div>
      ) : posts.length === 0 ? (
        <Card className="p-12 text-center">
          <p className="text-muted-foreground">No blog posts found.</p>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {posts.map((post) => (
            <Card
              key={post.id}
              className="overflow-hidden hover:shadow-lg transition-shadow cursor-pointer"
              onClick={() => navigate(`/blog/${post.slug}`)}
            >
              {post.featuredImage && (
                <img
                  src={post.featuredImage}
                  alt={post.title}
                  className="w-full h-48 object-cover"
                />
              )}
              <div className="p-6">
                <h2 className="text-xl font-semibold mb-2 line-clamp-2">
                  {post.title}
                </h2>
                {post.excerpt && (
                  <p className="text-muted-foreground mb-4 line-clamp-3">
                    {post.excerpt}
                  </p>
                )}
                <div className="flex items-center justify-between text-sm text-muted-foreground">
                  <div className="flex items-center gap-4">
                    {post.publishedAt && (
                      <div className="flex items-center gap-1">
                        <Calendar className="h-4 w-4" />
                        <span>{formatDate(post.publishedAt)}</span>
                      </div>
                    )}
                    <div className="flex items-center gap-1">
                      <Eye className="h-4 w-4" />
                      <span>{post.viewCount}</span>
                    </div>
                  </div>
                  <Button variant="ghost" size="sm">
                    Read More <ArrowRight className="ml-1 h-4 w-4" />
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
