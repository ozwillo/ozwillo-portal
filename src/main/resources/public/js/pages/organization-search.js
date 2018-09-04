import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';

// Components
import UpdateTitle from '../components/update-title';
import customFetch from '../util/custom-fetch';

//action
import {fetchUserOrganizations} from '../actions/organization';
import OrganizationAutoSuggest from "../components/autosuggests/organization-autosuggest";
import OrganizationCard from "../components/organization-card"
import {Redirect} from "react-router";

class OrganizationSearch extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            userOrganizationsFilter: '',
            isLoading: true,
            inputValue: '',
            organizationSelected: {},
            organizationsHistory: []
        };

        this.handleChange = this.handleChange.bind(this);
    }

    componentDidMount() {
        this.props.fetchUserOrganizations()
            .then(() => {
                this.setState({isLoading: false});
            });
        this._handleOrganizationsHistory();

    }

    _handleOrganizationsHistory = () => {
        customFetch("/my/api/organizationHistory")
            .then(res => {
               this.setState({organizationsHistory: res})
            });
    };

    handleChange(e) {
        this.setState({
            [e.currentTarget.name]: e.currentTarget.value
        });
    }

    _displayOrganizationsHistory = () => {
        let {organizationsHistory} = this.state;
        let result = [];
        if(organizationsHistory && organizationsHistory.length > 0) {
            for (let organization of organizationsHistory) {
                let organizationId = organization.organizationId;
                let organizationCard = (
                    <OrganizationCard key={organizationId} organization={organization}/>);
                result.push(organizationCard)
            }
        }

        return result;
    };

    render() {
        let {organization_id} = this.state.organizationSelected;
        if (organization_id) {
            return <Redirect to={`/my/organization/${organization_id}/`}/>
        }

        return <section className="organization-search oz-body wrapper flex-col">

            <UpdateTitle title={this.context.t('organization.search.title')}/>

            <div className="flex-row end options">
                <Link to="/my/organization/create" className="btn btn-default undecorated-link">
                    {this.context.t('organization.search.new')}
                </Link>
            </div>

            <section>
                <header className="title">
                    <span>{this.context.t('organization.search.title')}</span>
                </header>

                <form className="search oz-form">
                    <OrganizationAutoSuggest
                        className={"field"}
                        value={this.state.inputValue}
                        name={"orga-auto-suggest"}
                        onChange={(event, value) => this.setState({inputValue: value})}
                        onOrganizationSelected={(value) => {
                            this.setState({organizationSelected: value})
                        }}
                        placeholder={this.context.t('ui.search')}
                    />
                </form>

                <div className={"container-organization-history"}>
                    <div className={"content"}>
                    {this._displayOrganizationsHistory()}
                    </div>
                </div>

                {
                    this.state.isLoading &&
                    <div className="loading-container">
                        <i className="fa fa-spinner fa-spin loading"/>
                    </div>
                }
            </section>

        </section>;
    }

}

const mapStateToProps = state => {
    return {
        userOrganizations: state.organization.organizations
    };
};

const mapDispatchToProps = dispatch => {
    return {
        fetchUserOrganizations() {
            return dispatch(fetchUserOrganizations());
        }
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(OrganizationSearch);