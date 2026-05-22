import { api } from '@/api';
import { Button, Divider, TextArea } from '@/components/ui';
import type { TAnalyzeResult } from '@/entities/analyze';
import { AnalyzeResult } from '@/sections/analyze/components/analyze-result';
import type { Nullable } from '@/types';
import { formatFileSize, getFileExtension } from '@/utils';
import { IconMovie, IconPaperclip, IconPhoto, IconQuestionMark, IconX } from '@tabler/icons-react';
import { AxiosError } from 'axios';
import { useState, type ChangeEvent } from 'react';
import { useDropzone, type DropzoneOptions } from 'react-dropzone';

const getFileIcon = (file: File) => {
  if (file.type.startsWith('image/')) {
    return IconPhoto;
  }
  if (file.type.startsWith('video/')) {
    return IconMovie;
  }
  return IconQuestionMark;
};

// TODO: рефактор, вынести компоненты/хуки

export const AnalyzeSection = () => {
  const [text, setText] = useState('');
  const [file, setFile] = useState<Nullable<File>>(null);
  const [error, setError] = useState<string>('');
  const [result, setResult] = useState<Nullable<TAnalyzeResult>>(null);
  const [isAnalyzing, setIsAnalyzing] = useState(false);

  const runBtnEnabled = !isAnalyzing && (!!text.length || !!file);

  const FileIcon = file ? getFileIcon(file) : null;

  const onDrop: DropzoneOptions['onDrop'] = (acceptedFiles) => {
    const acceptedFile = acceptedFiles[0];
    setText('');
    setFile(acceptedFile);
  };

  const {
    getRootProps,
    getInputProps,
    inputRef: fileInputRef,
    isDragActive,
  } = useDropzone({ onDrop, noClick: true, noKeyboard: true });

  const handleChange = (e: ChangeEvent<HTMLTextAreaElement>) => setText(e.target.value);

  const handleUploadClick = () => {
    fileInputRef.current?.click();
  };

  const handleResetFileClick = () => {
    setFile(null);
    setResult(null);
  };

  const runAnalysis = async () => {
    setIsAnalyzing(true);
    setResult(null);
    setError('');

    try {
      const response = file
        ? await api.analyze.analyzeMedia(file)
        : await api.analyze.analyzeText(text);
      setResult(response.data);
    } catch (e: unknown) {
      if (e instanceof AxiosError) {
        setError(e.message);
      }
    } finally {
      setIsAnalyzing(false);
    }
  };

  return (
    <div>
      <input accept="image/*,video/*" ref={fileInputRef} type="file" hidden {...getInputProps()} />

      <div className="flex flex-col gap-3 px-5 py-4">
        <h2 className="text-sm font-bold">Анализ</h2>
        {file ? (
          <div className="bg-dark-blue flex items-center gap-2 rounded-lg p-2">
            <div className="bg-accent rounded-md p-1.5">{FileIcon && <FileIcon size={20} />}</div>

            <div className="flex min-w-0 flex-1 flex-col gap-0.5">
              <span className="truncate text-xs font-semibold">{file.name}</span>
              <span className="text-2xs text-gray truncate font-semibold">
                Файл {getFileExtension(file).toUpperCase()} • {formatFileSize(file.size)}
              </span>
            </div>

            <button onClick={handleResetFileClick}>
              <IconX className="text-gray" size={20} />
            </button>
          </div>
        ) : (
          <TextArea
            textAreaContainerClassName={isDragActive ? ' border-gray border-dashed bg-gray/30' : ''}
            value={text}
            onChange={handleChange}
            placeholder="Введите текст или переместите файл сюда..."
            rows={7}
            helperText={`${text.length} символов`}
            endAdornment={() => (
              <IconPaperclip onClick={handleUploadClick} className="cursor-pointer" size={16} />
            )}
            {...getRootProps()}
          />
        )}

        <Button onClick={runAnalysis} disabled={!runBtnEnabled}>
          Запустить проверку
        </Button>
      </div>
      <Divider />
      <AnalyzeResult error={error} isAnalyzing={isAnalyzing} analyzeResult={result} />
    </div>
  );
};
