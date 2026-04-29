const fs = require('fs');

const args = process.argv.slice(2);

const VALID_FLAGS = ['-c', '-l', '-w', '-m']

let fileName: string = "/dev/stdin"

let passedFlags = []
for (const arg of args) {
  if (VALID_FLAGS.includes(arg)) {
    passedFlags.push(arg)
  }
}

if (passedFlags.length === args.length-1) {
  fileName = args[args.length-1] as string
}

if (passedFlags.length === 0) {
  passedFlags = ['-c', '-l', '-w']
}

const content = fs.readFileSync(fileName, 'utf-8');

let counts = []
if (passedFlags.includes('-l')) {
  counts.push(getLineCount(content))
} 
if (passedFlags.includes('-w')) {
  counts.push(getWordCount(content))
} 
if (passedFlags.includes('-c')) {
  counts.push(getByteCount(content))
} 
if (passedFlags.includes('-m')) {
  counts.push(getCharCount(content))
}

if (fileName === "/dev/stdin") {
  console.log(counts.join(" "));
} else {
  console.log(counts.join(" ") + " " + fileName);
}

function getByteCount(content: string) : number {
  return Buffer.byteLength(content, 'utf-8');
}

function getLineCount(content: string) : number {
  let lineCount = 0
  for (const char of content) {
    if (char == "\n") {
        lineCount++
    }
  }
  return lineCount;
}

function getWordCount(content: string) : number {
  const delimeters = ['\n','\r','\f','\v','\t', ' ']
  let word = ""
  let wordCount = 0
  for (const char of content) {
    if (delimeters.includes(char)) {
      if (word.trim() !== '') {
        wordCount++
        word = ""
      }
    } else {
      word += char
    }
  }
  return wordCount;
}

function getCharCount(content: string) : number {
  let charCount = 0;
  for (const char of content) {
    charCount++
  }
  return charCount;
}
