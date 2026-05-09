import { API_PROXY_METHODS_MAP, type ApiProxyRequestPayload } from '@/api';
import {
  ChromeRuntimeMessageType,
  type ChromeRuntimeMessage,
  type ChromeRuntimeMessageResponse,
} from '@/types';

chrome.runtime.onMessage.addListener(
  (message: ChromeRuntimeMessage, _, sendResponse: (res: ChromeRuntimeMessageResponse) => void) => {
    switch (message.type) {
      case ChromeRuntimeMessageType.API: {
        const payload = message.payload as ApiProxyRequestPayload;
        const { apiProxyKey, apiRequestParams } = payload;
        const apiMethod = API_PROXY_METHODS_MAP[apiProxyKey] as unknown as (
          ...args: typeof apiRequestParams
        ) => ReturnType<(typeof API_PROXY_METHODS_MAP)[typeof apiProxyKey]>;
        apiMethod(...apiRequestParams)
          .then((data) => sendResponse({ success: true, data }))
          .catch((error) => sendResponse({ success: false, error }));

        return true; // Для асинхронности
      }
    }
  },
);
