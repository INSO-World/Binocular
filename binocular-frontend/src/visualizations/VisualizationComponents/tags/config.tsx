'use strict';

import { connect } from 'react-redux';
import styles from './styles.module.scss';
import React from 'react';

const mapStateToProps = (state) => {
  return {};
};

const mapDispatchToProps = (dispatch) => {
  return {};
};

const TagsConfigComponent = (props) => {
  return <div className={styles.configContainer}></div>;
};

const DashboardConfig = connect(mapStateToProps, mapDispatchToProps)(TagsConfigComponent);

export default DashboardConfig;
