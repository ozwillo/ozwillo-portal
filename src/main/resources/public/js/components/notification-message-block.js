import React from 'react';
import PropTypes from 'prop-types';

export default class NotificationMessageBlock extends React.PureComponent{

    componentDidMount() {}

    render(){
        const {type, message, close, display} = this.props;

        if(!display){
            return null;
        }else{
            return (
                <div className={`alert alert-${type}`} role="alert">
                    <button type="button" className="close" aria-label="Close" onClick={() => close()}>
                        <span aria-hidden="true">&times;</span>
                    </button>
                    { message }
                </div>
            )
        }
    }


}


NotificationMessageBlock.propTypes = {
    type: PropTypes.oneOf(['danger', 'success']).isRequired,
    message: PropTypes.string,
    close: PropTypes.func,
    display: PropTypes.bool
};
