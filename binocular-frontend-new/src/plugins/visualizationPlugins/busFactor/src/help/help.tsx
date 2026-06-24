function Help() {
  return (
    <>
      <h2>Bus Factor / CI Error Rate Help</h2>
      <h3>Data</h3>
      <p>
        This visualization shows two repository health metrics over time. The <strong>bus factor</strong> (left axis, blue) estimates how
        many contributors would need to leave before the project is in serious trouble — a higher value means knowledge is spread across
        people. The <strong>CI error rate</strong> (right axis, red) is the share of failed CI runs per time period, shown as a percentage.
      </p>
      <h3>Parameters</h3>
      <div>
        <ul className={'list-disc ml-5'}>
          <li>
            <span className={'font-bold'}>Repository path:</span> The repository to query, set in the settings. Without it no data can be
            loaded.
          </li>
          <li>
            <span className={'font-bold'}>Date range &amp; granularity:</span> Controlled by the global dashboard parameters. The
            granularity (days / weeks / months / years) determines the size of each time bucket on the x-axis.
          </li>
        </ul>
      </div>
    </>
  );
}

export default Help;
