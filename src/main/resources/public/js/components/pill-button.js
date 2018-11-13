import React from "react";
import PropTypes from 'prop-types';

export default class PillButton extends React.PureComponent{

    state = {
        isActive: false
    };

    _handleActiveButton = () => {
        return this.state.isActive ? "btn-default-inverse": "btn-default";
    };

    _handleClick = () => {
        this.setState({isActive: !this.state.isActive},() => {
            this.props.onClick()
        });
    };

    render(){
        const {text} = this.props;

        return(
            <div className={`pill-button btn ${this._handleActiveButton()}`} onClick={this._handleClick}>
                <p className={"pill-text"}>{text}</p>
            </div>
        )
    }
}


PillButton.propTypes = {
    onClick: PropTypes.func,
    text: PropTypes.string,
};