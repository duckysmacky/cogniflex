import torch
import torch.nn as nn
import torch.optim as optim
from torchvision.models.video import r3d_18  

from video_preprocess import train_loader, val_loader

model = r3d_18(weights=True) 
model.fc = nn.Linear(model.fc.in_features, 2)  

for param in model.parameters():
    param.requires_grad = False

for param in model.layer4.parameters():
    param.requires_grad = True
for param in model.fc.parameters():
    param.requires_grad = True

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model = model.to(device)

optimizer = optim.AdamW(model.parameters(), lr=0.0001)
criterion = nn.CrossEntropyLoss()

print("Модель готова к обучению!")
print(f"Последний слой: {model.fc}")

best_accuracy = 0.0
best_epoch = 0

num_epoch = 15

for epoch in range(num_epoch):
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
    
    # ВЫВОД 
    print(f"Epoch {epoch+1}/{num_epoch} | Loss: {avg_loss:.4f} | Val Acc: {accuracy:.2f}%")
    
    # лучшая модель
    if accuracy > best_accuracy:
        best_accuracy = accuracy
        best_epoch = epoch + 1
        torch.save(model.state_dict(), 'best_model.pth')
        print(f"Лучшая модель сохранена (Acc: {accuracy:.2f}%)")


print(f"конец")
print(f"Лучшая точность: {best_accuracy:.2f}% эпоха {best_epoch}")
print(f"Модель сохранена в best_model.pth")
print("="*50)
