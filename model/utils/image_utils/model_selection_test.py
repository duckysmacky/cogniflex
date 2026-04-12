import torch
import torch.nn as nn
from torchvision import models, transforms
from PIL import Image
import mediapipe as mp
import cv2
from model.utils.image_utils.preprocess_images import IMAGE_TRANSFORM_VAL

class MultitypePictureDetector_test:

    def __init__(self, path_general:str, path_faces:str):

        self.model_general = models.resnet18(weights=None)
        self.model_general.fc = nn.Linear(self.model_general.fc.in_features, 2)
        self.model_general.load_state_dict(torch.load(path_general, map_location='cpu'))
        self.model_general.eval()

        self.model_faces = models.resnet18(weights=None)
        self.model_faces.fc = nn.Linear(self.model_faces.fc.in_features, 2)
        self.model_faces.load_state_dict(torch.load(path_faces, map_location='cpu'))
        self.model_faces.eval()

        mp_face_detector = mp.solutions.face_detection
        self.mediapipe_detector = mp_face_detector.FaceDetection(
            model_selection = 0,
            min_detection_confidence = 0.5
        )

    def detect_face(self, image_path) -> bool: #image_bgr = np.ndarray
        image_bgr = cv2.imread(image_path)
        result = self.mediapipe_detector.process(cv2.cvtColor(image_bgr, cv2.COLOR_BGR2RGB))
        return result.detections is not None
    
    def predict_picture(self, picture_path: str):
        image_bgr = cv2.imread(picture_path)
        if image_bgr is None:
            raise ValueError(f"Cannot read image: {picture_path}")
        
        image_rgb = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2RGB)
        results = self.mediapipe_detector.process(image_rgb)
        is_face_detected = results.detections is not None

        image_pil = Image.fromarray(image_rgb)
        image_tensor = IMAGE_TRANSFORM_VAL(image_pil).unsqueeze(0)  # [1, 3, 224, 224]

        with torch.no_grad():
            model = self.model_faces if is_face_detected else self.model_general
            logits = model(image_tensor)
            probs = torch.softmax(logits, dim=1)
            pred = torch.argmax(probs, dim=1).item()
            confidence = probs[0].max().item()

        return confidence, pred
    

if __name__ == '__main__':
    detector = MultitypePictureDetector_test('...', '. . .') #model paths: general and face-detector
    prediction = detector.predict_picture('. . .') #picture path
    print(prediction)