import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Calendar, Eye, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { contentApi } from '@/services/content-api';
import { formatDate } from '@/lib/utils';
import { markdownToHtml } from '@/lib/markdown';

/**
 * Public blog post detail page
 */
export function BlogPostDetail() {
  const { slug } = useParams<{ slug: string }>();
  const navigate = useNavigate();

  const { data: post, isLoading } = useQuery({
    queryKey: ['blog-post', slug],
    queryFn: () => contentApi.getBlogPostBySlug(slug!),
    enabled: !!slug,
  });

  if (isLoading) {
    return (
      <div className="container mx-auto py-8 px-4">
        <div className="text-center py-12">Loading...</div>
      </div>
    );
  }

  if (!post) {
    return (
      <div className="container mx-auto py-8 px-4">
        <Card className="p-12 text-center">
          <p className="text-muted-foreground">Blog post not found.</p>
          <Button onClick={() => navigate('/blog')} className="mt-4">
            Back to Blog
          </Button>
        </Card>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-8 px-4 max-w-4xl">
      <Button
        variant="ghost"
        onClick={() => navigate('/blog')}
        className="mb-6"
      >
        <ArrowLeft className="mr-2 h-4 w-4" />
        Back to Blog
      </Button>

      <article>
        {/* Header */}
        <header className="mb-8">
          <h1 className="text-4xl font-bold mb-4">{post.title}</h1>
          <div className="flex items-center gap-4 text-muted-foreground">
            {post.publishedAt && (
              <div className="flex items-center gap-1">
                <Calendar className="h-4 w-4" />
                <span>{formatDate(post.publishedAt)}</span>
              </div>
            )}
            <div className="flex items-center gap-1">
              <Eye className="h-4 w-4" />
              <span>{post.viewCount} views</span>
            </div>
            {post.authorName && (
              <span>By {post.authorName}</span>
            )}
          </div>
        </header>

        {/* Featured Image */}
        {post.featuredImage && (
          <div className="mb-8">
            <img
              src={post.featuredImage}
              alt={post.title}
              className="w-full rounded-lg"
            />
          </div>
        )}

        {/* Content */}
        <div
          className="prose prose-lg max-w-none mb-8"
          dangerouslySetInnerHTML={{ __html: markdownToHtml(post.content || '') }}
        />

        {/* Tags */}
        {post.tags && post.tags.length > 0 && (
          <div className="flex flex-wrap gap-2 mb-8">
            {post.tags.map((tag) => (
              <span
                key={tag.id}
                className="px-3 py-1 bg-muted rounded-full text-sm"
              >
                {tag.name}
              </span>
            ))}
          </div>
        )}
      </article>
    </div>
  );
}
