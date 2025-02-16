import { spawn } from 'child_process';
import { CommitTagGenerator } from './commitTagGenerator.ts';
import tagsConfig from '../../config/tagsConfig.json';
import { CommitMetadata } from '../types/supportingTypes/CommitMetadata';
import { NonRecoverableError } from '../types/errorTypes.ts';

export class LlamaTagGenerator extends CommitTagGenerator {
  private javaPath: string;
  private jarPath: string;
  private systemPrompt: string;
  private javaOptions: string[];

  constructor(modelName: string) {
    super('llama', modelName);

    const llamaConfig = tagsConfig.provider.llama;
    this.javaPath = llamaConfig.javaPath || 'java';

    if (!llamaConfig.jarPath) {
      throw new NonRecoverableError('Path to llama3.jar is not configured in tagsConfig.json under provider "llama".');
    }
    this.jarPath = llamaConfig.jarPath;

    this.javaOptions = ['--enable-preview', '--add-modules', 'jdk.incubator.vector'];
    this.systemPrompt = `
You are a git commit analyser.
You will receive metadata from a commit, including file changes. 
Your task is to assign the correct categorizations to these commits as a JavaScript array.
**IMPORTANT:** Only return the JavaScript array without any additional text or explanations.
`;
  }

  protected async getTagsWithoutCheck(metadata: CommitMetadata): Promise<string[]> {
    const prompt = this.constructPrompt(metadata);

    const args = [
      ...this.javaOptions,
      '-jar',
      this.jarPath,
      '--model',
      this.model.name,
      '-n',
      `${this.model.max_tokens}`,
      '-sp',
      this.systemPrompt.trim(),
      '--prompt',
      prompt.trim(),
    ];

    return new Promise<string[]>((resolve, reject) => {
      let javaProcess;
      try {
        javaProcess = spawn(this.javaPath, args, { shell: false });
      } catch (error: any) {
        console.error('Failed to start Llama3 process:', error.message);

        let userFriendlyMessage = `Error executing Llama3: ${error.message}`;
        if (error.code === 'E2BIG') {
          userFriendlyMessage = 'The prompt or arguments sent to Llama3 are too large. Please reduce the size of the input.';
        }

        return reject(new NonRecoverableError(userFriendlyMessage));
      }

      let stdoutData = '';
      let stderrData = '';

      javaProcess.stdout.on('data', (data) => {
        stdoutData += data.toString();
      });

      javaProcess.stderr.on('data', (data) => {
        stderrData += data.toString();
      });

      javaProcess.on('close', (code) => {
        if (code !== 0) {
          console.error(`Llama3 exited with code ${code}`);
          console.error('Llama3 stderr:', stderrData);
          return reject(new NonRecoverableError(`Llama3 process exited with code ${code}`));
        }

        if (stderrData) {
          console.warn('Llama3 stderr:', stderrData);
        }

        try {
          const tags = this.extractJsonArray(stdoutData);
          if (tags) {
            resolve(tags);
          } else {
            console.error('Failed to extract JSON array from Llama3 output.');
            console.error('Llama3 stdout:', stdoutData);
            reject(new NonRecoverableError('Failed to extract JSON array from Llama3 output'));
          }
        } catch (parseError: any) {
          console.error('Error parsing Llama3 output:', parseError);
          console.error('Llama3 stdout:', stdoutData);
          reject(new NonRecoverableError('Failed to parse Llama3 output'));
        }
      });

      javaProcess.on('error', (error) => {
        console.error('Error executing Llama3:', error.message);
        reject(new NonRecoverableError(`Error executing Llama3: ${error.message}`));
      });
    });
  }

  private extractJsonArray(output: string): string[] | null {
    const start = output.indexOf('[');
    const end = output.lastIndexOf(']');
    if (start === -1 || end === -1 || end < start) {
      return null;
    }
    const jsonArrayString = output.substring(start, end + 1);
    try {
      const tags: string[] = JSON.parse(jsonArrayString);
      return tags;
    } catch (error) {
      console.error('Error parsing JSON array from output:', error);
      return null;
    }
  }

  private constructPrompt(metadata: CommitMetadata): string {
    return `
Analyze the following commit message and changed files, then assign relevant categories from the list below.
Only return a JavaScript array without additional text.

**Possible Categories:**
<possible-tags-data>${this.possibleTags.join(',')}</possible-tags-data>

**Commit Message Metadata:**
<commit-message-metadata>
${JSON.stringify(metadata, null, 2)}
</commit-message-metadata>
    `;
  }
}
