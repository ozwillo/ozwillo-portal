import React from "react";
import PropTypes from "prop-types";

export default class LabelSection extends React.PureComponent{
    render(){
        const {label, children} = this.props;
        return (
        <div className="label-section form-group">
            <label className="control-label">
                {label}
            </label>
            <div className={"label-section-content"}>
                {children}
            </div>
        </div>
        )
    }
}

LabelSection.propTypes = {
    label: PropTypes.string,
    children: PropTypes.node,
};