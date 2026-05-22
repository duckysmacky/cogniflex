import torch
from transformers import AutoTokenizer, AutoModel
import torch.nn as nn


class RoBERTaDetector(nn.Module):
    def __init__(self, model_name='roberta-base', dropout_rate=0.3):
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


class TextDetector:
    def __init__(self, model_weights_path: str, model_name: str = 'roberta-base'):
        self.device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        self.tokenizer = AutoTokenizer.from_pretrained(model_name, cache_dir='weights/roberta_cache')
        self.model = RoBERTaDetector(model_name, dropout_rate=0.3)

        from safetensors.torch import load_file
        state_dict = load_file(model_weights_path)
        self.model.load_state_dict(state_dict, strict=False)
        self.model.to(self.device)
        self.model.eval()

    def predict_text(self, text: str):
        with torch.no_grad():
            encoded = self.tokenizer(
                text,
                truncation=True,
                padding='max_length',
                max_length=512,
                return_tensors='pt'
            )
            input_ids = encoded['input_ids'].to(self.device)
            attention_mask = encoded['attention_mask'].to(self.device)
            logits = self.model(input_ids, attention_mask)
            probs = torch.softmax(logits, dim=1)
            pred = torch.argmax(probs, dim=1).item()
            confidence = probs[0].max().item()
        return confidence, pred