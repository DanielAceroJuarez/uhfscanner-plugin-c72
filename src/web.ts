import { WebPlugin } from '@capacitor/core';

import type { UHFScannerPluginPlugin } from './definitions';

export class UHFScannerPluginWeb extends WebPlugin implements UHFScannerPluginPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
