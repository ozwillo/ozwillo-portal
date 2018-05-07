import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';

//Components
import OrganizationForm from '../forms/organization-form';

//actions
import {fetchCountries} from '../../actions/config';
import {updateOrganizationFormAction} from '../../actions/components/organization-form';
import {fetchUpdateOrganization} from '../../actions/organization';

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

            isLoading: false
        };

        //bind methods
        this.onSubmit = this.onSubmit.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        this.props.updateOrganizationForm(nextProps.orgInfo);
    }

    componentDidMount() {
        this.props.fetchCountries();
    }

    onSubmit(info) {
        this.props.fetchUpdateOrganization(info)
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
        return <article className="admin-tab">
            <OrganizationForm onSubmit={this.onSubmit} countries={this.props.countries}
                              isLoading={this.state.isLoading} countryFieldIsDisabled={true}
                              label={this.context.t('ui.save')}/>

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
        </article>;
    }
}

const mapStateToProps = state => {
    return {
        countries: state.config.countries,
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
        fetchUpdateOrganization(info) {
            return dispatch(fetchUpdateOrganization(info));
        }
    };
};

const AdminTabWithRedux = connect(mapStateToProps, mapDispatchToProps)(AdminTab);

export {
    AdminTabHeaderWithRedux as AdminTabHeader,
    AdminTabWithRedux as AdminTab
};