import { ApiProxyKey, apiProxyRequest } from '@/api';
import { AnalyzeKind } from '@/entities/analyze';
import { MIN_TEXT_LENGTH_DEFAULT } from '@/sections';
import type { AbstractObject } from '@/types';

type Settings = { detectEnabled: boolean; minTextLength: number };

const ANALYZE_DELAY_MS = 1500;

const CANDIDATE_CLASS = '_cogniflex_candidate';
const HAS_TOOLTIP_CLASS = '_cogniflex_has-tooltip';

const STORAGE_SETTINGS_KEY = 'settings-storage';

const INTERSECTION_OBSERVER_THRESHOLD = 0.5;

const analyzedElements = new WeakMap<
  Element,
  {
    text: string;
    kind: AnalyzeKind;
    accuracy: number;
  }
>();

const pendingTimers = new WeakMap<Element, ReturnType<typeof setTimeout>>();

let intersectionObserver: IntersectionObserver | null = null;
let currentMinTextLength = MIN_TEXT_LENGTH_DEFAULT;

function injectStyles() {
  const style = document.createElement('style');
  style.textContent = `
    .${CANDIDATE_CLASS} {
      position: relative;
    }

    .${CANDIDATE_CLASS}::after {
      content: "";
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      z-index: 1;
      transition: background-color 0.2s ease-in-out;
    }

    .${CANDIDATE_CLASS}:hover::after {
      background: rgba(255, 255, 0, 0.05);
    }

    .${CANDIDATE_CLASS}:hover::before {
      top: -40px;
    }

    .${CANDIDATE_CLASS}::before {
      opacity: 0.5;
      transition: top 0.2s ease-in-out;
      content: "Проверяем..";
      position: absolute;
      top: -15px;
      left: 0;
      padding: 4px 8px;
      border-radius: 6px;
      font-size: 12px;
      font-family: sans-serif;
      z-index: 100000000;
      pointer-events: none;
      color: #fff;
      background: rgba(0, 0, 0, 0.7);
      backdrop-filter: blur(2px);
    }

    .${CANDIDATE_CLASS}.${HAS_TOOLTIP_CLASS}::before {
      opacity: 1;
      content: attr(data-content);
    }
  `;
  document.head.appendChild(style);
}
function attachTooltip(element: HTMLElement, accuracy: number, kind: AnalyzeKind) {
  removeTooltip(element);

  const tooltipContent = `${kind === AnalyzeKind.AI ? '🤖' : '✅'} ${Math.round(accuracy * 100)}%`;
  element.setAttribute('data-content', tooltipContent);
  element.classList.add(HAS_TOOLTIP_CLASS);
}

const removeTooltip = (element: Element) => element.classList.remove(HAS_TOOLTIP_CLASS);

function parseSettingsValue(storageValue: unknown) {
  if (!storageValue) {
    return {};
  }

  if (typeof storageValue === 'string') {
    try {
      return JSON.parse(storageValue).state || {};
    } catch {
      return {};
    }
  }

  return (storageValue as AbstractObject).state ?? {};
}

// Функция для получения настроек из хранилища
async function getSettings() {
  return new Promise<Settings>((resolve) => {
    chrome.storage.local.get(STORAGE_SETTINGS_KEY, (result) => {
      const settings = parseSettingsValue(result[STORAGE_SETTINGS_KEY]);
      resolve({
        detectEnabled: settings.detectEnabled ?? false,
        minTextLength: settings.minTextLength ?? MIN_TEXT_LENGTH_DEFAULT,
      });
    });
  });
}

function clearPendingTimer(element: HTMLElement) {
  const timer = pendingTimers.get(element);
  if (timer) {
    clearTimeout(timer);
    pendingTimers.delete(element);
  }
}

function markCandidate(element: HTMLElement) {
  const text = element.textContent?.trim();
  if (!text) return;

  const cached = analyzedElements.get(element);
  if (cached && cached.text === text) {
    element.classList.add(CANDIDATE_CLASS);
    attachTooltip(element, cached.accuracy, cached.kind);
    return;
  }

  if (pendingTimers.has(element)) return;

  element.classList.add(CANDIDATE_CLASS);

  const timer = setTimeout(() => {
    clearPendingTimer(element);
    apiProxyRequest(ApiProxyKey.ANALYZE_TEXT, text, (response) => {
      if (!element.classList.contains(CANDIDATE_CLASS)) return;

      if (response.success) {
        const { accuracy, kind } = response.data.data;
        analyzedElements.set(element, { accuracy, kind, text });
        attachTooltip(element, accuracy, kind);
      }
    }).catch((e) => console.error(e instanceof Error ? e.message : e));
  }, ANALYZE_DELAY_MS);

  pendingTimers.set(element, timer);
}

function unmarkCandidate(element: HTMLElement) {
  clearPendingTimer(element);
  removeTooltip(element);
  element.classList.remove(CANDIDATE_CLASS);
}

function unmarkPendingCandidates() {
  document.querySelectorAll(`.${CANDIDATE_CLASS}`).forEach((element) => {
    // все помеченные кандидаты точно pending.
    //  убираем только если ещё не проанализирован
    if (!analyzedElements.has(element)) {
      unmarkCandidate(element as HTMLElement);
    }
  });
}

function unmarkAllCandidates() {
  document.querySelectorAll(`.${CANDIDATE_CLASS}`).forEach((element) => {
    unmarkCandidate(element as HTMLElement);
  });
}

function isTextCandidate(element: Element, minTextLength: number) {
  return (
    element.children.length === 0 &&
    element.textContent &&
    element.textContent.trim().length > minTextLength
  );
}

function createIntersectionObserver() {
  if (intersectionObserver) {
    intersectionObserver.disconnect();
  }

  intersectionObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (!(entry.target instanceof HTMLElement)) return;

        if (entry.isIntersecting) {
          markCandidate(entry.target);
        } else {
          unmarkCandidate(entry.target);
        }
      });
    },
    {
      threshold: INTERSECTION_OBSERVER_THRESHOLD,
    },
  );
}

function observeCandidateElements(minTextLength: number) {
  unmarkPendingCandidates();
  intersectionObserver?.disconnect();
  createIntersectionObserver();

  const elements = document.querySelectorAll('*');
  elements.forEach((element) => {
    const cached = analyzedElements.get(element);
    if (cached) {
      element.classList.add(CANDIDATE_CLASS);
      attachTooltip(element as HTMLElement, cached.accuracy, cached.kind);
    }

    if (isTextCandidate(element, minTextLength)) {
      intersectionObserver?.observe(element);
    }
  });
}

function observeNodeCandidates(node: Element, minTextLength: number) {
  if (isTextCandidate(node, minTextLength)) {
    intersectionObserver?.observe(node);
  }

  node.querySelectorAll('*').forEach((child) => {
    if (isTextCandidate(child, minTextLength)) {
      intersectionObserver?.observe(child);
    }
  });
}

// Основная функция
async function init() {
  injectStyles();

  const settings = await getSettings();
  currentMinTextLength = settings.minTextLength;

  if (settings.detectEnabled) {
    observeCandidateElements(settings.minTextLength);
  } else {
    unmarkAllCandidates();
    intersectionObserver?.disconnect();
    intersectionObserver = null;
  }
}

// Слушать изменения в хранилище
chrome.storage.onChanged.addListener((changes, namespace) => {
  if (namespace !== 'local' || !changes[STORAGE_SETTINGS_KEY]) return;

  const prev = parseSettingsValue(changes[STORAGE_SETTINGS_KEY].oldValue);
  const next = parseSettingsValue(changes[STORAGE_SETTINGS_KEY].newValue);

  if (prev.detectEnabled !== next.detectEnabled) {
    init();
    return;
  }

  // изменился порог длины — переобходим элементы, но кэш сохраняем
  if (prev.minTextLength !== next.minTextLength) {
    currentMinTextLength = next.minTextLength ?? MIN_TEXT_LENGTH_DEFAULT;
    observeCandidateElements(currentMinTextLength);
    return;
  }
});

// Наблюдение за изменениями в DOM для динамического контента
const mutationObserver = new MutationObserver((mutations) => {
  if (!intersectionObserver) return;

  for (const mutation of mutations) {
    mutation.addedNodes.forEach((node) => {
      if (!(node instanceof Element)) return;
      observeNodeCandidates(node, currentMinTextLength);
    });
  }
});

function observeMutations() {
  mutationObserver.observe(document.body, {
    childList: true,
    subtree: true,
  });
}

init().then(observeMutations);
