import os
import sys
from pathlib import Path

ROOT_DIR = Path(__file__).resolve().parents[2]
if str(ROOT_DIR) not in sys.path:
    sys.path.insert(0, str(ROOT_DIR))

import torch
import torch.nn as nn
from torchvision import models, transforms
from PIL import Image
import mediapipe as mp
import cv2
from model.utils.image_utils.preprocess_images import IMAGE_TRANSFORM_VAL
import io
import numpy as np
from safetensors.torch import load_file

class MultitypePictureDetector:

    def __init__(self, path_general:str, path_faces:str):

        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        #model general loading
        self.model_general = models.resnet18(weights=None)
        self.model_general.fc = nn.Linear(self.model_general.fc.in_features, 2)        
        self.model_general.load_state_dict(load_file(path_general, map_location=self.device))
        self.model_general.to(self.device)
        self.model_general.eval()
        #model faces loading
        self.model_faces = models.resnet18(weights=None)
        self.model_faces.fc = nn.Linear(self.model_faces.fc.in_features, 2)
        self.model_faces.load_state_dict(load_file(path_faces, map_location=self.device))
        self.model_faces.to(self.device)
        self.model_faces.eval()

        mp_face_detector = mp.solutions.face_detection
        self.mediapipe_detector = mp_face_detector.FaceDetection(
            model_selection = 0,
            min_detection_confidence = 0.5
        )
    
    def predict_picture(self, picture_bytes: bytes):
        #data type uint-8
        nparr = np.frombuffer(picture_bytes, np.uint8)
        
        image_bgr = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        if image_bgr is None:
            raise ValueError(f"Cannot read image")
        
        image_rgb = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2RGB)
        results = self.mediapipe_detector.process(image_rgb)
        is_face_detected = results.detections is not None

        image_pil = Image.fromarray(image_rgb)
        image_tensor = IMAGE_TRANSFORM_VAL(image_pil).unsqueeze(0).to(self.device)  # [1, 3, 224, 224]

        with torch.no_grad():
            model = self.model_faces if is_face_detected else self.model_general
            logits = model(image_tensor)
            probs = torch.softmax(logits, dim=1)
            pred = torch.argmax(probs, dim=1).item()
            confidence = probs[0].max().item()

        return confidence, pred
    
    def __del__(self):
        self.mediapipe_detector.close()