import Anthropics from '@anthropic-ai/sdk';
import { CommitTagGenerator } from './commitTagGenerator';
import config from '../utils/config';
import { CommitMetadata } from '../types/supportingTypes/CommitMetadata';
import { NonRecoverableError, TransientError } from '../types/errorTypes.ts';

export class AnthropicTagGenerator extends CommitTagGenerator {
  private anthropic: Anthropics;
  private system: string;

  constructor(modelName: string) {
    super('anthropic', modelName);
    const anthropicConfig = config.get('llm.anthropic');
    const apiKey = anthropicConfig.apiKey;
    if (!apiKey) {
      throw new NonRecoverableError('Anthropics API key is not configured in .binocularrc.');
    }

    this.system = `
You are a git commit analyser.
You will get metadata from a commit, including file changes, as the input and you will have to assign the correct 
categorizations to those commits only as a JavaScript array. You can only assign the possible categorise given in the prompt.`;
    this.anthropic = new Anthropics({ apiKey });
  }

  async getTagsWithoutCheck(metadata: CommitMetadata): Promise<string[]> {
    console.log(metadata);
    const prompt = this.constructPrompt(metadata);
    console.log('Constructed Prompt:', prompt);

    try {
      const response = await this.anthropic.messages.create({
        model: this.model.name,
        max_tokens: this.model.max_tokens,
        system: this.system,
        messages: [
          { role: 'user', content: prompt },
          { role: 'assistant', content: '[Always return just a JavaScript Array]' },
        ],
      });

      console.log('Response:', JSON.stringify(response));
      try {
        const tags = JSON.parse(response.content[0]['text']);
        console.log('Tags:', tags);
        if (!Array.isArray(tags)) {
          throw new NonRecoverableError('The response did not return a valid JavaScript array of tags.');
        }
        return tags;
      } catch (parseError: any) {
        throw new NonRecoverableError(`Failed to parse the Anthropics API response: ${parseError.message}`);
      }
    } catch (error: any) {
      if (error.response) {
        const status = error.response.status;
        if (status === 429) {
          const retryAfter = error.response.headers['retry-after'] || 'unknown';
          console.warn(`Rate limited. Retry after ${retryAfter} seconds.`);
          throw new TransientError(`Rate-limited by Anthropics. Please retry after ${retryAfter} seconds.`);
        } else {
          const errMsg = `Anthropics API Error (${status}): ${error.response.data?.error?.message || 'Unknown error'}`;
          console.error(errMsg);
          throw new NonRecoverableError(errMsg);
        }
      } else {
        console.error('Error fetching tags from Anthropics:', error.message);
        if (error.code === 'ENOTFOUND' || error.code === 'ETIMEDOUT' || error.code === 'ECONNRESET') {
          throw new TransientError(`Network-related error: ${error.message}`);
        } else {
          throw new NonRecoverableError(`An unexpected error occurred: ${error.message}`);
        }
      }
    }
  }

  private constructPrompt(metadata: CommitMetadata): string {
    return `
Analyze the following commit message and changed files, then assign relevant categories from the list below. 
Only return a JavaScript array.

**Possible Categories:**
<possible-tags-data>${this.possibleTags}</possible-tags-data>

**Commit Message Metadata:**
<commit-message-metadata>\n${JSON.stringify(metadata)}\n</commit-message-metadata>
`;
  }
}
