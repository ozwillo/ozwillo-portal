import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

// Components
import DropdownMenu from '../components/dropdown-menu';

//action
import { fetchOrganizations } from '../actions/organization';
import { fetchUserOrganizations } from '../actions/organization';

class Organization extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    componentDidMount() {
        this.props.fetchUserOrganizations();
    }

    render() {
        return <section className="organization oz-body flex-col">

            <section>
                <h1 className="title">
                    {this.context.t('search')}
                </h1>

                <form className="search oz-form">
                    <input className="field form-control" type="text" placeholder={this.context.t('ui.search')}/>
                </form>
            </section>


            <section>
                <h1 className="title">
                    {this.context.t('my organization')}
                </h1>

                <form className="search oz-form">
                    <input className="field form-control" type="text" placeholder={this.context.t('ui.search')}/>
                </form>

                <ul className="organisations-list undecorated-list">
                {

                    this.props.userOrganizations.map((org) => {
                        return <li key={org.id} className="item">
                            <DropdownMenu header={org.name} className="organization">
                                <h1>{org.label}</h1>
                            </DropdownMenu>
                        </li>
                    })
                }
                </ul>
            </section>

        </section>;
    }

}

const mapStateToProps = state => {
    return {
        organizations: state.organization.organizations,
        userOrganizations: state.userInfo.organizations
    };
};

const mapDispatchToProps = dispatch => {
    return {
        fetchOrganizations() {
            return dispatch(fetchOrganizations());
        },
        fetchUserOrganizations() {
            return dispatch(fetchUserOrganizations());
        }
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(Organization);