import React from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import PropTypes from "prop-types";

class Header extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    render() {
        return <header className="container-fluid oz-header">
            <div className="row">
                <div className="col-md-8 col-md-offset-2 alert alert-danger alert-dismissible hidden" id="error-container"
                     role="alert">
                    <button type="button" className="close" data-dismiss="alert" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <div id="error-message"></div>
                </div>
            </div>
            <div className="row clearfix">
                <div className="col-md-12">
                    <div className="row clearfix">

                        <div className="col-md-4">
                            <div className="logo-home">
                                <Link to='/my/dashboard'>
                                    <img src="/img/logo-ozwillo.png" alt="Logo Ozwillo"/>
                                </Link>
                            </div>
                        </div>

                        <div className="col-md-4 my-oasis">
                            <p className="text-center welcome" data-toggle="tooltip" data-placement="bottom"
                               title={ this.props.message }>
                                <Link to="/my/notif">
                                    <span>{this.context.t('ui.welcome')} {this.props.userInfo.nickname} </span>
                                    <span className="badge badge-notifications">
                                        { this.props.notificationsCount }
                                        <span className="sr-only">{ this.props.message }</span>
                                    </span>
                                </Link>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </header>
    }

}

const mapStateToProps = (state) => {
    return {
        notificationsCount: state.notifications.count,
        message: state.notifications.message,
        userInfo: state.userInfo
    }
}

export default connect(mapStateToProps)(Header);