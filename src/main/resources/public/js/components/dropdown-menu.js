import React from 'react';
import PropTypes from 'prop-types';

class DropDownMenu extends React.Component {

    static propTypes = {
        header: PropTypes.node,
        footer: PropTypes.node,
        isOpen: PropTypes.bool,
        isAvailable: PropTypes.bool,
        dropDownChange: PropTypes.func,
        dropDownIcon: PropTypes.node
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
        this.setState({isOpen: !this.state.isOpen}, () => {
            if (this.props.dropDownChange)
                this.props.dropDownChange(this.state.isOpen);
        });
    }

    render() {
        const isOpenClassName = (!this.state.isOpen && 'hidden') || '';
        const iconClassName = (this.state.isOpen && 'down') || 'right';
        const isAvailableClassName = (!this.props.isAvailable && 'hidden') || '';
        const isEmptyClassName = (!this.props.children && 'empty hidden') || '';
        const {dropDownIcon} = this.props;

        return <section className={`oz-dropdown-menu flex-col ${this.props.className || ''}`}>
            <header className="header flex-row">
                <div className="content">{this.props.header}</div>
                <div className={`dropdown-icons flex-row ${isAvailableClassName} ${isEmptyClassName}`}
                     onClick={this.dropDownToggle}>
                    {dropDownIcon}
                    <i className={`fa fa-caret-${iconClassName}`}/>
                </div>
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