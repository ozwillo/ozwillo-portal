import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import { i18n } from "../config/i18n-config"
import { t } from "@lingui/macro"



// Components
import UpdateTitle from '../components/update-title';
import customFetch from '../util/custom-fetch';

//action
import {fetchUserOrganizations} from '../actions/organization';
import OrganizationAutoSuggest from "../components/autosuggests/organization-autosuggest";
import OrganizationCard from "../components/organization-card"
import {Redirect} from "react-router";

class OrganizationSearch extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            userOrganizationsFilter: '',
            isLoading: true,
            inputValue: '',
            organizationSelected: {},
            organizationsHistory: [],
            organizationHistoryMessage: ''
        };

        this.handleChange = this.handleChange.bind(this);
    }

    componentDidMount() {
        this._handleOrganizationsHistory();
    }

    _handleOrganizationsHistory = () => {
        customFetch("/my/api/organizationHistory")
            .then(res => {
                if(res && res.length >0){
                    this._sortOrganizationHistoryByDate(res);
                    this.setState({organizationsHistory: res, organizationHistoryMessage: ''});
                } else {
                    this.setState({organizationHistoryMessage: i18n._(t`organization.search.history.empty`)});
                }
            });
    };

    _sortOrganizationHistoryByDate = (array) =>{
        array.sort(function(a,b){
            return new Date(b.date) - new Date(a.date);
        });
    };

    handleChange(e) {
        this.setState({
            [e.currentTarget.name]: e.currentTarget.value
        });
    }

    _displayOrganizationsHistory = () => {
        const {organizationsHistory, organizationHistoryMessage} = this.state;
        if (organizationsHistory.length > 0) {
            let result = [];
            organizationsHistory.map(organization => {
                let dcOrganizationId = organization.dcOrganizationId;
                let organizationCard = (
                    <OrganizationCard key={dcOrganizationId} organization={organization}/>);
                result.push(organizationCard)
            });
            return result;
        }else{
            return organizationHistoryMessage;
        }
    };

    render() {
        const {organization_id} = this.state.organizationSelected;
        if (organization_id) {
            return <Redirect to={`/my/organization/${organization_id}/`}/>
        }

        return <section className="organization-search oz-body wrapper flex-col">

            <UpdateTitle title={i18n._(t`organization.search.title`)}/>

            <section>
                <header className="title">
                    <span>{i18n._(t`organization.search.title`)}</span>
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
                        placeholder={i18n._(t`search.organization.search-organization`)}
                    />
                    <div className="flex-row options">
                        <Link to="/my/organization/create" className={"new-organization"}>
                            {i18n._(t`organization.search.new`)}
                        </Link>
                    </div>
                </form>

                <div className={"container-organization-history"}>
                    <p className={"history-title"}>
                        <i className={"fa fa-star"}/>
                        {i18n._(t`organization.search.history.description`)} :
                    </p>
                    <div className={"content-card-history"}>
                        {this._displayOrganizationsHistory()}
                    </div>
                </div>
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
