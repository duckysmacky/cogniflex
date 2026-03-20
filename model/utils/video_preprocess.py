import os
import cv2
import torch
from torchvision import transforms
from PIL import Image
from preprocess_images import IMAGE_TRANSFORM_VAL
from torch.utils.data import Dataset


def preprocess__one_video(path: str, N_frames: int = 16) -> list:

    cap = cv2.VideoCapture(path)

    if not cap.isOpened():
        raise Exception(f'cant open file {path}')
    
    total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    
    if total_frames < N_frames:
        frames_indexes = [i for i in range(total_frames)]

    else:
        step = total_frames//N_frames
        frames_indexes = [int(i*step) for i in range(N_frames)]

    frames_indexes = set(frames_indexes)
    frames = []
    idx = 0
    while True:
        ret, frame = cap.read()

        if not ret: break
        if idx in frames_indexes:
            frames.append(frame)
        
        idx += 1

    while len(frames) < N_frames: frames.append(frames[-1])

    cap.release()
    cv2.destroyAllWindows()

    processed_frames = []
    transformer_video = IMAGE_TRANSFORM_VAL
    for frame in frames:
        frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        image = Image.fromarray(frame)
        tensor = transformer_video(image)
        processed_frames.append(tensor)

    video_tensor = torch.stack(processed_frames) 
    video_tensor = video_tensor.permute(1, 0, 2, 3)
    return video_tensor


def get_paths_forensics(data_dir:str) -> list[str]:
    fake_folder = ['DeepFakeDetection', 'Deepfakes', 'Face2Face', 
                    'FaceShifter', 'FaceSwap', 'NeuralTextures']
    video_paths = []
    
    for folder in fake_folder:
        
        folder_path = os.path.join(data_dir, folder)
        if not os.path.exists(folder_path):
            print('файл не найден') 
            continue
        
        for file in os.listdir(folder_path):
            if file.lower().endswith(('.mp4', '.avi', '.mov')):
                video_paths.append(os.path.join(folder_path, file))
            
            
    return video_paths    
        
    
def get_paths_celebs(data_dir:str) -> list[str]:
    video_paths = []
    folder_path = os.path.join(data_dir, 'original')
    if not os.path.exists(folder_path):
        print('файл не найден')
        return video_paths
    
    for file in os.listdir(folder_path):
        if file.lower().endswith(('.mp4', '.avi', '.mov')):
            video_paths.append(os.path.join(folder_path, file))
    
    return video_paths

class VideoDataset(Dataset):

    def __init__(self, video_paths, labels):
        self.video_paths = video_paths
        self.labels = labels

    def __len__(self):
        return len(self.video_paths)
    
    def __getitem__(self, index):
        video = preprocess__one_video(self.video_paths[index])
        label = self.labels[index]

        return video, label
