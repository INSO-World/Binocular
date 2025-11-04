export interface infoTooltipContent {
  headline: string;
  text: string;
}

export function showInfoTooltip(x: number, y: number, content: infoTooltipContent) {
  (document.getElementById('infoTooltip') as HTMLDialogElement).showModal();
  if (y >= window.innerHeight / 2) {
    (document.getElementById('infoTooltipPositionController') as HTMLDivElement).style.top = `auto`;
    (document.getElementById('infoTooltipPositionController') as HTMLDivElement).style.bottom = `${window.innerHeight - y - 10}px`;
  } else {
    (document.getElementById('infoTooltipPositionController') as HTMLDivElement).style.top = `${y - 10}px`;
    (document.getElementById('infoTooltipPositionController') as HTMLDivElement).style.bottom = `auto`;
  }
  if (x >= window.innerWidth / 2) {
    (document.getElementById('infoTooltipPositionController') as HTMLDivElement).style.left = `auto`;
    (document.getElementById('infoTooltipPositionController') as HTMLDivElement).style.right = `${window.innerWidth - x - 10}px`;
  } else {
    (document.getElementById('infoTooltipPositionController') as HTMLDivElement).style.left = `${x - 10}px`;
    (document.getElementById('infoTooltipPositionController') as HTMLDivElement).style.right = `auto`;
  }

  (document.getElementById('infoTooltipContent') as HTMLDivElement).innerHTML = '';
  const headline = document.createElement('h1');
  headline.innerText = content.headline;
  const text = document.createElement('p');
  text.innerText = content.text;
  (document.getElementById('infoTooltipContent') as HTMLDivElement).appendChild(headline);
  (document.getElementById('infoTooltipContent') as HTMLDivElement).appendChild(text);
}
