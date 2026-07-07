/**
 * Help text for the Bus Factor / CI Quadrant widget.
 * This is rendered inside the widget's help popup (the "?" button in the item bar).
 * Keep it short and explain what the axes and quadrants mean plus the settings.
 */
function Help() {
  return (
    <>
      <h2>Bus Factor / CI Quadrant</h2>
      <p>
        Every Module is shown as a Point: x = CI-Error rate, y = Bus-Factor. The four quadrants show the classes of risk. Threshold values
        can be edited in settings. The date settings only have impact on CI error rate not on the bus factor.
      </p>
    </>
  );
}
export default Help;
