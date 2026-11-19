import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Upload, Image as ImageIcon } from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
Dialog,
DialogContent,
DialogHeader,
DialogTitle,
} from '@/components/ui/dialog';
import { contentApi } from '@/services/content-api';
import { useAuthStore } from '@/stores/auth-store';

interface MediaLibraryProps {
  onSelect: (url: string) => void;
  multiple?: boolean;
}

/**
 * Media library component for selecting and uploading images
 */
export function MediaLibrary({ onSelect, multiple = false }: MediaLibraryProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [selectedFiles, setSelectedFiles] = useState<string[]>([]);
  const { user } = useAuthStore();
  const queryClient = useQueryClient();

  // Fetch media files
  const { data: mediaData, isLoading } = useQuery({
    queryKey: ['media-files'],
    queryFn: () => contentApi.getMediaFiles({
      type: 'image',
      page: 0,
      size: 50,
    }),
    enabled: isOpen,
  });

  // Upload mutation
  const uploadMutation = useMutation({
    mutationFn: (file: File) => contentApi.uploadMedia(file, user?.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['media-files'] });
    },
  });

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      uploadMutation.mutate(file);
    }
  };

  const handleSelect = (url: string) => {
    if (multiple) {
      setSelectedFiles(prev => 
        prev.includes(url) 
          ? prev.filter(u => u !== url)
          : [...prev, url]
      );
    } else {
      onSelect(url);
      setIsOpen(false);
    }
  };

  const handleConfirm = () => {
    if (selectedFiles.length > 0) {
      onSelect(selectedFiles[0]); // For now, just select first
      setIsOpen(false);
      setSelectedFiles([]);
    }
  };

  const mediaFiles = mediaData?.content || [];

  return (
    <>
      <Button
        type="button"
        variant="outline"
        onClick={() => setIsOpen(true)}
      >
        <ImageIcon className="mr-2 h-4 w-4" />
        Media Library
      </Button>

      <Dialog open={isOpen} onOpenChange={setIsOpen}>
        <DialogContent className="max-w-4xl max-h-[80vh] flex flex-col p-0">
          <DialogHeader className="p-6 border-b">
            <DialogTitle>Media Library</DialogTitle>
          </DialogHeader>

            {/* Upload Section */}
            <div className="p-4 border-b bg-muted/30">
              <label className="flex items-center gap-2 cursor-pointer">
                <Upload className="h-4 w-4" />
                <span className="text-sm">Upload new image</span>
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleFileUpload}
                  className="hidden"
                />
              </label>
              {uploadMutation.isPending && (
                <p className="text-sm text-muted-foreground mt-2">Uploading...</p>
              )}
            </div>

            {/* Media Grid */}
            <div className="flex-1 overflow-y-auto p-6">
              {isLoading ? (
                <div className="text-center py-12">Loading...</div>
              ) : mediaFiles.length === 0 ? (
                <div className="text-center py-12 text-muted-foreground">
                  No media files found. Upload an image to get started.
                </div>
              ) : (
                <div className="grid grid-cols-4 gap-4">
                  {mediaFiles.map((file) => (
                    <div
                      key={file.id}
                      className={`relative aspect-square border-2 rounded-lg overflow-hidden cursor-pointer transition-all ${
                        selectedFiles.includes(file.url) || (!multiple && false)
                          ? 'border-primary ring-2 ring-primary'
                          : 'border-border hover:border-primary'
                      }`}
                      onClick={() => handleSelect(file.url)}
                    >
                      <img
                        src={file.url}
                        alt={file.alt || file.originalName}
                        className="w-full h-full object-cover"
                      />
                      <div className="absolute inset-0 bg-black/0 hover:bg-black/20 transition-colors" />
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Footer */}
            {multiple && selectedFiles.length > 0 && (
              <div className="p-4 border-t flex justify-between items-center">
                <span className="text-sm text-muted-foreground">
                  {selectedFiles.length} file(s) selected
                </span>
                <Button onClick={handleConfirm}>
                  Select {selectedFiles.length} file(s)
                </Button>
              </div>
            )}
        </DialogContent>
      </Dialog>
    </>
  );
}
