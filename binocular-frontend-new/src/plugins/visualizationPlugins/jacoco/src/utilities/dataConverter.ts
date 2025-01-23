import { SunburstData } from '../chart/chart.tsx';

export function convertJacocoDataToSunburstData(): SunburstData {
  const createCounters = () => ({
    INSTRUCTION: { missed: Math.floor(Math.random() * 50), covered: Math.floor(Math.random() * 50) },
    LINE: { missed: Math.floor(Math.random() * 20), covered: Math.floor(Math.random() * 20) },
    COMPLEXITY: { missed: Math.floor(Math.random() * 10), covered: Math.floor(Math.random() * 10) },
    METHOD: { missed: Math.floor(Math.random() * 5), covered: Math.floor(Math.random() * 5) },
    CLASS: { missed: Math.floor(Math.random() * 5), covered: Math.floor(Math.random() * 5) },
  });

  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-expect-error
  const createObjects = (name, depth) => {
    if (depth === 0) {
      return Array.from({ length: 20 }, (_, index) => ({
        name: `${name}_object_${index + 1}`,
        counters: [createCounters()],
      }));
    }

    return Array.from({ length: 5 }, (_, index) => ({
      name: `${name}_subdir_${index + 1}`,
      children: createObjects(`${name}_subdir_${index + 1}`, depth - 1),
    }));
  };

  return {
    name: 'at',
    children: [
      {
        name: 'weber',
        children: [
          {
            name: 'martin',
            children: createObjects('e_commerce', 2),
          },
          {
            name: 'sarah',
            children: createObjects('finance', 1),
          },
        ],
      },
      {
        name: 'mueller',
        children: [
          {
            name: 'john',
            children: createObjects('john_dept', 1),
          },
          {
            name: 'alice',
            children: createObjects('alice_dept', 1),
          },
        ],
      },
    ],
  };
}
