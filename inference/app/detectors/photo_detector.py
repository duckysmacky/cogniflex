import cv2
import torch
import torch.nn as nn
from PIL import Image
from torchvision import models

from app.inference.image_preprocessing import IMAGE_TRANSFORM_VAL


class MultitypePictureDetector:
    def __init__(self, path_general: str, path_faces: str):
        self.model_general = models.resnet18(weights=None)
        self.model_general.fc = nn.Linear(self.model_general.fc.in_features, 2)
        self.model_general.load_state_dict(torch.load(path_general, map_location="cpu"))
        self.model_general.eval()

        self.model_faces = models.resnet18(weights=None)
        self.model_faces.fc = nn.Linear(self.model_faces.fc.in_features, 2)
        self.model_faces.load_state_dict(torch.load(path_faces, map_location="cpu"))
        self.model_faces.eval()

        self.face_cascade = cv2.CascadeClassifier(
            cv2.data.haarcascades + "haarcascade_frontalface_default.xml"
        )

    def predict_picture(self, picture_path: str):
        image_bgr = cv2.imread(picture_path)
        if image_bgr is None:
            raise ValueError(f"Cannot read image: {picture_path}")

        image_rgb = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2RGB)
        gray = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2GRAY)
        faces = self.face_cascade.detectMultiScale(
            gray,
            scaleFactor=1.1,
            minNeighbors=5,
            minSize=(30, 30),
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
