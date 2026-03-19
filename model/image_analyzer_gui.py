import os
import flet as ft
from utils.picture_detection import PictureDetector

def main(page: ft.Page):
    page.title = 'test model'
    page.theme_mode = 'dark'
    page.vertical_alignment = ft.MainAxisAlignment.CENTER
    page.scroll = ft.ScrollMode.ADAPTIVE
    
    detector = None
    model_loaded = False
    
    img = ft.Image(src=None, width=224, height=224)
    result_text = ft.Text("", size=16)
    
    model_path_input = ft.TextField(
        label="📁 Путь к файлу модели",
        hint_text="Например: C:/models/best_resnet_fc-2.pth",
        width=400,
        value=os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 
                          "weights", "best_resnet_fc-2.pth")
    )
    
    # Поле для пути к картинке
    image_path_input = ft.TextField(
        label="🖼️ Путь к картинке",
        hint_text="Например: C:/Users/User/Desktop/photo.jpg",
        width=400
    )
    
    # Текст статуса модели
    model_status = ft.Text("⏳ Модель не загружена", color="red", size=14)
    
    def load_model(e):
        nonlocal detector, model_loaded
        
        model_path = model_path_input.value.replace('\\', '/')
        
        if not os.path.exists(model_path):
            model_status.value = f"❌ Файл не найден: {model_path}"
            model_status.color = "red"
            model_loaded = False
            detector = None
            page.update()
            return
        
        try:
            model_status.value = "🔄 Загружаю модель..."
            page.update()
            
            detector = PictureDetector(model_path)
            model_loaded = True
            model_status.value = f"✅ Модель загружена: {os.path.basename(model_path)}"
            model_status.color = "green"
            
        except Exception as e:
            model_loaded = False
            detector = None
            
            if "CUDA" in str(e):
                try:
                    import torch
                    from torchvision.models import resnet18
                    import torch.nn as nn
                    from PIL import Image
                    from utils.preprocess_data import IMAGE_TRANSFORM_TRAIN
                    
                    model = resnet18()
                    model.fc = nn.Linear(model.fc.in_features, 2)
                    model.load_state_dict(torch.load(model_path, map_location='cpu'))
                    model.eval()
                    
                    class WrapperDetector:
                        def __init__(self, model):
                            self.model = model
                        def predict_picure(self, path):
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
                    model_status.value = f"✅ Модель загружена (CPU): {os.path.basename(model_path)}"
                    model_status.color = "green"
                    
                except Exception as e2:
                    model_status.value = f"❌ Ошибка: {str(e2)[:50]}..."
                    model_status.color = "red"
            else:
                model_status.value = f"❌ Ошибка: {str(e)[:50]}..."
                model_status.color = "red"
        
        page.update()
    
    def load_image(e):
        if image_path_input.value:
            file_path = image_path_input.value.replace('\\', '/')
            if os.path.exists(file_path):
                img.src = file_path
                result_text.value = ""
                page.update()
                print(f"📸 Загружаю: {file_path}")
            else:
                result_text.value = "❌ Файл не найден!"
                result_text.color = "red"
                page.update()
    
    def delete_image(e):
        img.src = None
        image_path_input.value = ""
        result_text.value = ""
        page.update()
        print("🗑️ Фото удалено")
    
    def analyze_image(e):
        if not img.src:
            result_text.value = "⚠️ Сначала загрузите изображение!"
            result_text.color = "red"
            page.update()
            return
        
        if not model_loaded or detector is None:
            result_text.value = "⚠️ Модель не загружена!"
            result_text.color = "red"
            page.update()
            return
        
        try:
            confidence, prediction = detector.predict_picure(img.src)
            
            class_names = ["nature (настоящее)", "ai (сгенерированное)"]
            
            result_text.value = f"фото: {class_names[prediction]} (уверенность: {confidence:.2%})"
            result_text.color = "green"
            page.update()
            
        except Exception as e:
            result_text.value = f"❌ Ошибка: {str(e)}"
            result_text.color = "red"
            page.update()
    
    page.add(
        ft.Column([
            ft.Text("🎯 Анализатор изображений", size=28, weight=ft.FontWeight.BOLD),
            
            ft.Container(
                content=ft.Column([
                    ft.Text("📁 Модель", size=18, weight=ft.FontWeight.BOLD),
                    model_path_input,
                    ft.Row([
                        ft.Button("🔄 Загрузить модель", on_click=load_model), 
                    ], alignment=ft.MainAxisAlignment.CENTER),
                    model_status,
                ]),
                padding=10,
                border=ft.border.all(1, ft.Colors.GREY_800),  
                border_radius=10,
                margin=5
            ),
            
            ft.Container(
                content=ft.Column([
                    ft.Text("🖼️ Изображение", size=18, weight=ft.FontWeight.BOLD),
                    image_path_input,
                    ft.Row([
                        ft.Button("📷 Загрузить", on_click=load_image),        
                        ft.Button("🗑️ Удалить", on_click=delete_image),        
                        ft.Button("🔍 Анализировать", on_click=analyze_image), 
                    ], alignment=ft.MainAxisAlignment.CENTER),
                ]),
                padding=10,
                border=ft.border.all(1, ft.Colors.GREY_800),  
                border_radius=10,
                margin=5
            ),
            
            ft.Container(
                content=ft.Column([
                    ft.Text("📊 Результат", size=18, weight=ft.FontWeight.BOLD),
                    img,
                    result_text,
                ], horizontal_alignment=ft.CrossAxisAlignment.CENTER),
                padding=10,
                border=ft.border.all(1, ft.Colors.GREY_800),  
                border_radius=10,
                margin=5
            ),
        ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=10)
    )
    
    page.on_load = lambda _: load_model(None)

ft.app(target=main)
