const observer = new IntersectionObserver(
  (entries) => {
    entries.forEach((entry) => {
      if (!entry.isIntersecting) return;

      const img = entry.target as HTMLImageElement;
      console.log(img);
      // действия/проверки с изображениями
    });
  },
  {
    threshold: 0.1,
  },
);

function observeImage(img: HTMLImageElement) {
  if (img.dataset.observing) return;
  img.dataset.observing = 'true';
  observer.observe(img);
}

// уже существующие картинки
document.querySelectorAll('img').forEach(observeImage);

// новые элементы в DOM
const mutationObserver = new MutationObserver((mutations) => {
  for (const mutation of mutations) {
    mutation.addedNodes.forEach((node) => {
      if (!(node instanceof Element)) return;

      // если добавили img
      if (node.tagName === 'IMG') {
        observeImage(node as HTMLImageElement);
      }

      // если добавили контейнер с img внутри
      node.querySelectorAll?.('img').forEach(observeImage);
    });
  }
});

mutationObserver.observe(document.body, {
  childList: true,
  subtree: true,
});
