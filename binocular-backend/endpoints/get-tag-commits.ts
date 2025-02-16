import tagsConfig from '../../config/tagsConfig.json';
import { getLLMTags, getMetadataFromCommits, saveTagsToArangoDB } from '../tags/tagsService.ts';
import CommitTags from '../types/supportingTypes/CommitTags.ts';

export default async function (req, res, context) {
  try {
    if (req.method === 'POST') {
      const commitShas: Array<string> = req.body['commitShas'];
      if (!commitShas || !Array.isArray(commitShas)) {
        return res.status(400).json({ error: 'commitShas must exist and be an array' });
      }

      const { provider: providerParam, model: modelParam } = req.query;

      if (providerParam && modelParam) {
        return res.status(400).json({ error: 'Parameters "provider" and "model" are mutually exclusive.' });
      }

      let chosenProvider = tagsConfig.defaults.provider;
      let chosenModel = tagsConfig.defaults.model;

      if (providerParam) {
        if (!tagsConfig.provider[providerParam]) {
          return res.status(400).json({ error: `Unknown provider "${providerParam}"` });
        }
        chosenProvider = providerParam;
        chosenModel = tagsConfig.provider[providerParam].defaults.model;
      } else if (modelParam) {
        let providerFound: string | null = null;
        let modelSupported = false;

        for (const [providerName, providerConfig] of Object.entries(tagsConfig.provider)) {
          if (!providerConfig || !Array.isArray(providerConfig.models)) {
            continue;
          }

          const isModelSupported = providerConfig.models.some((model) => model.name === modelParam);
          if (isModelSupported) {
            providerFound = providerName;
            modelSupported = true;
            break;
          }
        }

        if (!modelSupported || !providerFound) {
          return res.status(400).json({
            error: `Model "${modelParam}" is not supported by any known provider.`,
          });
        }

        chosenProvider = providerFound;
        chosenModel = modelParam;
      }

      const metadataFromCommit = await getMetadataFromCommits(context.db, commitShas);

      const tags: CommitTags = await getLLMTags(metadataFromCommit.data, chosenProvider, chosenModel);

      await saveTagsToArangoDB(context.db, tags);
      res.status(200).json({ message: 'Tags successfully added', tags: tags });
    } else {
      res.status(405).json({ error: 'Method Not Allowed' });
    }
  } catch (error: any) {
    console.error('Error tagging commits:', error);
    res.status(500).json({ error: 'Internal Server Error' });
  }
}
