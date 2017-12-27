import React from 'react';
import PropTypes from 'prop-types';

class SideMenu extends React.Component {

    static propTypes = {
        isOpen: PropTypes.bool,
        onClickBackground: PropTypes.func
    };

    constructor(props) {
        super(props);

        //bind methods
        this.defaultOnClickBackground = this.defaultOnClickBackground.bind(this);
    }

    defaultOnClickBackground(e) {
        e.preventDefault();
    }

    render() {
        const isOpen = this.props.isOpen;
        return <section className={`side-menu flex-row ${this.props.className || ''} ${(!isOpen && 'hidden') || ''}`}>
            <div className="black-background" onClick={this.props.onClickBackground || this.defaultOnClickBackground}/>
            <aside className="menu">
                {this.props.children}
            </aside>
        </section>;
    }

}

export default SideMenu;