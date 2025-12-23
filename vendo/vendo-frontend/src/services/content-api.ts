import { apiClient, handleApiError, showSuccessToast } from '@/lib/api';
import type { ApiResponse } from '@/types';

/**
 * Content Management API service
 * Handles all content-related API calls (blog posts, pages, media)
 */

// Blog Post Types
export interface BlogPost {
  id: number;
  title: string;
  slug: string;
  excerpt?: string;
  content?: string;
  featuredImage?: string;
  status: 'draft' | 'published' | 'archived';
  categoryId?: number;
  categoryName?: string;
  authorId: number;
  authorName?: string;
  metaTitle?: string;
  metaDescription?: string;
  metaKeywords?: string;
  viewCount: number;
  publishedAt?: string;
  tags?: BlogTag[];
  comments?: BlogComment[];
  createdAt: string;
  updatedAt: string;
}

export interface BlogTag {
  id: number;
  name: string;
  slug: string;
  postCount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface BlogComment {
  id: number;
  postId: number;
  userId: number;
  userName?: string;
  content: string;
  parentId?: number;
  isApproved: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface BlogPostRequest {
  title: string;
  slug: string;
  excerpt?: string;
  content?: string;
  featuredImage?: string;
  status?: 'draft' | 'published' | 'archived';
  categoryId?: number;
  tagIds?: number[];
  metaTitle?: string;
  metaDescription?: string;
  metaKeywords?: string;
}

// Page Types
export interface Page {
  id: number;
  title: string;
  slug: string;
  content?: string;
  template?: string;
  status: 'draft' | 'published' | 'archived';
  metaTitle?: string;
  metaDescription?: string;
  metaKeywords?: string;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface PageRequest {
  title: string;
  slug: string;
  content?: string;
  template?: string;
  status?: 'draft' | 'published' | 'archived';
  metaTitle?: string;
  metaDescription?: string;
  metaKeywords?: string;
}

// Media Types
export interface MediaFile {
  id: number;
  filename: string;
  originalName: string;
  path: string;
  url: string;
  mimeType?: string;
  fileSize: number;
  type: 'image' | 'video' | 'audio' | 'document';
  userId?: number;
  alt?: string;
  description?: string;
  width?: number;
  height?: number;
  duration?: number;
  cdnUrl?: string;
  createdAt: string;
  updatedAt: string;
}

// Content Response Types
export interface ContentResponse {
  blogPosts?: BlogPost[];
  pages?: Page[];
  mediaFiles?: MediaFile[];
}

// Paginated Response
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

class ContentApiService {
  // Blog Post Methods
  async getBlogPosts(params?: {
    status?: string;
    categoryId?: number;
    keyword?: string;
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<BlogPost> | null> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<BlogPost>>>(
        '/v1/blog/posts',
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch blog posts');
      return null;
    }
  }

  async getBlogPostById(id: number): Promise<BlogPost | null> {
    try {
      const response = await apiClient.get<ApiResponse<BlogPost>>(
        `/v1/blog/posts/${id}`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch blog post');
      return null;
    }
  }

  async getBlogPostBySlug(slug: string): Promise<BlogPost | null> {
    try {
      const response = await apiClient.get<ApiResponse<BlogPost>>(
        `/v1/blog/posts/slug/${slug}`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch blog post');
      return null;
    }
  }

  async createBlogPost(data: BlogPostRequest): Promise<BlogPost | null> {
    try {
      const response = await apiClient.post<ApiResponse<BlogPost>>(
        '/v1/blog/posts',
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Blog post created successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to create blog post');
      return null;
    }
  }

  async updateBlogPost(id: number, data: BlogPostRequest): Promise<BlogPost | null> {
    try {
      const response = await apiClient.put<ApiResponse<BlogPost>>(
        `/v1/blog/posts/${id}`,
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Blog post updated successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to update blog post');
      return null;
    }
  }

  async publishBlogPost(id: number): Promise<BlogPost | null> {
    try {
      const response = await apiClient.put<ApiResponse<BlogPost>>(
        `/v1/blog/posts/${id}/publish`
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Blog post published successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to publish blog post');
      return null;
    }
  }

  async deleteBlogPost(id: number): Promise<boolean> {
    try {
      const response = await apiClient.delete<ApiResponse<void>>(
        `/v1/blog/posts/${id}`
      );
      
      if (response.data.success) {
        showSuccessToast(response.data.message || 'Blog post deleted successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to delete blog post');
      return false;
    }
  }

  // Page Methods
  async getPageBySlug(slug: string): Promise<Page | null> {
    try {
      const response = await apiClient.get<ApiResponse<Page>>(
        `/v1/pages/slug/${slug}`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch page');
      return null;
    }
  }

  async getPageById(id: number): Promise<Page | null> {
    try {
      const response = await apiClient.get<ApiResponse<Page>>(
        `/v1/pages/${id}`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch page');
      return null;
    }
  }

  async createPage(data: PageRequest): Promise<Page | null> {
    try {
      const response = await apiClient.post<ApiResponse<Page>>(
        '/v1/pages',
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Page created successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to create page');
      return null;
    }
  }

  async updatePage(id: number, data: PageRequest): Promise<Page | null> {
    try {
      const response = await apiClient.put<ApiResponse<Page>>(
        `/v1/pages/${id}`,
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Page updated successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to update page');
      return null;
    }
  }

  async deletePage(id: number): Promise<boolean> {
    try {
      const response = await apiClient.delete<ApiResponse<void>>(
        `/v1/pages/${id}`
      );
      
      if (response.data.success) {
        showSuccessToast(response.data.message || 'Page deleted successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to delete page');
      return false;
    }
  }

  // Media Methods
  async uploadMedia(file: File, userId?: number): Promise<MediaFile | null> {
    try {
      const formData = new FormData();
      formData.append('file', file);
      if (userId) {
        formData.append('userId', userId.toString());
      }

      const response = await apiClient.post<ApiResponse<MediaFile>>(
        '/v1/media/upload',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast('File uploaded successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to upload file');
      return null;
    }
  }

  async getMediaFiles(params?: {
    userId?: number;
    type?: string;
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<MediaFile> | null> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<MediaFile>>>(
        '/v1/media/files',
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch media files');
      return null;
    }
  }

  async deleteMediaFile(id: number): Promise<boolean> {
    try {
      const response = await apiClient.delete<ApiResponse<void>>(
        `/v1/media/files/${id}`
      );
      
      if (response.data.success) {
        showSuccessToast('File deleted successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to delete file');
      return false;
    }
  }

  // Content Overview
  async getContentOverview(params?: {
    blogLimit?: number;
    pageLimit?: number;
    mediaLimit?: number;
  }): Promise<ContentResponse | null> {
    try {
      const response = await apiClient.get<ApiResponse<ContentResponse>>(
        '/v1/content/overview',
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch content overview');
      return null;
    }
  }
}

export const contentApi = new ContentApiService();
export default contentApi;
