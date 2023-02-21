export interface UHFScannerPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
