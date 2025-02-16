const fs = require('fs');

function getTimestamp() {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  const seconds = String(now.getSeconds()).padStart(2, '0');
  return `${year}/${month}/${day} ${hours}:${minutes}:${seconds}`;
}

const logsDir = './logs';
if (!fs.existsSync(logsDir)) {
  fs.mkdirSync(logsDir);
}

const output = fs.createWriteStream(`./logs/stdout.${getTimestamp().replace(/[/\s:]/g, '-')}.log`, { flags: 'a', encoding: 'utf8' });
const errorOutput = fs.createWriteStream(`./logs/stderr.${getTimestamp().replace(/[/\s:]/g, '-')}.log`, { flags: 'a', encoding: 'utf8' });

const originalLog = console.log;
const originalError = console.error;

console.log = (...args) => {
  const timestampedMessage = `[${getTimestamp()}] ${args.join(' ')}`;
  originalLog(timestampedMessage); // Log to the console
  output.write(`${timestampedMessage}\n`); // Write to the log file
};

console.error = (...args) => {
  const timestampedMessage = `[${getTimestamp()}] ${args.join(' ')}`;
  originalError(timestampedMessage); // Log to the console as error
  errorOutput.write(`${timestampedMessage}\n`); // Write to the error log file
};

module.exports = { status: 'Custom console logging initialized' };
