'use strict';

import React from 'react';
import PropTypes from 'prop-types';

const Loading = ({className}, context) =>
    <p className={'text-center ' + className}>
        <i className="fa fa-spinner fa-spin loading"/> {context.t('ui.loading')}
    </p>

Loading.contextTypes = {
    t: PropTypes.func.isRequired
};


export default Loading;
