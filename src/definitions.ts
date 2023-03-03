
export type CallbackID = string;
export interface UHFScannerPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  execute(options: { action: string }, callback: UHFScanneerCallback,): Promise<CallbackID>;
}

export type UHFScanneerCallback = (
  value: string | null,
  result: string | null,
  err?: any,
) => void;