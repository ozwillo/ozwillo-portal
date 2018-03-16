import React from 'react';
import {PropTypes} from 'prop-types';
import {Tooltip} from 'react-tippy';


class CustomTooltip extends React.Component {

    static propTypes = {
        title: PropTypes.string.isRequired
    };

    constructor(props) {
        super(props);
    }

    render() {
        return <Tooltip title={this.props.title} className={this.props.className}
                        arrow={true}
                        size="big">
            {this.props.children}
        </Tooltip>;
    }

}

export default CustomTooltip;