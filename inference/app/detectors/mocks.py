import random


class MockVideoDetector:
    def predict_video(self, path):
        pred = random.choice([0, 1])
        conf = random.uniform(0.5, 1.0)
        return conf, pred


class MockTextDetector:
    def predict_text(self, text):
        pred = random.choice([0, 1])
        conf = random.uniform(0.5, 1.0)
        return conf, pred
