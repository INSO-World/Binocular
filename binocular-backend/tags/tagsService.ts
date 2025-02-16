import CommitTags from '../types/supportingTypes/CommitTags.ts';
import { CommitTagGenerator } from './commitTagGenerator.ts';
import { AnthropicTagGenerator } from './anthropicTagGenerator.ts';
import { CommitMetadata } from '../types/supportingTypes/CommitMetadata.ts';
import { LlamaTagGenerator } from './llamaTagGenerator.ts';

const providerRegistry: Record<string, { new (model: string): CommitTagGenerator }> = {
  anthropic: AnthropicTagGenerator,
  llama: LlamaTagGenerator,
};

export async function getLLMTags(metadata: CommitMetadata[], provider: string, model: string): Promise<CommitTags> {
  console.log(`Chosen provider: ${provider}, model: ${model}`);

  const ProviderClass = providerRegistry[provider];
  if (!ProviderClass) {
    throw new Error(`Unsupported LLM provider: ${provider}`);
  }

  const tagger = new ProviderClass(model);
  const tags: CommitTags = {};

  for (const commit of metadata) {
    tags[commit.id] = await tagger.getTags(commit);
  }

  return tags;
}

export async function getMetadataFromCommits(db, commitShas) {
  const query = `
    FOR commit IN commits
      FILTER commit.sha IN @commitShas
      RETURN {
        id: commit._id,
        sha: commit.sha,
        message: commit.message,
        branch: commit.branch,
        date: commit.date,
        webUrl: commit.webUrl,
        diff: (
        FOR c, edge
          IN INBOUND commit._id \`commits-commits\`
          RETURN {
            id: c._id,
            edgeId: edge._id,
            sha: c.sha,
            message: c.message,
            branch: c.branch,
            date: c.date,
            webUrl: c.webUrl,
            files: edge.diff.files != null ? edge.diff.files : []
          }
        )
      }
  `;
  const cursor = await db.query(query, { commitShas });
  if (cursor === null || cursor === undefined) {
    throw new Error("Can't retrieve the query. This is probably because your database is offline or some other database/query issues");
  }
  const commits = await cursor.all();
  return {
    data: commits,
  };
}

export async function saveTagsToArangoDB(db: any, tags: CommitTags): Promise<void> {
  const rawDb = db.arango;
  const commitsCollection = await rawDb.collection('commits');

  const operations = Object.entries(tags).map(([commitId, commitTag]) => {
    if (commitTag.status === 'Success') {
      commitsCollection.update(commitId, { tags: commitTag.tags }, { mergeObjects: false });
    }
  });

  await Promise.all(operations);
}
