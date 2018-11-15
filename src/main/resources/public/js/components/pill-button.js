import React from "react";
import PropTypes from 'prop-types';

export default class PillButton extends React.PureComponent{

    state = {
        isActive: this.props.isActive ? this.props.isActive : false
    };


    componentWillReceiveProps(nextProps){
        if(nextProps.isActive){
           this.setState({isActive: nextProps.isActive});
        }
    }

    _handleActiveButton = () => {
        return this.state.isActive ? "btn-default-inverse": "btn-default";
    };

    _handleClick = () => {
        const {onClick} = this.props;
        if(onClick) {
            this.setState({isActive: !this.state.isActive}, () => {
                onClick()
            });
        }
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
    isActive: PropTypes.bool
};