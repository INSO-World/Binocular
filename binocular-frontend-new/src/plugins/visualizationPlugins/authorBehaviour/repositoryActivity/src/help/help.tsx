function Help() {
  return (
    <>
      <h2>Repository Activity Help</h2>
      <h3>Data</h3>
      <p>
        The Repository Activity visualization displays activity patterns across the repository over time using interactive heatmaps. It
        tracks commits, aggregating them into color-coded cells where higher intensity indicates more activity.
      </p>
      <h3>Views</h3>
      <div>
        <ul className={'list-disc ml-5'}>
          <li>
            <span className={'font-bold'}>Activity Timeline:</span> A calendar-style overview showing daily activity across weeks and
            months. Each cell represents a single day, with rows for each day of the week (Monday to Sunday) and columns for each week.
            Click on any cell to drill down into the detailed weekly view.
          </li>
          <li>
            <span className={'font-bold'}>Weekly Heatmap:</span> A detailed 24-hour by 7-day heatmap for a selected week. Rows represent
            days of the week and columns represent hours of the day (0:00 to 23:00), showing at which times of day activity occurs.
          </li>
        </ul>
      </div>
      <h3>Parameters</h3>
      <div>
        <ul className={'list-disc ml-5'}>
          <li>
            <span className={'font-bold'}>Show Activity Timeline:</span> Toggle between the Activity Timeline overview and the detailed
            Weekly Heatmap view.
          </li>
        </ul>
      </div>
      <h3>Interaction</h3>
      <div>
        <ul className={'list-disc ml-5'}>
          <li>Hover over cells to see a tooltip with the date and a breakdown of activity types.</li>
          <li>Click a cell in the Activity Timeline to switch to the Weekly Heatmap for that week.</li>
          <li>Use the week picker to navigate between weeks, or use the previous/next buttons to step through weeks one at a time.</li>
          <li>Use the Back button in the Weekly Heatmap view to return to the Activity Timeline.</li>
        </ul>
      </div>
    </>
  );
}

export default Help;
