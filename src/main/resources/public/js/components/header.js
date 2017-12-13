import React from 'react';
import { Link } from 'react-router-dom';

class Header extends React.Component {

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
                               title="Vous avez 0 notification en attente">
                                <Link to="/my/notif">
                                    <span>Welcome Benoit</span>
                                    <span className="badge badge-notifications">
                                0 <span className="sr-only">Unread notifications</span>
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

export default Header;