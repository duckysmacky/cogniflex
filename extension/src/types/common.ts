export type Nullable<T> = T | null;

export type AbstractObject = Record<string, unknown>;

export type AbstractFunction = (...args: any[]) => any;
