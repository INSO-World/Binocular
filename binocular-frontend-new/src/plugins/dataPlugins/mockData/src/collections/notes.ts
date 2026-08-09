import type { DataPluginNote, DataPluginNotes } from '../../../../interfaces/dataPluginInterfaces/dataPluginNotes.ts';
import noteData from '../data/notes.json.zip';

const notes = noteData as unknown as DataPluginNote[];

export default class Notes implements DataPluginNotes {
  public async getAll(_from: string, _to: string) {
    return notes;
  }
}
