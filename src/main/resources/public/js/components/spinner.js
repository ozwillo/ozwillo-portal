import React from 'react';
import PropTypes from 'prop-types';

export default class Spinner extends React.PureComponent {


    render() {
        const {display, className, ...rest} = this.props;

        if (display) {
            return (
                <div {...rest} className={`container-loading text-center ${className}`}>
                    <i className="fa fa-spinner fa-spin loading"/>
                </div>
            );
        } else {
            return null;
        }
    }
}

Spinner.propTypes = {
    className: PropTypes.string,
    display: PropTypes.bool.isRequired,
    rest: PropTypes.object
};
