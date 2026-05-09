import { API_PROXY_METHODS_MAP } from '@/api/proxy/constants';
import type { ApiProxyKey } from '@/api/proxy/types';
import {
  type ChromeRuntimeCallback,
  type ChromeRuntimeMessage,
  ChromeRuntimeMessageType,
} from '@/types';

export type ApiProxyRequestPayload<TKey = ApiProxyKey, TParams = any[]> = {
  apiProxyKey: TKey;
  apiRequestParams: TParams;
};

type ApiProxyMethodResult<TKey extends ApiProxyKey> = Awaited<
  ReturnType<(typeof API_PROXY_METHODS_MAP)[TKey]>
>;

export const apiProxyRequest = <
  TKey extends ApiProxyKey,
  TParams extends Parameters<(typeof API_PROXY_METHODS_MAP)[TKey]>,
>(
  apiProxyKey: TKey,
  ...args: [
    ...apiRequestParams: TParams,
    callback?: ChromeRuntimeCallback<ApiProxyMethodResult<TKey>>,
  ]
) => {
  const lastArg = args[args.length - 1];
  const hasCallback = typeof lastArg === 'function';
  const apiRequestParams = (hasCallback ? args.slice(0, -1) : args) as TParams;
  const callback = hasCallback ? (lastArg as ChromeRuntimeCallback) : undefined;

  return chrome.runtime
    .sendMessage<
      ChromeRuntimeMessage<
        typeof ChromeRuntimeMessageType.API,
        ApiProxyRequestPayload<TKey, TParams>
      >
    >({
      type: ChromeRuntimeMessageType.API,
      payload: {
        apiRequestParams,
        apiProxyKey,
      },
    })
    .then(callback);
};
