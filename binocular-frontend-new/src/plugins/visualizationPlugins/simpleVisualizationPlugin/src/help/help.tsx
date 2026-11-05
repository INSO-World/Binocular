function Help() {
  // TODO change if used as a template
  return (
    <>
      <h2>Example Help</h2>
      <h3>Data</h3>
      <p>The builds visualization provides an overview of the build statuses. It allows users to track the outcomes of builds, including</p>
      <h3>Parameters</h3>
      <div>
        <ul className={'list-disc ml-5'}>
          <li>
            <span className={'font-bold'}>Split X per Y:</span>
            Change the mode of visualization to show all X grouped by Y.
          </li>
          <li>
            <span className={'font-bold'}>Visualization Style:</span> Change the style of how the lines are calculated into either curved
            (default), stepped or linear.
          </li>
          <li>
            <span className={'font-bold'}>Show Sprints:</span> Overlay the in the dashboard defined sprints onto the chart.
          </li>
        </ul>
      </div>
    </>
  );
}

export default Help;
