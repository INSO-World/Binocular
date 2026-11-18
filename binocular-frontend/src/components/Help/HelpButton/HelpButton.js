'use strict';

import PropTypes from 'prop-types';
import cx from 'classnames';

import Icon from '../../icon';
import styles from './help-button.module.scss';

const HelpButton = (props) => (
  <div className={cx(styles.helpButton)}>
    <div className={cx({ [styles.spinning]: props.spinning })}>
      <a
        href="#"
        aria-label={props.shown ? 'Close visualization help' : 'Show visualization help'}
        aria-expanded={props.shown}
        aria-controls={props.controls}
        onKeyDown={(event) => {
          if (props.controls && event.key === 'Escape') props.onClick();
        }}
        onClick={(event) => {
          event.preventDefault();
          props.onClick();
        }}>
        <Icon name={props.icon} />
      </a>
    </div>
  </div>
);

HelpButton.propTypes = {
  spinning: PropTypes.bool,
};

export default HelpButton;
