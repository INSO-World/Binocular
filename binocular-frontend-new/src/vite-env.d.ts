/// <reference types="vite/client" />
declare module '*.scss';
declare module '*.json.zip' {
  const value: unknown;
  export default value;
}
declare module '*.json.zip?url' {
  const src: string;
  export default src;
}
