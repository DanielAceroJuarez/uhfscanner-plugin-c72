import { registerPlugin } from '@capacitor/core';

import type { UHFScannerPluginPlugin } from './definitions';

const UHFScannerPlugin = registerPlugin<UHFScannerPluginPlugin>('UHFScannerPlugin', {
  web: () => import('./web').then(m => new m.UHFScannerPluginWeb()),
});

export * from './definitions';
export { UHFScannerPlugin };
