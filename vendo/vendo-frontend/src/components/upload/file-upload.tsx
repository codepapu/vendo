import { useState, useCallback, useRef } from 'react';
import { Upload, X, File, CheckCircle2, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { mediaApi, type MediaFile } from '@/services/media-api';
import { cn } from '@/lib/utils';

export interface FileUploadProps {
  /**
   * Accepted file types (e.g., 'image/*', '.pdf,.doc')
   */
  accept?: string;
  /**
   * Maximum file size in bytes
   */
  maxSize?: number;
  /**
   * Maximum number of files
   */
  maxFiles?: number;
  /**
   * Whether to allow multiple files
   */
  multiple?: boolean;
  /**
   * Callback when files are uploaded successfully
   */
  onUploadComplete?: (files: MediaFile[]) => void;
  /**
   * Callback when upload fails
   */
  onUploadError?: (error: Error) => void;
  /**
   * User ID for file ownership
   */
  userId?: number;
  /**
   * Custom className
   */
  className?: string;
  /**
   * Disabled state
   */
  disabled?: boolean;
}

interface UploadFile {
  file: File;
  id: string;
  progress: number;
  status: 'pending' | 'uploading' | 'success' | 'error';
  error?: string;
  result?: MediaFile;
}

/**
 * File upload component with drag-and-drop functionality
 */
export function FileUpload({
  accept,
  maxSize = 10 * 1024 * 1024, // 10MB default
  maxFiles = 1,
  multiple = false,
  onUploadComplete,
  onUploadError,
  userId,
  className,
  disabled = false,
}: FileUploadProps) {
  const [files, setFiles] = useState<UploadFile[]>([]);
  const [isDragging, setIsDragging] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const validateFile = (file: File): string | null => {
    // Check file size
    if (file.size > maxSize) {
      return `File size exceeds ${(maxSize / 1024 / 1024).toFixed(2)}MB`;
    }

    // Check file type if accept is specified
    if (accept) {
      const acceptedTypes = accept.split(',').map((type) => type.trim());
      const fileType = file.type;
      const fileName = file.name.toLowerCase();

      const isAccepted = acceptedTypes.some((type) => {
        if (type.startsWith('.')) {
          // Extension check
          return fileName.endsWith(type.toLowerCase());
        } else if (type.includes('/*')) {
          // MIME type wildcard (e.g., 'image/*')
          const baseType = type.split('/')[0];
          return fileType.startsWith(baseType + '/');
        } else {
          // Exact MIME type
          return fileType === type;
        }
      });

      if (!isAccepted) {
        return `File type not accepted. Accepted types: ${accept}`;
      }
    }

    return null;
  };

  const handleFileSelect = useCallback(
    (selectedFiles: FileList | null) => {
      if (!selectedFiles || selectedFiles.length === 0) return;

      const fileArray = Array.from(selectedFiles);
      const filesToUpload: UploadFile[] = [];

      // Limit number of files
      const filesToProcess = fileArray.slice(0, multiple ? maxFiles : 1);

      for (const file of filesToProcess) {
        const error = validateFile(file);
        if (error) {
          filesToUpload.push({
            file,
            id: Math.random().toString(36).substring(7),
            progress: 0,
            status: 'error',
            error,
          });
          continue;
        }

        filesToUpload.push({
          file,
          id: Math.random().toString(36).substring(7),
          progress: 0,
          status: 'pending',
        });
      }

      setFiles((prev) => [...prev, ...filesToUpload]);

      // Start uploading
      filesToUpload.forEach((uploadFileItem) => {
        if (uploadFileItem.status === 'pending') {
          uploadFile(uploadFileItem);
        }
      });
    },
    [maxSize, accept, multiple, maxFiles]
  );

  const uploadFile = async (uploadFile: UploadFile) => {
    setFiles((prev) =>
      prev.map((f) =>
        f.id === uploadFile.id ? { ...f, status: 'uploading' } : f
      )
    );

    try {
      const result = await mediaApi.uploadFile(
        uploadFile.file,
        userId,
        (progress) => {
          setFiles((prev) =>
            prev.map((f) =>
              f.id === uploadFile.id ? { ...f, progress } : f
            )
          );
        }
      );

      if (result) {
        setFiles((prev) =>
          prev.map((f) =>
            f.id === uploadFile.id
              ? { ...f, status: 'success', progress: 100, result }
              : f
          )
        );

        // Call onUploadComplete with all successful uploads
        const successfulFiles = files
          .filter((f) => f.status === 'success' && f.result)
          .map((f) => f.result!)
          .concat(result);

        if (onUploadComplete) {
          onUploadComplete(successfulFiles);
        }
      } else {
        throw new Error('Upload failed');
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : 'Upload failed';
      setFiles((prev) =>
        prev.map((f) =>
          f.id === uploadFile.id
            ? { ...f, status: 'error', error: errorMessage }
            : f
        )
      );

      if (onUploadError) {
        onUploadError(
          error instanceof Error ? error : new Error(errorMessage)
        );
      }
    }
  };

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!disabled) {
      setIsDragging(true);
    }
  }, [disabled]);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  }, []);

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      e.stopPropagation();
      setIsDragging(false);

      if (disabled) return;

      const droppedFiles = e.dataTransfer.files;
      handleFileSelect(droppedFiles);
    },
    [disabled, handleFileSelect]
  );

  const handleFileInputChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      handleFileSelect(e.target.files);
      // Reset input so same file can be selected again
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    },
    [handleFileSelect]
  );

  const removeFile = (id: string) => {
    setFiles((prev) => prev.filter((f) => f.id !== id));
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
      {/* Drop Zone */}
      <div
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        className={cn(
          'border-2 border-dashed rounded-lg p-8 text-center transition-colors',
          isDragging
            ? 'border-primary bg-primary/5'
            : 'border-muted-foreground/25 hover:border-muted-foreground/50',
          disabled && 'opacity-50 cursor-not-allowed'
        )}
      >
        <input
          ref={fileInputRef}
          type="file"
          accept={accept}
          multiple={multiple}
          onChange={handleFileInputChange}
          disabled={disabled}
          className="hidden"
        />

        <Upload className="mx-auto h-12 w-12 text-muted-foreground mb-4" />
        <p className="text-sm font-medium mb-2">
          Drag and drop files here, or click to select
        </p>
        <p className="text-xs text-muted-foreground mb-4">
          {accept && `Accepted: ${accept}`}
          {maxSize && ` • Max size: ${formatFileSize(maxSize)}`}
          {multiple && maxFiles > 1 && ` • Max files: ${maxFiles}`}
        </p>
        <Button
          type="button"
          variant="outline"
          onClick={() => fileInputRef.current?.click()}
          disabled={disabled}
        >
          Select Files
        </Button>
      </div>

      {/* File List */}
      {files.length > 0 && (
        <div className="mt-4 space-y-2">
          {files.map((uploadFile) => (
            <div
              key={uploadFile.id}
              className="flex items-center gap-4 p-3 border rounded-lg"
            >
              <File className="h-5 w-5 text-muted-foreground flex-shrink-0" />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium truncate">
                  {uploadFile.file.name}
                </p>
                <p className="text-xs text-muted-foreground">
                  {formatFileSize(uploadFile.file.size)}
                </p>
                {uploadFile.status === 'uploading' && (
                  <Progress value={uploadFile.progress} className="mt-2" />
                )}
                {uploadFile.status === 'error' && uploadFile.error && (
                  <Alert variant="destructive" className="mt-2">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription className="text-xs">
                      {uploadFile.error}
                    </AlertDescription>
                  </Alert>
                )}
                {uploadFile.status === 'success' && (
                  <div className="flex items-center gap-1 mt-1 text-xs text-green-600">
                    <CheckCircle2 className="h-3 w-3" />
                    <span>Uploaded successfully</span>
                  </div>
                )}
              </div>
              <Button
                type="button"
                variant="ghost"
                size="icon"
                onClick={() => removeFile(uploadFile.id)}
                className="flex-shrink-0"
              >
                <X className="h-4 w-4" />
              </Button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

