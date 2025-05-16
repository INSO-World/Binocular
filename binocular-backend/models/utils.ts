import AccountUser from '../types/supportingTypes/AccountUser.ts';
import User, { UserDataType } from './models/User.ts';
import _ from 'lodash';
import { Entry } from './Model.ts';

export async function findBestUserMatch(author: AccountUser) {
  console.log(author);
  const user = await User.findAll();
  console.log(user);
  const bestMatch = user.reduce((best: any, userEntry) => {
    if (userEntry === null) {
      return;
    }
    const userName = normalizeName(userEntry.data.gitSignature);
    const authorName = normalizeName(author.name);
    let score = 0;

    if (userName.plain === authorName.plain) {
      score++;
    }

    if (userName.sorted === authorName.sorted) {
      score++;
    }

    if (!best || score > best.score) {
      return { score, user };
    } else if (score > 0) {
      return best;
    }
  }, null);
  return bestMatch ? bestMatch?.data.user : null;
}

function normalizeName(name: string) {
  const plain = _.chain(name).deburr().lowerCase().trim().value();
  const sorted = _.chain(plain).split(/\s+/).sort().join(' ').value();

  return { plain, sorted };
}

export async function findBestUserMatchLeve(author: AccountUser) {
  const users = await User.findAll();
  let bestUser = new Entry<UserDataType>({} as UserDataType, { isNew: true });
  let highestScore = 0;

  for (const user of users) {
    if (user === null) {
      return;
    }
    const distance = levenshteinDistance(author.name || author.login, user.data.gitSignature);
    const maxLength = Math.max((author.name || author.login).length, user.data.gitSignature.length);
    const score = maxLength === 0 ? 1 : 1 - distance / maxLength;

    if (score > highestScore) {
      highestScore = score;
      bestUser = user;
    }
  }

  if (bestUser.data === null) {
    return;
  } else {
    return bestUser;
  }
}

function levenshteinDistance(a: string, b: string): number {
  const dp: number[][] = [];

  for (let i = 0; i <= a.length; i++) {
    dp[i] = [];
    dp[i][0] = i; // cost of deletions
  }
  for (let j = 0; j <= b.length; j++) {
    dp[0][j] = j; // cost of insertions
  }

  // Fill in the rest of the matrix
  for (let i = 1; i <= a.length; i++) {
    for (let j = 1; j <= b.length; j++) {
      const cost = a[i - 1] === b[j - 1] ? 0 : 1;
      dp[i][j] = Math.min(
        dp[i - 1][j] + 1, // deletion
        dp[i][j - 1] + 1, // insertion
        dp[i - 1][j - 1] + cost, // substitution
      );
    }
  }

  return dp[a.length][b.length];
}
