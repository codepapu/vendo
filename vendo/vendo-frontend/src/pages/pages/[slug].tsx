import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Card } from '@/components/ui/card';
import { contentApi } from '@/services/content-api';
import { markdownToHtml } from '@/lib/markdown';

/**
 * Dynamic page rendering based on slug
 */
export function DynamicPage() {
  const { slug } = useParams<{ slug: string }>();

  const { data: page, isLoading } = useQuery({
    queryKey: ['page', slug],
    queryFn: () => contentApi.getPageBySlug(slug!),
    enabled: !!slug,
  });

  if (isLoading) {
    return (
      <div className="container mx-auto py-8 px-4">
        <div className="text-center py-12">Loading...</div>
      </div>
    );
  }

  if (!page) {
    return (
      <div className="container mx-auto py-8 px-4">
        <Card className="p-12 text-center">
          <h1 className="text-2xl font-bold mb-4">Page Not Found</h1>
          <p className="text-muted-foreground">
            The page you're looking for doesn't exist.
          </p>
        </Card>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-8 px-4 max-w-4xl">
      <article>
        <header className="mb-8">
          <h1 className="text-4xl font-bold mb-4">{page.title}</h1>
        </header>

        <div
          className="prose prose-lg max-w-none"
          dangerouslySetInnerHTML={{ __html: markdownToHtml(page.content || '') }}
        />
      </article>
    </div>
  );
}
