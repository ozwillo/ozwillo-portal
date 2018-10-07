'use strict';

import React from 'react';
import PropTypes from 'prop-types';

const DropdownBlockError = ({errorMessage}) =>
    <div className="alert alert-danger" role="alert">
        <button type="button" className="close" data-dismiss="alert" aria-label="Close">
            <span aria-hidden="true">&times;</span>
        </button>
        { errorMessage }
    </div>

DropdownBlockError.contextTypes = {
    errorMessage: PropTypes.string.isRequired
};

const DropdownBlockSuccess = ({successMessage}) =>
    <div className="alert alert-success" role="alert">
        <button type="button" className="close" data-dismiss="alert" aria-label="Close">
            <span aria-hidden="true">&times;</span>
        </button>
        { successMessage }
    </div>

DropdownBlockSuccess.contextTypes = {
    successMessage: PropTypes.string.isRequired
};

export { DropdownBlockError, DropdownBlockSuccess };
