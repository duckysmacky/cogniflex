import { ApiProxyKey, apiProxyRequest } from '@/api';
import { AnalyzeKind } from '@/entities/analyze';
import { MIN_TEXT_LENGTH_DEFAULT } from '@/sections';
import type { AbstractObject, Nullable } from '@/types';
import { autoUpdate, computePosition, flip, offset, shift } from '@floating-ui/dom';

type Settings = { detectEnabled: boolean; minTextLength: number };

const SKIP_TAGS = ['script', 'style', 'noscript', 'iframe', 'svg', 'canvas'];

const ANALYZE_DELAY_MS = 1000;

const CANDIDATE_ATTR = 'data-cogniflex-candidate';
const TOOLTIP_VISIBLE_ATTR = 'data-cogniflex-tooltip-visible';
const HIGHLIGHT_ATTR = 'data-cogniflex-shadow';
const TOOLTIP_EL_ID = '_cogniflex_tooltip';

const STORAGE_SETTINGS_KEY = 'settings-storage';
const INTERSECTION_OBSERVER_THRESHOLD = 0.5;

const HIGHLIGHT_AI = '0 0 0 2px rgba(239, 68, 68, 0.6)';
const HIGHLIGHT_HUMAN = '0 0 0 2px rgba(38, 197, 94, 0.6)';

const analyzedElements = new WeakMap<
  HTMLElement,
  { text: string; kind: AnalyzeKind; accuracy: number }
>();

const pendingTimers = new WeakMap<Element, ReturnType<typeof setTimeout>>();

let intersectionObserver: IntersectionObserver | null = null;
let currentMinTextLength = MIN_TEXT_LENGTH_DEFAULT;
let tooltipEl: HTMLElement | null = null;
let stylesInjected = false;

let currentHoveredElement: HTMLElement | null = null;

let tooltipUpdateCleanup: Nullable<() => void> = null;

function createTooltip() {
  const host = document.createElement('div');
  host.style.cssText =
    'all:initial;position:fixed;top:0;left:0;z-index:2147483647;pointer-events:none;';

  const shadow = host.attachShadow({ mode: 'closed' });

  const style = document.createElement('style');
  style.textContent = `
    #${TOOLTIP_EL_ID} {
      position: absolute;
      padding: 4px 8px;
      border-radius: 6px;
      font-size: 12px;
      font-family: sans-serif;
      color: #fff;
      background: rgba(0, 0, 0, 0.75);
      backdrop-filter: blur(3px);
      pointer-events: none;
      white-space: nowrap;
      opacity: 0;
      transition: opacity 0.1s ease;
    }
    #${TOOLTIP_EL_ID}[${TOOLTIP_VISIBLE_ATTR}] {
      opacity: 1;
    }
    [${CANDIDATE_ATTR}]
  `;

  tooltipEl = document.createElement('div');
  tooltipEl.id = TOOLTIP_EL_ID;

  shadow.appendChild(style);
  shadow.appendChild(tooltipEl);
  document.documentElement.appendChild(host);
}

async function updateTooltipPosition() {
  if (!currentHoveredElement || !tooltipEl) {
    return;
  }

  const { x, y } = await computePosition(currentHoveredElement, tooltipEl, {
    placement: 'top-end',
    middleware: [offset(8), flip(), shift({ padding: 5 })],
  });
  Object.assign(tooltipEl.style, {
    left: `${x}px`,
    top: `${y}px`,
  });
}

function showTooltip(element: HTMLElement) {
  if (!tooltipEl) return;
  const data = analyzedElements.get(element);
  if (!data) return;

  tooltipUpdateCleanup?.();

  currentHoveredElement = element;

  const icon = data.kind === AnalyzeKind.AI ? '🤖' : '✅';
  tooltipEl.textContent = `${icon} ${Math.round(data.accuracy * 100)}%`;

  tooltipEl.setAttribute(TOOLTIP_VISIBLE_ATTR, '');

  tooltipUpdateCleanup = autoUpdate(element, tooltipEl, updateTooltipPosition);
}

function hideTooltip() {
  currentHoveredElement = null;
  tooltipEl?.removeAttribute(TOOLTIP_VISIBLE_ATTR);
  tooltipUpdateCleanup?.();
  tooltipUpdateCleanup = null;
}

function highlightElement(element: HTMLElement, kind: AnalyzeKind) {
  if (!element.hasAttribute(HIGHLIGHT_ATTR)) {
    element.setAttribute(HIGHLIGHT_ATTR, element.style.boxShadow);
  }

  const shadow = kind === AnalyzeKind.AI ? HIGHLIGHT_AI : HIGHLIGHT_HUMAN;
  const existingShadow = element.style.boxShadow;
  element.style.boxShadow = existingShadow ? `${existingShadow}, ${shadow}` : shadow;
}

function removeHighlight(element: HTMLElement) {
  if (!element.hasAttribute(HIGHLIGHT_ATTR)) {
    return;
  }

  element.style.boxShadow = element.getAttribute(HIGHLIGHT_ATTR) ?? '';
  element.removeAttribute(HIGHLIGHT_ATTR);
}

function attachHoverListeners() {
  document.addEventListener(
    'mouseover',
    (e) => {
      const target = (e.target as HTMLElement).closest(`[${CANDIDATE_ATTR}]`);
      if (target && analyzedElements.has(target as HTMLElement)) {
        showTooltip(target as HTMLElement);
      } else {
        hideTooltip();
      }
    },
    { passive: true },
  );

  document.addEventListener(
    'mouseout',
    (e) => {
      const target = (e.target as HTMLElement).closest(`[${CANDIDATE_ATTR}]`);
      if (target && analyzedElements.has(target as HTMLElement)) {
        hideTooltip();
      }
    },
    { passive: true },
  );
}

function injectStyles() {
  if (!stylesInjected) {
    createTooltip();
    attachHoverListeners();
    stylesInjected = true;
  }
}

function parseSettingsValue(storageValue: unknown) {
  if (!storageValue) return {};
  if (typeof storageValue === 'string') {
    try {
      return JSON.parse(storageValue).state || {};
    } catch {
      return {};
    }
  }
  return (storageValue as AbstractObject).state ?? {};
}

async function getSettings(): Promise<Settings> {
  return new Promise((resolve) => {
    chrome.storage.local.get(STORAGE_SETTINGS_KEY, (result) => {
      const s = parseSettingsValue(result[STORAGE_SETTINGS_KEY]);
      resolve({
        detectEnabled: s.detectEnabled ?? false,
        minTextLength: s.minTextLength ?? MIN_TEXT_LENGTH_DEFAULT,
      });
    });
  });
}

function isTextCandidate(element: Element, minTextLength: number) {
  const tag = element.tagName.toLowerCase();
  return (
    !SKIP_TAGS.includes(tag) &&
    element.children.length === 0 &&
    element.textContent &&
    element.textContent.trim().length > minTextLength
  );
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
    highlightElement(element, cached.kind);
    return;
  }

  element.setAttribute(CANDIDATE_ATTR, '');

  if (pendingTimers.has(element)) return;

  const timer = setTimeout(() => {
    clearPendingTimer(element);
    apiProxyRequest(ApiProxyKey.ANALYZE_TEXT, text, (response) => {
      if (response.success) {
        const { accuracy, kind } = response.data.data;
        analyzedElements.set(element, { accuracy, kind, text });
        highlightElement(element, kind);
      }
    }).catch((e) => {
      console.error(e instanceof Error ? e.message : e);
    });
  }, ANALYZE_DELAY_MS);

  pendingTimers.set(element, timer);
}

function unmarkCandidate(element: HTMLElement) {
  clearPendingTimer(element);
  removeHighlight(element);
}

function unmarkAllCandidates() {
  document
    .querySelectorAll(`[${CANDIDATE_ATTR}]`)
    .forEach((el) => unmarkCandidate(el as HTMLElement));
}

function createIntersectionObserver() {
  intersectionObserver?.disconnect();

  intersectionObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (!(entry.target instanceof HTMLElement)) return;

        if (entry.isIntersecting) {
          markCandidate(entry.target);
        } else {
          clearPendingTimer(entry.target);
        }
      });
    },
    { threshold: INTERSECTION_OBSERVER_THRESHOLD },
  );
}

function observeCandidateElements(minTextLength: number) {
  intersectionObserver?.disconnect();
  createIntersectionObserver();

  document.querySelectorAll('*').forEach((element) => {
    // восстанавливаем подсветку для уже проанализированных
    const cached = analyzedElements.get(element as HTMLElement);
    if (cached) {
      highlightElement(element as HTMLElement, cached.kind);
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

  if (prev.minTextLength !== next.minTextLength) {
    currentMinTextLength = next.minTextLength ?? MIN_TEXT_LENGTH_DEFAULT;
    observeCandidateElements(currentMinTextLength);
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
