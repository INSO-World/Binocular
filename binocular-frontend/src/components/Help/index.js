'use strict';

import styles from './styles.module.scss';

export default function Help({ children }) {
  return (
    <aside className={styles.helpTooltip} id="visualization-help-tooltip" role="tooltip">
      {children}
    </aside>
  );
}
