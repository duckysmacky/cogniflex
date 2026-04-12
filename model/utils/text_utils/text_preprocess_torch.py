import torch
from torch.utils.data import DataLoader, Dataset
from transformers import AutoTokenizer

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


