# -*- coding: utf-8 -*-
import os
import sys

from ..prod_utils.text_preprocess_prod import prepare_text_prod, predict_text_prod, RoBERTaDetectorProd
from ..utils.text_utils.text_preprocess_torch import MODEL_ID

import torch
from torch.utils.data import DataLoader, Dataset
from transformers import AutoTokenizer, AutoModel
import torch.nn as nn
from safetensors.torch import load_file

TEXT_AI = "The rain wasn't the polite, misting kind you find in storybooks; it was a rhythmic drumming against the skylight that made the dusty rows of the bookstore feel like a sanctuary. I found myself tucked between a leaning stack of Victorian biographies and a shelf of cookbooks that smelled vaguely of dried thyme and old glue. There’s a particular sort of silence in places like this—not the absence of sound, but the presence of a thousand quiet voices waiting to be heard. I picked up a tattered paperback, its spine cracked in three places, and found a pressed wildflower between pages eighty-four and eighty-five. It had flattened into a translucent ghost of its former self, yet it held the weight of a summer I’d never known. You start to wonder who put it there, if they were happy or just bored, and if they ever came back for it. It’s funny how a single scrap of nature can make a room full of ink and paper feel suddenly, sharply alive. I didn’t buy the book, but I think I’ll remember the flower for a long time."
TEXT_HUMAN = "I’m inclined to reserve all judgments, a habit that has opened up many curious natures to me and also made me the victim of not a few veteran bores. The abnormal mind is quick to detect and attach itself to this quality when it appears in a normal person, and so it came about that in college I was unjustly accused of being a politician, because I was privy to the secret griefs of wild, unknown men. Most of the confidences were unsought—frequently I have feigned sleep, preoccupation, or a hostile levity when I realized by some unmistakable sign that an intimate revelation was quivering on the horizon; for the intimate revelations of young men, or at least the terms in which they express them, are usually plagiaristic and marred by obvious suppressions. Reserving judgments is a matter of infinite hope. I am still a little afraid of missing something if I forget that, as my father snobbishly suggested, and I snobbishly repeat, a sense of the fundamental decencies is parcelled out unequally at birth. And, after boasting this way of my tolerance, I come to the admission that it has a limit. Conduct may be founded on the hard rock or the wet marshes, but after a certain point I don’t care what it’s founded on. When I came back from the East last autumn I felt that I wanted the world to be in uniform and at a sort of moral attention forever; I wanted no more riotous excursions with privileged glimpses into the human heart. Only Gatsby, the man who gives his name to this book, was exempt from my reaction—Gatsby, who represented everything for which I have an unaffected scorn."
MODEL_PATH_TEST = '/Users/iaroslav/cogniflex/model/models_torch/model_texts.safetensors'
TOKENIZER = AutoTokenizer.from_pretrained(MODEL_ID)

try:
    model = RoBERTaDetectorProd(MODEL_ID, MODEL_PATH_TEST)
except:
    raise Exception('model loading failed')
                    
try:
    prediction1 = predict_text_prod(model, TEXT_AI)
    prediction2 = predict_text_prod(model, TEXT_HUMAN)
except:
    raise Exception('model predicting failed')

condition1 = 'ai' in prediction1 and '0.9' in prediction1
condition2 = 'human' in prediction2 and '0.9' in prediction2

if not(condition1 and condition2):
    raise Exception('wrong validation predictions')