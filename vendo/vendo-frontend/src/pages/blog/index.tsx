import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Calendar, Eye, ArrowRight } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { contentApi } from '@/services/content-api';
import { formatDate } from '@/lib/utils';

/**
 * Public blog listing page
 */
export function BlogIndex() {
  const navigate = useNavigate();

  const { data: postsData, isLoading } = useQuery({
    queryKey: ['blog-posts', 'published'],
    queryFn: () => contentApi.getBlogPosts({
      status: 'published',
      page: 0,
      size: 20,
    }),
  });

  if (isLoading) {
    return (
      <div className="container mx-auto py-8 px-4">
        <div className="text-center py-12">Loading...</div>
      </div>
    );
  }

  const posts = postsData?.content || [];

  return (
    <div className="container mx-auto py-8 px-4 max-w-6xl">
      {/* Header */}
      <header className="mb-12 text-center">
        <h1 className="text-4xl font-bold mb-4">Blog</h1>
        <p className="text-muted-foreground text-lg">
          Latest news, updates, and insights
        </p>
      </header>

      {/* Blog Posts Grid */}
      {posts.length === 0 ? (
        <Card className="p-12 text-center">
          <p className="text-muted-foreground">No blog posts available yet.</p>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {posts.map((post) => (
            <Card
              key={post.id}
              className="overflow-hidden hover:shadow-lg transition-shadow cursor-pointer"
              onClick={() => navigate(`/blog/${post.slug}`)}
            >
              {/* Featured Image */}
              {post.featuredImage && (
                <div className="aspect-video overflow-hidden bg-muted">
                  <img
                    src={post.featuredImage}
                    alt={post.title}
                    className="w-full h-full object-cover hover:scale-105 transition-transform"
                  />
                </div>
              )}

              {/* Content */}
              <div className="p-6">
                <h2 className="text-xl font-semibold mb-2 line-clamp-2">
                  {post.title}
                </h2>
                
                {post.excerpt && (
                  <p className="text-muted-foreground mb-4 line-clamp-3">
                    {post.excerpt}
                  </p>
                )}

                {/* Meta Info */}
                <div className="flex items-center gap-4 text-sm text-muted-foreground mb-4">
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

                {/* Read More */}
                <Button
                  variant="ghost"
                  className="w-full"
                  onClick={(e) => {
                    e.stopPropagation();
                    navigate(`/blog/${post.slug}`);
                  }}
                >
                  Read More
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
