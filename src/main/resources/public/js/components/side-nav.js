import React from "react";
import PropTypes from 'prop-types';

export default class SideNav extends React.Component {


    state = {
        isOpen: true,
    };

    _switchOpenState = () => {
        return this.setState({isOpen: !this.state.isOpen})
    };

    _displaySideNav = () => {
        const {isOpen} = this.state;
        if (isOpen) {
            return {
                width: '250px',
                transform : 'translate(0px, 0px)',
                transition: 'transform 0.8s, margin-right 0.85s',
                marginRight: '0'
        }
        } else {
            return {
                width: '250px',
                transform : 'translateX(-250px)',
                transition: 'transform 0.8s, margin-right 0.85s',
                marginRight: '-250px'


        }
        }
    };

    _displaySideNavButton = () => {
        const {isOpen} = this.state;
        const {isCloseChildren} = this.props;
        if (!isOpen) {
            return (
                <React.Fragment>
                    <div className={"side-nav-tongue"} onClick={() => this._switchOpenState()}>
                        <i className={"fa fa-chevron-right"}/>
                    </div>
                    {isCloseChildren}
                </React.Fragment>

            )
        }
    };

    render() {
        const {isOpenHeader} = this.props;

        return (
            <React.Fragment>
                <div style={this._displaySideNav()} className={"side-nav"}>
                    <div className={"side-nav-header"}>
                        <div className={"open-header-children"}>
                            {isOpenHeader}
                        </div>
                        <i className={"fa fa-chevron-left"} onClick={() => this._switchOpenState()}/>
                    </div>
                    <div className={"content"}>
                        {this.props.children}
                    </div>
                </div>
                {this._displaySideNavButton()}
            </React.Fragment>

        )
    }
}

SideNav.propTypes = {
    children: PropTypes.node,
    isCloseChildren: PropTypes.node,
    isOpenHeader: PropTypes.node
};
