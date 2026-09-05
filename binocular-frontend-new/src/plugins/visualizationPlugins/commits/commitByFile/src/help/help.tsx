function Help() {
  return (
    <div className="z-[999]">
      <h2>Commit By File Help</h2>
      <h3>Data</h3>
      <p>
        The commit by file visualization provides an overview of the files changed in a specific commit. It allows users to easily see where
        the main changes are made and how they are distributed across different files.
      </p>
      <h3>Hover</h3>
      <p>
        Hovering over a file shows its name plus how many lines it added and deleted in this commit. Hovering over a folder shows the same
        totals for everything underneath it, plus a one-level breakdown of the subfolders and files directly inside it.
      </p>
      <h3>Parameters</h3>
      <div>
        <ul className={'list-disc ml-5'}>
          <li>
            <span className={'font-bold'}>SHA:</span> This lets you manually enter the SHA of the commit you want to visualize.
          </li>
        </ul>
      </div>
    </div>
  );
}

export default Help;
