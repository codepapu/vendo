import { useState } from 'react';
import { Image as ImageIcon, X, CheckCircle2, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { mediaApi, type MediaFile } from '@/services/media-api';
import { cn } from '@/lib/utils';

export interface ImageUploadProps {
  /**
   * Callback when image is uploaded successfully
   */
  onUploadComplete?: (file: MediaFile) => void;
  /**
   * Callback when upload fails
   */
  onUploadError?: (error: Error) => void;
  /**
   * User ID for file ownership
   */
  userId?: number;
  /**
   * Maximum file size in bytes (default: 5MB)
   */
  maxSize?: number;
  /**
   * Accepted image formats (default: image/*)
   */
  accept?: string;
  /**
   * Custom className
   */
  className?: string;
  /**
   * Disabled state
   */
  disabled?: boolean;
  /**
   * Show preview
   */
  showPreview?: boolean;
  /**
   * Initial image URL for preview
   */
  initialImageUrl?: string;
}

/**
 * Image upload component with preview and validation
 */
export function ImageUpload({
  onUploadComplete,
  onUploadError,
  userId,
  maxSize = 5 * 1024 * 1024, // 5MB default
  accept = 'image/*',
  className,
  disabled = false,
  showPreview = true,
  initialImageUrl,
}: ImageUploadProps) {
  const [preview, setPreview] = useState<string | null>(initialImageUrl || null);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [uploadedFile, setUploadedFile] = useState<MediaFile | null>(null);
  const fileInputRef = useState<HTMLInputElement | null>(null)[0];

  const validateImage = (file: File): string | null => {
    // Check file size
    if (file.size > maxSize) {
      return `Image size exceeds ${(maxSize / 1024 / 1024).toFixed(2)}MB`;
    }

    // Check if it's an image
    if (!file.type.startsWith('image/')) {
      return 'File must be an image';
    }

    return null;
  };

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setError(null);
    const validationError = validateImage(file);
    if (validationError) {
      setError(validationError);
      if (onUploadError) {
        onUploadError(new Error(validationError));
      }
      return;
    }

    // Create preview
    const reader = new FileReader();
    reader.onloadend = () => {
      setPreview(reader.result as string);
    };
    reader.readAsDataURL(file);

    // Upload file
    setUploading(true);
    setProgress(0);

    try {
      const result = await mediaApi.uploadFile(
        file,
        userId,
        (uploadProgress) => {
          setProgress(uploadProgress);
        }
      );

      if (result) {
        setUploadedFile(result);
        setProgress(100);
        if (onUploadComplete) {
          onUploadComplete(result);
        }
      } else {
        throw new Error('Upload failed');
      }
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : 'Upload failed';
      setError(errorMessage);
      if (onUploadError) {
        onUploadError(err instanceof Error ? err : new Error(errorMessage));
      }
    } finally {
      setUploading(false);
    }
  };

  const handleRemove = () => {
    setPreview(null);
    setUploadedFile(null);
    setError(null);
    setProgress(0);
    if (fileInputRef) {
      (fileInputRef as any).value = '';
    }
  };

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  };

  return (
    <div className={cn('w-full', className)}>
      {showPreview && preview ? (
        <div className="relative group">
          <div className="relative aspect-video w-full rounded-lg overflow-hidden border">
            <img
              src={preview}
              alt="Preview"
              className="w-full h-full object-cover"
            />
            {!disabled && (
              <div className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={() => {
                    const input = document.querySelector(
                      'input[type="file"]'
                    ) as HTMLInputElement;
                    input?.click();
                  }}
                  disabled={uploading}
                >
                  Change Image
                </Button>
                <Button
                  type="button"
                  variant="destructive"
                  size="sm"
                  onClick={handleRemove}
                  disabled={uploading}
                >
                  <X className="h-4 w-4 mr-1" />
                  Remove
                </Button>
              </div>
            )}
          </div>

          {uploading && (
            <div className="mt-2">
              <Progress value={progress} className="mb-2" />
              <p className="text-xs text-muted-foreground text-center">
                Uploading... {progress}%
              </p>
            </div>
          )}

          {uploadedFile && !uploading && (
            <div className="mt-2 flex items-center justify-center gap-1 text-xs text-green-600">
              <CheckCircle2 className="h-3 w-3" />
              <span>Uploaded successfully</span>
            </div>
          )}

          {error && (
            <Alert variant="destructive" className="mt-2">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription className="text-xs">{error}</AlertDescription>
            </Alert>
          )}

          <input
            ref={(el) => {
              if (el) (fileInputRef as any) = el;
            }}
            type="file"
            accept={accept}
            onChange={handleFileSelect}
            disabled={disabled || uploading}
            className="hidden"
          />
        </div>
      ) : (
        <div
          className={cn(
            'border-2 border-dashed rounded-lg p-8 text-center transition-colors',
            'border-muted-foreground/25 hover:border-muted-foreground/50',
            disabled && 'opacity-50 cursor-not-allowed'
          )}
        >
          <ImageIcon className="mx-auto h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-sm font-medium mb-2">
            Click to upload an image
          </p>
          <p className="text-xs text-muted-foreground mb-4">
            {accept} • Max size: {formatFileSize(maxSize)}
          </p>
          <Button
            type="button"
            variant="outline"
            onClick={() => {
              const input = document.querySelector(
                'input[type="file"]'
              ) as HTMLInputElement;
              input?.click();
            }}
            disabled={disabled || uploading}
          >
            <ImageIcon className="h-4 w-4 mr-2" />
            Select Image
          </Button>

          <input
            ref={(el) => {
              if (el) (fileInputRef as any) = el;
            }}
            type="file"
            accept={accept}
            onChange={handleFileSelect}
            disabled={disabled || uploading}
            className="hidden"
          />

          {uploading && (
            <div className="mt-4">
              <Progress value={progress} className="mb-2" />
              <p className="text-xs text-muted-foreground">
                Uploading... {progress}%
              </p>
            </div>
          )}

          {error && (
            <Alert variant="destructive" className="mt-4">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription className="text-xs">{error}</AlertDescription>
            </Alert>
          )}
        </div>
      )}
    </div>
  );
}

