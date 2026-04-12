import torch
from torch.utils.data import DataLoader, Dataset
from transformers import AutoTokenizer, AutoModel
import torch.nn as nn

def tokenize_texts(texts, tokenizer, max_length = 512):

    encodings = tokenizer(
        texts,
        trancuation = True,
        padding = 'max_length',
        max_length = max_length,
        return_tensors = 'pt'
    )
    return encodings['input_ids'], encodings['attention_mask']


class AIDetectionDataset(Dataset):

    def __init__(self, texts, labels, tokenizer, max_length=512):
        self.texts = texts
        self.labels = labels
        self.tokenizer = tokenizer
        self.max_length = max_length

    def __len__(self):
        return len(self.labels)
    
    def __getitem__(self, idx):
        text = self.texts[idx]
        label = self.labels[idx]

        input_ids, attention_mask = tokenize_texts([text], self.tokenizer, self.max_length)
        return {
            'input_ids': input_ids.squeeze(0), #[1, 512] -> 1
            'attention_mask': attention_mask.squeeze(0),
            'labels': torch.tensor(label, dtype=torch.long)
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
        cls_vector = output.last_hidden_state[: , 0, : ]
        logits = self.classifier(cls_vector)
        return logits
