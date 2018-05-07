import React from 'react';
import PropTypes from 'prop-types';

class UpdateTitle extends React.Component {

    static propTypes = {
        title: PropTypes.string
    };

    componentDidMount() {
        // First render
        document.title = this.props.title;
    }

    componentWillReceiveProps(nextProps) {
        // Other renders
        document.title = nextProps.title;
    }


    render() {
        return null;
    }
}

export default UpdateTitle;