import flet as ft
from utils.picture_detection import PictureDetector

try:
    
    detector = PictureDetector("C:/pet_project/my_learn/best_resnet_fc-2.pth")
    model_loaded = True
    print("Модель загружена!")
except Exception as e:
    print(f"Ошибка загрузки модели: {e}")
    
  
    if "CUDA" in str(e):
        print("Пробую загрузить с map_location='cpu'...")
        try:
            
            import torch
            from torchvision.models import resnet18
            import torch.nn as nn
            
            model = resnet18()
            model.fc = nn.Linear(model.fc.in_features, 2)
            model.load_state_dict(torch.load("C:/pet_project/my_learn/best_resnet_fc-2.pth", map_location='cpu'))
            model.eval()
            
            
            class WrapperDetector:
                def __init__(self, model):
                    self.model = model
                def predict_picure(self, path):
                    from PIL import Image
                    from utils.preprocess_data import IMAGE_TRANSFORM_TRAIN
                    image = Image.open(path).convert('RGB')
                    image_tensor = IMAGE_TRANSFORM_TRAIN(image)
                    image_tensor = image_tensor.unsqueeze(0)
                    with torch.no_grad():
                        logits = self.model(image_tensor)
                        probs = torch.softmax(logits, dim=1)
                        pred = torch.argmax(probs, dim=1).item()
                        confidence = probs[0].max().item()
                    return confidence, pred
            
            detector = WrapperDetector(model)
            model_loaded = True
            print("✅ Модель загружена с map_location='cpu'!")
        except Exception as e2:
            detector = None
            model_loaded = False
            print(f"❌ Всё равно не работает: {e2}")
    else:
        detector = None
        model_loaded = False

def main(page: ft.Page):
    
    page.title = 'test model'
    page.theme_mode = 'dark'
    page.vertical_alignment = ft.MainAxisAlignment.CENTER
    
    img = ft.Image(src=None, width=224, height=224)
    result_text = ft.Text("", size=16)
    
    path_input = ft.TextField(
        label="Введите путь к картинке",
        hint_text="Например: C:/Users/User/Desktop/photo.jpg",
        width=400
    )
    
    def load_image(e):
        if path_input.value:
            file_path = path_input.value.replace('\\', '/')
            img.src = file_path
            result_text.value = ""
            page.update()
            print(f"Загружаю: {file_path}")
    
    def delete_image(e):
        img.src = None
        path_input.value = ""
        result_text.value = ""
        page.update()
        print("Фото удалено")
    
    def analyze_image(e):
        if not img.src:
            result_text.value = "Сначала загрузите изображение!"
            result_text.color = "red"
            page.update()
            return
        
        if not model_loaded or detector is None:
            result_text.value = "Модель не загружена!"
            result_text.color = "red"
            page.update()
            return
        
        try:
            confidence, prediction = detector.predict_picure(img.src)
            
            class_names = ["nature (настоящее)", "ai (сгенерированное)"]
            
            result_text.value = f"Результат: {class_names[prediction]} (уверенность: {confidence:.2%})"
            result_text.color = "green"
            page.update()
            
        except Exception as e:
            result_text.value = f"Ошибка: {str(e)}"
            result_text.color = "red"
            page.update()
    
    page.add(
        ft.Column([
            ft.Text("Загрузка картинки", size=24),
            ft.Text("Статус модели: " + ("✅" if model_loaded else "❌"), size=14),
            path_input,
            ft.Row([
                ft.ElevatedButton("📷 Загрузить", on_click=load_image),
                ft.ElevatedButton("🗑️ Удалить", on_click=delete_image),
                ft.ElevatedButton("🔍 Анализировать", on_click=analyze_image),
            ], alignment=ft.MainAxisAlignment.CENTER),
            img,
            result_text
        ], horizontal_alignment=ft.CrossAxisAlignment.CENTER)
    )
    
ft.app(target=main)
