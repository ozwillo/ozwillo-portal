import React from 'react';
import PropTypes from 'prop-types';

class DropDownMenu extends React.Component {

    static propTypes = {
        header: PropTypes.node,
        isOpen: PropTypes.bool
    };

    constructor(props) {
        super(props);

        this.state = {
            isOpen: !!props.isOpen
        };

        //bind methods
        this.dropDownToggle = this.dropDownToggle.bind(this);
    }


    dropDownToggle(){
        this.setState({
            isOpen: !this.state.isOpen
        });
    }

    render() {
        const isOpen = this.state.isOpen;
        return <section className={`oz-dropdown-menu flex-col ${this.props.className || ''}`}>
            <header className="header flex-row">
                <div className="content">{this.props.header}</div>
                <i className={`fa fa-chevron-${(isOpen && 'up') || 'down'} arrow-icon`} onClick={this.dropDownToggle}/>
            </header>

            <article className={`content flex-row ${(!isOpen && 'hidden') || ''}`}>
                {this.props.children}
            </article>
        </section>
    }

}

export default DropDownMenu;