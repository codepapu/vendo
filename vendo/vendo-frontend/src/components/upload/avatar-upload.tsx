import { useState, useRef } from 'react';
import { User, Upload, X, CheckCircle2, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar';
import { mediaApi, type MediaFile } from '@/services/media-api';
import { cn } from '@/lib/utils';

export interface AvatarUploadProps {
  /**
   * Callback when avatar is uploaded successfully
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
   * Maximum file size in bytes (default: 2MB)
   */
  maxSize?: number;
  /**
   * Initial avatar URL
   */
  initialAvatarUrl?: string;
  /**
   * User's name for fallback avatar
   */
  userName?: string;
  /**
   * Custom className
   */
  className?: string;
  /**
   * Disabled state
   */
  disabled?: boolean;
  /**
   * Avatar size in pixels (default: 128)
   */
  size?: number;
}

/**
 * Avatar upload component with circular preview
 */
export function AvatarUpload({
  onUploadComplete,
  onUploadError,
  userId,
  maxSize = 2 * 1024 * 1024, // 2MB default
  initialAvatarUrl,
  userName,
  className,
  disabled = false,
  size = 128,
}: AvatarUploadProps) {
  const [preview, setPreview] = useState<string | null>(initialAvatarUrl || null);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [uploadedFile, setUploadedFile] = useState<MediaFile | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const validateImage = (file: File): string | null => {
    // Check file size
    if (file.size > maxSize) {
      return `Image size exceeds ${(maxSize / 1024 / 1024).toFixed(2)}MB`;
    }

    // Check if it's an image
    if (!file.type.startsWith('image/')) {
      return 'File must be an image';
    }

    // Check dimensions (should be square ideally)
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
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const getInitials = (name?: string): string => {
    if (!name) return 'U';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  };

  return (
    <div className={cn('flex flex-col items-center gap-4', className)}>
      <div className="relative">
        <Avatar
          style={{ width: size, height: size }}
          className="border-2 border-border"
        >
          <AvatarImage src={preview || undefined} alt={userName || 'Avatar'} />
          <AvatarFallback className="text-lg">
            {getInitials(userName)}
          </AvatarFallback>
        </Avatar>

        {!disabled && (
          <div className="absolute -bottom-2 -right-2">
            <Button
              type="button"
              size="icon"
              variant="secondary"
              className="rounded-full"
              onClick={() => fileInputRef.current?.click()}
              disabled={uploading}
            >
              <Upload className="h-4 w-4" />
            </Button>
          </div>
        )}

        {uploading && (
          <div className="absolute inset-0 flex items-center justify-center bg-background/80 rounded-full">
            <div className="text-center">
              <Progress value={progress} className="w-16 mb-1" />
              <span className="text-xs text-muted-foreground">{progress}%</span>
            </div>
          </div>
        )}

        {uploadedFile && !uploading && (
          <div className="absolute -top-2 -right-2 bg-green-500 rounded-full p-1">
            <CheckCircle2 className="h-4 w-4 text-white" />
          </div>
        )}
      </div>

      <div className="flex flex-col items-center gap-2">
        {preview && !disabled && (
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={handleRemove}
            disabled={uploading}
          >
            <X className="h-4 w-4 mr-1" />
            Remove Avatar
          </Button>
        )}

        {error && (
          <Alert variant="destructive" className="w-full">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription className="text-xs">{error}</AlertDescription>
          </Alert>
        )}
      </div>

      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        onChange={handleFileSelect}
        disabled={disabled || uploading}
        className="hidden"
      />
    </div>
  );
}

