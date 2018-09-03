import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';

//Components
import OrganizationForm from '../forms/organization-form';

//actions
import {fetchCountries} from '../../actions/config';
import {updateOrganizationFormAction} from '../../actions/components/organization-form';
import {fetchOrganizationInfo, fetchUpdateOrganization} from '../../actions/organization';

class AdminTabHeader extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    render() {
        return <Link className="undecorated-link" to={`/my/organization/${this.props.organization.id}/admin`}>
            <header className="tab-header">
                <span>{this.context.t('organization.desc.admin')}</span>
            </header>
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

        this.state = {
            isLoading: true
        };

        this.onSubmit = this.onSubmit.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        this.props.updateOrganizationForm(nextProps.orgInfo);
    }

    componentDidMount() {
        // Fetch information of current organization
        if (!this.isPersonal) {
            this.props.fetchOrganizationInfo(this.props.organization.dc_id)
                .then(() => this.setState({isLoading: false}));
        }
        this.props.fetchCountries();
    }

    onSubmit(org) {
        this.setState({isLoading: true});

        this.props.fetchUpdateOrganization(org)
            .then(() => {
                this.setState({
                    isLoading: false,
                    success: this.context.t('organization.desc.form.success')
                })
            })
            .catch(err => {
                this.setState({
                    isLoading: false,
                    success: '',
                    error: err.error
                })
            });
    }

    render() {
        if (!this.props.orgInfo) {
            return (
                <div className="container-loading text-center">
                    <i className="fa fa-spinner fa-spin loading"/>
                </div>
            );
        } else {
            return (
                <article className="admin-tab">

                    <OrganizationForm onSubmit={this.onSubmit} countries={this.props.countries}
                                      isLoading={this.state.isLoading}
                                      label={this.context.t('ui.save')}
                                      alreadyRegistered={true}
                                      initialTaxRegNum={this.props.orgInfo.tax_reg_num}/>

                    <div className="text-center">
                        {
                            this.state.error &&
                            <span className="error-message">{this.state.error}</span>
                        }
                        {
                            this.state.success &&
                            <span className="success-message">{this.state.success}</span>
                        }
                    </div>
                </article>
            );
        }
    }
}

const mapStateToProps = state => {
    return {
        countries: state.config.countries,
        organization: state.organization.current,
        orgInfo: state.organization.current.info

    };
};

const mapDispatchToProps = dispatch => {
    return {
        fetchCountries() {
            return dispatch(fetchCountries());
        },
        updateOrganizationForm(info) {
            return dispatch(updateOrganizationFormAction(info))
        },
        fetchUpdateOrganization(org) {
            return dispatch(fetchUpdateOrganization(org));
        },
        fetchOrganizationInfo(dcId) {
            return dispatch(fetchOrganizationInfo(dcId));
        }
    };
};

const AdminTabWithRedux = connect(mapStateToProps, mapDispatchToProps)(AdminTab);

export {
    AdminTabHeaderWithRedux as AdminTabHeader,
    AdminTabWithRedux as AdminTab
};