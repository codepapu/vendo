import { apiClient, handleApiError, showSuccessToast } from '@/lib/api';
import type { ApiResponse } from '@/types';

/**
 * Media file interface
 */
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

/**
 * Paginated response for media files
 */
export interface PaginatedMediaResponse {
  content: MediaFile[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

/**
 * Upload progress callback
 */
export type UploadProgressCallback = (progress: number) => void;

/**
 * Media API service
 * Handles all media-related API calls (file upload, management, optimization)
 */
class MediaApiService {
  /**
   * Upload a file
   */
  async uploadFile(
    file: File,
    userId?: number,
    onProgress?: UploadProgressCallback
  ): Promise<MediaFile | null> {
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
          onUploadProgress: (progressEvent) => {
            if (onProgress && progressEvent.total) {
              const progress = Math.round(
                (progressEvent.loaded * 100) / progressEvent.total
              );
              onProgress(progress);
            }
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

  /**
   * Upload multiple files
   */
  async uploadFiles(
    files: File[],
    userId?: number,
    onProgress?: (fileIndex: number, progress: number) => void
  ): Promise<MediaFile[]> {
    const uploadPromises = files.map((file, index) =>
      this.uploadFile(file, userId, (progress) => {
        if (onProgress) {
          onProgress(index, progress);
        }
      })
    );

    const results = await Promise.all(uploadPromises);
    return results.filter((file): file is MediaFile => file !== null);
  }

  /**
   * Get all media files with pagination
   */
  async getMediaFiles(params?: {
    userId?: number;
    type?: string;
    page?: number;
    size?: number;
  }): Promise<PaginatedMediaResponse | null> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedMediaResponse>>(
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

  /**
   * Get media file by ID
   */
  async getMediaFileById(id: number): Promise<MediaFile | null> {
    try {
      const response = await apiClient.get<ApiResponse<MediaFile>>(
        `/v1/media/files/${id}`
      );

      if (response.data.success && response.data.data) {
        return response.data.data;
      }

      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch media file');
      return null;
    }
  }

  /**
   * Get file URL (presigned URL for S3/MinIO)
   */
  async getFileUrl(id: number): Promise<string | null> {
    try {
      const response = await apiClient.get<ApiResponse<string>>(
        `/v1/media/files/${id}/url`
      );

      if (response.data.success && response.data.data) {
        return response.data.data;
      }

      return null;
    } catch (error) {
      handleApiError(error, 'Failed to get file URL');
      return null;
    }
  }

  /**
   * Delete media file
   */
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

  /**
   * Resize image
   */
  async resizeImage(
    id: number,
    width: number,
    height: number
  ): Promise<MediaFile | null> {
    try {
      const response = await apiClient.post<ApiResponse<MediaFile>>(
        `/v1/media/files/${id}/resize`,
        null,
        {
          params: { width, height },
        }
      );

      if (response.data.success && response.data.data) {
        showSuccessToast('Image resized successfully');
        return response.data.data;
      }

      return null;
    } catch (error) {
      handleApiError(error, 'Failed to resize image');
      return null;
    }
  }

  /**
   * Optimize image
   */
  async optimizeImage(id: number): Promise<MediaFile | null> {
    try {
      const response = await apiClient.post<ApiResponse<MediaFile>>(
        `/v1/media/files/${id}/optimize`
      );

      if (response.data.success && response.data.data) {
        showSuccessToast('Image optimized successfully');
        return response.data.data;
      }

      return null;
    } catch (error) {
      handleApiError(error, 'Failed to optimize image');
      return null;
    }
  }
}

export const mediaApi = new MediaApiService();
export default mediaApi;

