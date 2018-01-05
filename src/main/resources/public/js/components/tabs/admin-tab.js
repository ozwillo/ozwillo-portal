import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';


class AdminTabHeader extends React.Component {

    render() {
        return <Link className="undecorated-link" to={`/my/organization/${this.props.organization.id}/admin`}>
            <header className="tab-header">Admin</header>
        </Link>;
    }

}
const AdminTabHeaderWithRedux = connect(state => {
    return {
        organization: state.organization.current
    };
})(AdminTabHeader);

class AdminTab extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired,
    };

    constructor(props) {
        super(props);

        //bind methods
        this.onSubmit = this.onSubmit.bind(this);
    }

    onSubmit(e) {
        e.preventDefault();
        console.log('onSubmit !');
    }

    render() {
        return <article className="admin-tab">
            <header>Admin</header>
            <form onSubmit={this.onSubmit} className="form flex-col">
                <div className="row">
                    <label className="label">
                        Firstname :
                        <input name="fistname" type="text" placeholder="firstname" className="field"/>
                    </label>
                </div>

                <div className="row">
                    <label className="label">
                        Lastname :
                        <input name="lastname" type="text" placeholder="lastname" className="field"/>
                    </label>
                </div>

                <div className="row">
                    <label className="label">
                        Email :
                        <input name="email" type="email" placeholder="email" className="field"/>
                    </label>
                </div>

                <input type="submit" value="Send" className="btn oz-btn-save"/>
            </form>
        </article>;
    }
}

const mapStateToProps = state => {
    return {
        services: state.organization.current.services
    };
};
const AdminTabWithRedux = connect(mapStateToProps)(AdminTab);


export {
    AdminTabHeaderWithRedux as AdminTabHeader,
    AdminTabWithRedux as AdminTab
};