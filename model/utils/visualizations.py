import matplotlib.pyplot as plt
import seaborn as sns
from PIL import Image
#here are some utils for visualization

def view_picture(path:str):
    img = Image.open(path).convert('RGB')

    plt.imshow(img)
    plt.axis('off')
    plt.title('Example image')
    plt.show()


