export const ChromeRuntimeMessageType = {
  API: 'api',
} as const;
export type ChromeRuntimeMessageType =
  (typeof ChromeRuntimeMessageType)[keyof typeof ChromeRuntimeMessageType];

export type ChromeRuntimeMessage<
  TType extends ChromeRuntimeMessageType = ChromeRuntimeMessageType,
  TPayload = any,
> = {
  type: TType;
  payload?: TPayload;
};

export type ChromeRuntimeMessageResponse<TData = any, TError = any> =
  | {
      success: true;
      data: TData;
    }
  | {
      success: false;
      error: TError;
    };

export type ChromeRuntimeCallback<TData = any, TError = any> = (
  response: ChromeRuntimeMessageResponse<TData, TError>,
) => void;
