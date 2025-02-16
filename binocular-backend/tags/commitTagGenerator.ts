import tagsConfig from '../../config/tagsConfig.json';
import { CommitMetadata } from '../types/supportingTypes/CommitMetadata.ts';
import CommitTag from '../types/supportingTypes/CommitTag.ts';
import { NonRecoverableError, TransientError } from '../types/errorTypes.ts';
import { Result, Ok, Err } from '../types/functionalTypes.ts';

export abstract class CommitTagGenerator {
  protected possibleTags: string[];
  protected model: { name: string; max_tokens: number };
  protected constructor(provider: string, modelName: string) {
    this.possibleTags = tagsConfig.possibleTags || [];
    const providerConfig = tagsConfig.provider[provider];
    if (!providerConfig || !Array.isArray(providerConfig.models)) {
      throw new Error(`Provider "${provider}" is not configured properly.`);
    }
    const selectedModel = providerConfig.models.find((m) => m.name === modelName);
    if (!selectedModel) {
      throw new Error(`Model "${modelName}" is not supported by provider "${provider}".`);
    }
    this.model = selectedModel;
  }
  public async getTags(metadata: CommitMetadata): Promise<CommitTag> {
    const maxRetries = 3;

    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      const result = await this.safeGetTags(metadata);

      if (result.kind === 'ok') {
        const tags = result.value;
        if (this.validateTags(tags)) {
          return {
            tags,
            status: 'Success',
          };
        } else {
          console.log(`Attempt ${attempt} failed: generated tags did not match allowed tags.`);
        }
      } else {
        const error = result.error;
        console.error(`Attempt ${attempt} encountered an error: ${error.message}`);

        if (!this.isRecoverableError(error)) {
          return {
            status: `Error: Non-recoverable error encountered after ${attempt} attempt${attempt === 1 ? '' : 's'}: ${error.message}`,
          };
        }
      }
    }

    return {
      status: `Error: After ${maxRetries} attempts, could not produce valid tags.`,
    };
  }
  protected async safeGetTags(metadata: CommitMetadata): Promise<Result<string[]>> {
    try {
      const tags = await this.getTagsWithoutCheck(metadata);
      return Ok(tags);
    } catch (error) {
      if (error instanceof TransientError) {
        return Err(error);
      }
      if (error instanceof NonRecoverableError) {
        return Err(error);
      }
      return Err(new NonRecoverableError(`Unknown error: ${error instanceof Error ? error.message : String(error)}`));
    }
  }
  private validateTags(tags: string[]): boolean {
    return tags.every((tag) => this.possibleTags.includes(tag));
  }
  private isRecoverableError(error: Error): boolean {
    return error.message.includes('Transient') || error.message.includes('Network');
  }
  protected abstract getTagsWithoutCheck(metadata: CommitMetadata): Promise<string[]>;
}
