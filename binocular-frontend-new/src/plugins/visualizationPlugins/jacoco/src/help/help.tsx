function Help() {
  return (
    <>
      <h2>Jacoco Help</h2>
      <h3>Data</h3>
      <p>
        The jacoco visualization is designed to show the first or last test coverage report of a repository from a specific time frame. On
        hover, it displays various coverage metrics like the number of covered and missed instructions/lines/complexity/methods. The
        visualization is based on the Jacoco xml data, provided by the repository data plugin.
      </p>
      <br></br>
      <h4>Metrics:</h4>
      <div>
        <ul className={'list-disc ml-5'}>
          <li>
            <span className={'font-bold'}>Instruction:</span> The smallest unit JaCoCo counts are single Java byte code instructions.
          </li>
          <li>
            <span className={'font-bold'}>Line:</span> A source line is considered executed when at least one instruction that is assigned
            to this line has been executed. JaCoCo calculates line coverage for classes and source file based on the actual source lines
            covered.
          </li>
          <li>
            <span className={'font-bold'}>Complexity:</span> Based on the coverage status of each branch JaCoCo also calculates covered and
            missed complexity for each method. Missed complexity again is an indication for the number of test cases missing to fully cover
            a module.
          </li>
          <li>
            <span className={'font-bold'}>Method:</span> Each non-abstract method contains at least one instruction. A method is considered
            as executed when at least one instruction has been executed. As JaCoCo works on byte code level also constructors and static
            initializers are counted as methods.
          </li>
        </ul>
        <p>
          More Information about coverage metrics can be read
          <a href="https://www.eclemma.org/jacoco/trunk/doc/counters.html" target={'_blank'} rel="noreferrer">
            {' '}
            <span style={{ textDecoration: 'underline' }}>
              <b>here</b>
            </span>
          </a>
          .
        </p>
      </div>
      <h3>Parameters</h3>
      <div>
        <ul className={'list-disc ml-5'}>
          <li>
            <span className={'font-bold'}>Report:</span> Select the report from the desired time frame that you want to display.
          </li>
        </ul>
      </div>
    </>
  );
}

export default Help;
