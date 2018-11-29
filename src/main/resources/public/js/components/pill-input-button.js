import React from "react";
import PropTypes from "prop-types";

export default class PillInputButton extends React.Component {

    render(){
        const {id, label, ...rest} = this.props;
        return(
            <React.Fragment>
                <input
                    {...rest}
                    id={id}
                    type="checkbox"
                    className={"pill-input"}/>
                <label htmlFor={id} className={"pill-label badge badge-pill"}>
                    {label}
                </label>
            </React.Fragment>
        )
    }
}

PillInputButton.propTypes = {
    label: PropTypes.string.isRequired,
    onChange: PropTypes.func,
    id: PropTypes.string.isRequired,
    name: PropTypes.string,
    checked: PropTypes.bool
};
