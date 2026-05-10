import torch
import torch.nn as nn
import torch.optim as optim
from torchvision.models.video import r3d_18  
from video_preprocess import train_loader, val_loader

# ==================================================
# МОДЕЛЬ
# ==================================================
model = r3d_18(weights=True)
model.fc = nn.Sequential(
    nn.Dropout(0.5),
    nn.Linear(512, 2)
)

# Заморозка слоёв
for param in model.parameters():
    param.requires_grad = False

for param in model.layer4.parameters():
    param.requires_grad = True
for param in model.fc.parameters():
    param.requires_grad = True

# Устройство (GPU/CPU)
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model = model.to(device)

# Оптимизатор и функция потерь
optimizer = optim.AdamW(model.parameters(), lr=0.0001, weight_decay=0.01)
criterion = nn.CrossEntropyLoss()

print("Модель готова к обучению!")
print(f"Устройство: {device}")
print(f"Последний слой: {model.fc}")

class EarlyStopping:
    """Ранняя остановка обучения с восстановлением лучших весов"""
    def __init__(self, patience=5, min_delta=0.001, restore_best_weights=True):
        self.patience = patience
        self.min_delta = min_delta
        self.restore_best_weights = restore_best_weights
        self.best_model = None
        self.best_loss = None
        self.counter = 0
        self.early_stop = False

    def __call__(self, model, val_loss):
        if self.best_loss is None:
            self.best_loss = val_loss
            self._save_best_model(model)
        elif self.best_loss - val_loss >= self.min_delta:
            self.best_loss = val_loss
            self._save_best_model(model)
            self.counter = 0
        else:
            self.counter += 1
            if self.counter >= self.patience:
                self.early_stop = True
                if self.restore_best_weights:
                    self._restore_best_model(model)

    def _save_best_model(self, model):
        self.best_model = model.state_dict().copy()

    def _restore_best_model(self, model):
        model.load_state_dict(self.best_model)
        print(f"✅ Восстановлены лучшие веса (best loss: {self.best_loss:.4f})")



#ирли стоппинг

early_stopping = EarlyStopping(patience=5, min_delta=0.001, restore_best_weights=True)


best_accuracy = 0.0
best_epoch = 0
num_epoch = 15

# ЦИКЛ
for epoch in range(num_epoch):
    #учу
    model.train()  
    running_loss = 0.0
    
    for videos, labels in train_loader:  
        videos, labels = videos.to(device), labels.to(device)
        
        optimizer.zero_grad()
        outputs = model(videos)
        loss = criterion(outputs, labels)
        loss.backward()
        optimizer.step()
        
        running_loss += loss.item()
        
    avg_loss = running_loss / len(train_loader)

    #ВАЛИДАЦИЯ
    model.eval()
    correct = 0
    total = 0
    
    with torch.no_grad():
        for videos, labels in val_loader:
            videos, labels = videos.to(device), labels.to(device)
            outputs = model(videos)
            _, predicted = torch.max(outputs, 1)
            total += labels.size(0)
            correct += (predicted == labels).sum().item()
    
    accuracy = 100 * correct / total
    

    print(f"Epoch {epoch+1}/{num_epoch} | Loss: {avg_loss:.4f} | Val Acc: {accuracy:.2f}%")
    
    # сохранение
    if accuracy > best_accuracy:
        best_accuracy = accuracy
        best_epoch = epoch + 1
        torch.save(model.state_dict(), 'best_model.pth')
        print(f"  ✅ Лучшая модель сохранена (Acc: {accuracy:.2f}%)")
    
    #ирли стопинг
    early_stopping(model, avg_loss)
    if early_stopping.early_stop:
        print(f"⏹️ Early stopping на эпохе {epoch+1}")
        break


print("\n" + "="*50)
print(f"Обучение завершено!")
print(f"Лучшая точность: {best_accuracy:.2f}% на эпохе {best_epoch}")
print(f"Модель сохранена в best_model.pth")
print("="*50)
