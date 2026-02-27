import os
from PIL import Image
from torch.utils.data import Dataset
from torchvision import transforms

# here will be more model_types
model_type_paths = ['imagenet_ai_0419_biggan', 'imagenet_ai_0508_adm', 'imagenet_glide']

def get_paths(data_dir:str, model_type_paths = model_type_paths):
    images_paths_train = []
    images_paths_val = []
    labels_train = []
    labels_val = []

    for model_type in model_type_paths:
        for split in ['train', 'val']:
            for label_name in ['fake', 'real']:
                
                folder = os.path.join(data_dir, model_type, split, label_name)

                for file in os.listdir(folder):
                    if not file.lower().endswith(('.jpg', '.png', '.jpeg')): continue
                    full_path = os.path.join(folder, file)

                    if split == 'train':
                        images_paths_train.append(full_path)
                        labels_train.append(0 if label_name=='real' else 1)
                    else:
                        images_paths_val.append(full_path)
                        labels_val.append(0 if label_name=='real' else 1)

    return images_paths_train, images_paths_val, labels_train, labels_val   


IMAGE_TRANSFORM_TRAIN = transforms.Compose([
    transforms.Resize((256, 256)),
    transforms.RandomHorizontalFlip(),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485,0.456,0.406], #standard values used for dataset ImageNet
        std=[0.229,0.224,0.225]
    )   
])
IMAGE_TRANSFORM_VAL = transforms.Compose([
    transforms.Resize((256, 256)),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485,0.456,0.406], 
        std=[0.229,0.224,0.225]
    )
])


class DeepfakeDataset(Dataset):
    def __init__(self, paths:list, labels:list, transform=None):
        self.paths = paths
        self.labels = labels
        self.transform = transform

    def __len__(self):
        return len(self.paths)
    
    def __getitem__(self, idx):
        image = Image.open(self.paths[idx]).convert('RGB')
        label = self.labels[idx]
        if self.transform:
            image = self.transform(image)
        return image, label