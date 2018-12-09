'use strict';

import React from 'react';
import PropTypes from 'prop-types';
import { i18n } from "../app.js"
import { t } from "@lingui/macro"

const Loading = ({className}) =>
    <p className={'text-center ' + className}>
        <i className="fa fa-spinner fa-spin loading"/> {i18n._(t`ui.loading`)}
    </p>

export default Loading;
