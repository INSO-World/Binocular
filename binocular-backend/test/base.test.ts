import path from 'path';
import ctx from '../utils/context.ts';
import conf from '../utils/config';

const indexerOptions = {
  backend: true,
  frontend: false,
  open: false,
  clean: true,
  its: true,
  ci: true,
  jobs: true,
  updateJobs: true,
  export: true,
  server: false,
};
const targetPath = path.resolve('.');
ctx.setOptions(indexerOptions);
ctx.setTargetPath(targetPath);
conf.loadConfig(ctx);
