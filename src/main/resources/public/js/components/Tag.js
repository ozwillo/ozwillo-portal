import React from 'react';
import { withRouter } from 'react-router';
import PropTypes from 'prop-types';
import '../../css/components/catalog-card.css';

export class Tag extends React.PureComponent {
    render() {
        const { className, text, classNameButton } = this.props;

        return (
            <button type="button" key="indicator_button" className={`category-btn ${classNameButton}`}>
                <i className={className}></i>
                <span className="btn-label">{text}</span>
            </button>
        );
    }
}
Tag.propTypes = {
    className: PropTypes.string,
    text: PropTypes.string,
    classNameButton: PropTypes.string
};

export default withRouter(Tag);
