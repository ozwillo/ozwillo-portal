import React from "react";
import PropTypes from 'prop-types';


export default class Stepper extends React.Component {

    componentDidMount() {

    }

    _displaySteps = () => {
        const {nbSteps, activeStep, onClickStep} = this.props;
        let result = [];
        for (let i = 1; i <= nbSteps; i++) {
            if (activeStep === i)
                result.push(<div key={`stepper-button-${i}`} className={"btn btn-default-inverse"} onClick={() => onClickStep(i)}>{i}</div>);
            else
                result.push(<div key={`stepper-button-${i}`} className={"btn btn-default"} onClick={() => onClickStep(i)}>{i}</div>);

            if(i !== nbSteps) {
                result.push(<div key={`separator-button-${i}`} className={"separator-container"}>
                    <span className={"separator"}/>
                </div>)
            }
        }

        return result;
    };

    render() {
        return (
            <div className={"stepper"}>
                {this._displaySteps()}
            </div>
        );
    }


}

Stepper.propTypes = {
    nbSteps: PropTypes.number.isRequired,
    activeStep: PropTypes.number,
    onClickStep: PropTypes.func
};