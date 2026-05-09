import torch
import torch.nn as nn
from torchvision import models
from PIL import Image
import cv2
import numpy as np
from preprocess_images import IMAGE_TRANSFORM_VAL

class MultitypePictureDetector_test:

    def __init__(self, path_general: str, path_faces: str):
        self.model_general = models.resnet18(weights=None)
        self.model_general.fc = nn.Linear(self.model_general.fc.in_features, 2)
        self.model_general.load_state_dict(torch.load(path_general, map_location='cpu'))
        self.model_general.eval()

        self.model_faces = models.resnet18(weights=None)
        self.model_faces.fc = nn.Linear(self.model_faces.fc.in_features, 2)
        self.model_faces.load_state_dict(torch.load(path_faces, map_location='cpu'))
        self.model_faces.eval()

        self.face_cascade = cv2.CascadeClassifier(
            cv2.data.haarcascades + 'haarcascade_frontalface_default.xml'
        )

    def detect_face(self, image_path) -> bool:
        image_bgr = cv2.imread(image_path)
        if image_bgr is None:
            return False
        gray = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2GRAY)
        faces = self.face_cascade.detectMultiScale(
            gray, scaleFactor=1.1, minNeighbors=5, minSize=(30, 30)
        )
        return len(faces) > 0

    def predict_picture(self, picture_path: str):
        image_bgr = cv2.imread(picture_path)
        if image_bgr is None:
            raise ValueError(f"Cannot read image: {picture_path}")

        image_rgb = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2RGB)
        gray = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2GRAY)
        faces = self.face_cascade.detectMultiScale(
            gray, scaleFactor=1.1, minNeighbors=5, minSize=(30, 30)
        )
        is_face_detected = len(faces) > 0

        image_pil = Image.fromarray(image_rgb)
        image_tensor = IMAGE_TRANSFORM_VAL(image_pil).unsqueeze(0)

        with torch.no_grad():
            model = self.model_faces if is_face_detected else self.model_general
            logits = model(image_tensor)
            probs = torch.softmax(logits, dim=1)
            pred = torch.argmax(probs, dim=1).item()
            confidence = probs[0].max().item()

        return confidence, pred
    

if __name__ == '__main__':
    detector = MultitypePictureDetector('/Users/iaroslav/Desktop/resnet_general92.pth', '/Users/iaroslav/Desktop/resnet_faces88.pth')
    prediction = detector.predict_picture('/Users/iaroslav/Desktop/2026-03-23 8.06.30 PM.jpg') #insert some path 
    print(prediction)