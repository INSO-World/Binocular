'use strict';

import cx from 'classnames';
import styles from './styles.scss';

export default () => (
  <div className={cx('box', styles.help)}>
    <h1 className="title">File Evolution Help</h1>
  </div>
);
