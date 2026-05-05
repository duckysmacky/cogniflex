import torch
from torch.utils.data import DataLoader, Dataset
from transformers import AutoTokenizer, AutoModel
import torch.nn as nn
from safetensors.torch import load_file
import os

MODEL_ID = "FacebookAI/roberta-base"

def tokenize_texts(texts, tokenizer, max_length = 512):

    encodings = tokenizer(
        texts,
        truncation = True,
        padding = 'max_length',
        max_length = max_length,
        return_tensors = 'pt'
    )
    return encodings['input_ids'], encodings['attention_mask']


class AIDetectionDataset(Dataset):

    def __init__(self, texts, labels, tokenizer, max_length=512):
        self.encodings = tokenizer(
            texts.tolist(),
            truncation = True,
            padding = True,
            max_length = max_length,
        )
        self.labels = labels.tolist()

    def __len__(self):
        return len(self.labels)
    
    def __getitem__(self, idx):
        return {
            'input_ids': torch.tensor(self.encodings['input_ids'][idx], dtype=torch.long),
            'attention_mask': torch.tensor(self.encodings['attention_mask'][idx], dtype=torch.long),
            'labels': torch.tensor(self.labels[idx], dtype=torch.long)
        }

class RoBERTaDetector(nn.Module):
    
    def __init__(self, model_name, dropout_rate=0.3):
        super().__init__()
        self.roberta = AutoModel.from_pretrained(model_name)
        hidden_size = self.roberta.config.hidden_size

        self.classifier = nn.Sequential(
            nn.Dropout(dropout_rate),
            nn.Linear(hidden_size, 256),
            nn.ReLU(),
            nn.Dropout(dropout_rate),
            nn.Linear(256, 2)
        )

    def forward(self, input_ids, attention_mask):
        output = self.roberta(input_ids, attention_mask=attention_mask)
        cls_vector = output.last_hidden_state[:, 0, :]
        logits = self.classifier(cls_vector)
        return logits
        

#тестирование загрузки
if __name__ == '__main__':
    model = RoBERTaDetector(MODEL_ID)
