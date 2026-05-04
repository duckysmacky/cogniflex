import os
import cv2
import torch
import random
import pandas as pd  
from torchvision import transforms
from PIL import Image
from preprocess_images import IMAGE_TRANSFORM_VAL
from torch.utils.data import Dataset, DataLoader  
from sklearn.model_selection import train_test_split  

def preprocess__one_video(path: str, N_frames: int = 16):
    cap = cv2.VideoCapture(path)
    if not cap.isOpened():
        raise Exception(f'cant open file {path}')
    
    total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    
    if total_frames < N_frames:
        frames_indexes = [i for i in range(total_frames)]
    else:
        step = total_frames // N_frames
        frames_indexes = [int(i * step) for i in range(N_frames)]

    frames_indexes = set(frames_indexes)
    frames = []
    idx = 0
    while True:
        ret, frame = cap.read()
        if not ret:
            break
        if idx in frames_indexes:
            frames.append(frame)
        idx += 1

    while len(frames) < N_frames:
        frames.append(frames[-1])

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

data_root = r'C:\datasets\FaceForensics++_C23'
csv_root = r'C:\datasets\FaceForensics++_C23\csv'  # ← папка с CSV

def load_from_csv(csv_path, data_root):
    df = pd.read_csv(csv_path)
    paths = []
    labels = []
    for _, row in df.iterrows():
        full_path = os.path.join(data_root, row['File Path'])
        if os.path.exists(full_path):
            paths.append(full_path)
            labels.append(1 if row['Label'] == 'FAKE' else 0)
    return paths, labels

fake_csvs = ['DeepFakeDetection.csv', 'Deepfakes.csv', 'Face2Face.csv', 
             'FaceShifter.csv', 'FaceSwap.csv', 'NeuralTextures.csv']

all_fake_paths = []
for csv_name in fake_csvs:
    csv_path = os.path.join(csv_root, csv_name)  # ← читаем из папки csv
    paths, _ = load_from_csv(csv_path, data_root)
    all_fake_paths.extend(paths)

real_paths, _ = load_from_csv(os.path.join(csv_root, 'original.csv'), data_root)

print(f"Фейков: {len(all_fake_paths)}, Реальных: {len(real_paths)}")

random.seed(42)
fake_paths = random.sample(all_fake_paths, 1000)
real_paths = random.sample(real_paths, 1000)

fake_labels = [1] * 1000
real_labels = [0] * 1000

all_paths = fake_paths + real_paths
all_labels = fake_labels + real_labels

train_paths, val_paths, train_labels, val_labels = train_test_split(
    all_paths, all_labels, test_size=0.2, random_state=42
)

train_dataset = VideoDataset(train_paths, train_labels)
val_dataset = VideoDataset(val_paths, val_labels)

train_loader = DataLoader(train_dataset, batch_size=16, shuffle=True)
val_loader = DataLoader(val_dataset, batch_size=16, shuffle=False)

print(f"Train: {len(train_paths)}, Val: {len(val_paths)}")
