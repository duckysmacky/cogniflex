from torchvision.models import resnet18
import torch
import torch.nn as nn
from utils.preprocess_images import IMAGE_TRANSFORM_VAL
from PIL import Image

class PictureDetector:
    
    def __init__(self, path):

        self.model = resnet18()
        self.model.fc = nn.Linear(self.model.fc.in_features, 2)
        self.model.load_state_dict(torch.load(path), map_location='cpu')

        self.model.eval()

    def predict_picure(self, picture_path:str):

        image = Image.open(picture_path).convert('RGB')
        image_tensor = IMAGE_TRANSFORM_VAL(image)
        image_tensor = image_tensor.unsqueeze(0)

        with torch.no_grad():
            logits = self.model(image_tensor)
            probs = torch.softmax(logits, dim = 1)
            pred = torch.argmax(probs,dim=1).item()
            confidence = probs[0].max().item()
        
        return confidence, pred
    