# -*- coding: utf-8 -*-
import torch
from torch.utils.data import DataLoader, Dataset
from transformers import AutoTokenizer, AutoModel
import torch.nn as nn
from safetensors.torch import load_file
import os
from text_preprocess_torch import RoBERTaDetector, MODEL_ID

MODEL_PATH_TEST = '/Users/iaroslav/cogniflex/model/models_torch/model_texts.safetensors'
TOKENIZER = AutoTokenizer.from_pretrained(MODEL_ID)


class RoBERTaDetectorProd(RoBERTaDetector):

    def __init__(self, model_name, weights_path, dropout_rate=0.3):

        print(f'Startedt loading model {model_name}✅')
        super().__init__(model_name=model_name, dropout_rate=dropout_rate)
        self.device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        

        if not os.path.exists(weights_path):
            raise FileNotFoundError(f"Weights file not found: {weights_path}")

        try:
            print('started loading')
            state_dict = load_file(weights_path)
            self.load_state_dict(state_dict)
            print(f" Weights are successfully loaded from {weights_path}✅")
        except Exception as e:
            print(f"Loading weights failed: {e}❌")
            raise
        
        self.to(self.device)
        self.eval()
        print("Model is ready✅")
    
    
def prepare_text_prod(text: str):

    tokens = TOKENIZER(
        text,
        truncation=False,
        padding=False
    )

    input_ids = tokens["input_ids"]

    chunks = []

    for i in range(0, len(input_ids), 512):

        chunk_ids = input_ids[i:i+512]

        enc = TOKENIZER.pad(
            {
                "input_ids": [chunk_ids],
                "attention_mask": [[1]*len(chunk_ids)]
            },
            padding="max_length",
            max_length=512,
            return_tensors="pt"
        )

        chunks.append(enc)

    return chunks

def predict_text_prod(model, long_text:str):
    
    chunks = prepare_text_prod(long_text)
    
    probs_all = []
    with torch.no_grad():
        for chunk in chunks:

            chunk = {k: v.to(model.device) for k, v in chunk.items()}
            outputs = model(**chunk)
            logits = outputs
            prob = torch.softmax(logits, dim=1)

            probs_all.append(prob)


    probs_all = torch.cat(probs_all, dim=0)
    avg_probs = probs_all.mean(dim=0)

    prob_ai = avg_probs[1].item()
    prob_human = avg_probs[0].item()

    predicted_class = ('ai' if prob_ai > 0.9 else 'human')

    if predicted_class == 'ai':
        return f'{predicted_class} with probability {prob_ai}'
    else:
        return f'{predicted_class} with probability {prob_human}'



if __name__ == "__main__":

    long_text_to_predict_ai = "The rain wasn't the polite, misting kind you find in storybooks; it was a rhythmic drumming against the skylight that made the dusty rows of the bookstore feel like a sanctuary. I found myself tucked between a leaning stack of Victorian biographies and a shelf of cookbooks that smelled vaguely of dried thyme and old glue. There’s a particular sort of silence in places like this—not the absence of sound, but the presence of a thousand quiet voices waiting to be heard. I picked up a tattered paperback, its spine cracked in three places, and found a pressed wildflower between pages eighty-four and eighty-five. It had flattened into a translucent ghost of its former self, yet it held the weight of a summer I’d never known. You start to wonder who put it there, if they were happy or just bored, and if they ever came back for it. It’s funny how a single scrap of nature can make a room full of ink and paper feel suddenly, sharply alive. I didn’t buy the book, but I think I’ll remember the flower for a long time."
    long_text_to_predict_human = "I’m inclined to reserve all judgments, a habit that has opened up many curious natures to me and also made me the victim of not a few veteran bores. The abnormal mind is quick to detect and attach itself to this quality when it appears in a normal person, and so it came about that in college I was unjustly accused of being a politician, because I was privy to the secret griefs of wild, unknown men. Most of the confidences were unsought—frequently I have feigned sleep, preoccupation, or a hostile levity when I realized by some unmistakable sign that an intimate revelation was quivering on the horizon; for the intimate revelations of young men, or at least the terms in which they express them, are usually plagiaristic and marred by obvious suppressions. Reserving judgments is a matter of infinite hope. I am still a little afraid of missing something if I forget that, as my father snobbishly suggested, and I snobbishly repeat, a sense of the fundamental decencies is parcelled out unequally at birth. And, after boasting this way of my tolerance, I come to the admission that it has a limit. Conduct may be founded on the hard rock or the wet marshes, but after a certain point I don’t care what it’s founded on. When I came back from the East last autumn I felt that I wanted the world to be in uniform and at a sort of moral attention forever; I wanted no more riotous excursions with privileged glimpses into the human heart. Only Gatsby, the man who gives his name to this book, was exempt from my reaction—Gatsby, who represented everything for which I have an unaffected scorn."

    model = RoBERTaDetectorProd(MODEL_ID, MODEL_PATH_TEST)
    prediction1 = predict_text_prod(model, long_text_to_predict_ai)
    prediction2 = predict_text_prod(model, long_text_to_predict_human)
    print(prediction1)
    print(prediction2)