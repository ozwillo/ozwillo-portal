import React from 'react';
import PropTypes from 'prop-types';

class Notification extends React.Component {

    static PropTypes = {
        message: PropTypes.string
    };

    constructor(props) {
        super(props);

        this.state = Object.assign({
            display: true,
        }, props);

        //bind methods
        this.close = this.close.bind(this);
    }

    close() {
        this.setState({
            display: false
        });
    }

    render() {
        return <section className={`notification ${this.state.display && 'hidden'}`} id="error-container">
            <div className="close" onClick={this.close}>
                <i className="fa fa-times"/>
            </div>
            <p className='message'>
                {this.state}
            </p>
        </section>;
    }
}

export default Notification;
