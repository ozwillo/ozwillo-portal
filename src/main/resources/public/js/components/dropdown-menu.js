import React from 'react';
import PropTypes from 'prop-types';

class DropDownMenu extends React.Component {

    static propTypes = {
        header: PropTypes.node,
        footer: PropTypes.node,
        isOpen: PropTypes.bool,
        isAvailable: PropTypes.bool
    };

    static defaultProps = {
        isOpen: false,
        isAvailable: true
    };

    constructor(props) {
        super(props);

        this.state = {
            isOpen: props.isOpen
        };

        //bind methods
        this.dropDownToggle = this.dropDownToggle.bind(this);
    }

    dropDownToggle() {
        this.setState({
            isOpen: !this.state.isOpen
        });
    }

    render() {
        const isOpenClassName = (!this.state.isOpen && 'hidden') || '';
        const iconClassName = (this.state.isOpen && 'down') || 'right';
        const isAvailableClassName = (!this.props.isAvailable && 'invisible') || '';
        const isEmptyClassName = (!this.props.children && 'empty invisible') || '';

        return <section className={`oz-dropdown-menu flex-col ${this.props.className || ''}`}>
            <header className="header flex-row">
                <div className="content">{this.props.header}</div>
                <i onClick={this.dropDownToggle}
                   className={`fa fa-caret-${iconClassName} arrow-icon ${isEmptyClassName} ${isAvailableClassName}`}/>
            </header>

            <article className={`content ${isOpenClassName} ${isAvailableClassName} ${isEmptyClassName}`}>
                {this.props.children}
            </article>

            {
                this.props.footer &&
                <footer className="footer">
                    {this.props.footer}
                </footer>
            }
        </section>
    }

}

export default DropDownMenu;