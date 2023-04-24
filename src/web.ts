import { WebPlugin } from '@capacitor/core';

import type { CallbackID, UHFScanneerCallback, UHFScannerPluginPlugin } from './definitions';

export const START = "start";
export const STOP = "stop";

export class UHFScannerPluginWeb extends WebPlugin implements UHFScannerPluginPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async execute(options: { action: string , power: number}, callback: UHFScanneerCallback,): Promise<CallbackID> {
    console.log('ECHO', options);
    callback(options.action,"test");
    return options.action;
  }
}
