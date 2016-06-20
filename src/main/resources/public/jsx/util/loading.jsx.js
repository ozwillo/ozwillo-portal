'use strict';

import React from 'react';

import t from './message';

const Loading = ({ className }) =>
    <p className={'text-center ' + className}>
        <i className="fa fa-spinner fa-spin"></i> {t('ui.loading')}
    </p>

module.exports = { Loading };
