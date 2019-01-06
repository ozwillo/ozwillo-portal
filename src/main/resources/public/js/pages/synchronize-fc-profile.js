'use strict';

import React from "react";
import {withRouter} from 'react-router';

import PropTypes from "prop-types";
import UpdateTitle from '../components/update-title';
import customFetch from "../util/custom-fetch";

import { i18n } from "../config/i18n-config"
import { t } from "@lingui/macro"

const languageData = {
    'given_name': 'my.profile.personal.firstname',
    'middle_name': 'my.profile.personal.middlename',
    'family_name': 'my.profile.personal.lastname',
    'birthdate': 'my.profile.personal.birthdate',
    'gender': 'my.profile.personal.gender',
    'phone_number': 'my.profile.personal.phonenumber',
    'address': 'my.profile.personal.address'
};

/**
 * This class is used to update user's information with data from FranceConnect
 */
class SynchronizeFCProfile extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            userProfile: null,
            franceConnectProfile: null
        };

        //bind functions
        this.save = this.save.bind(this);
    }


    componentDidMount() {
        //Load FranceConnect data
        $.ajax({
            url: '/my/api/profile/franceconnect'
        })
            .done(data => {
                //Search different data between user profile and franceConnect
                const franceConnectProfile = {};
                Object.keys(data.franceConnectProfile).forEach((field) => {
                    if (data.userProfile[field] !== data.franceConnectProfile[field] &&
                        field !== "displayName") {
                        franceConnectProfile[field] = data.franceConnectProfile[field];
                    }
                });

                if (!Object.keys(franceConnectProfile).length) {
                    //No data to update
                    this.props.history.push('/my/profile');
                }

                this.setState({
                    userProfile: data.userProfile,
                    franceConnectProfile
                });

            })
            .fail((xhr, status, err) => {
                console.error(err);
            })
    }

    save(e) {
        //cancel submit
        e.preventDefault();

        //get all data from form
        const formData = new FormData(this.refs.form);
        const jsonData = Object.assign({}, this.state.userProfile);

        for (let pair of formData.entries()) {
            jsonData[pair[0]] = pair[1];
        }

        //Update user's data
        customFetch('/my/api/profile', {
            method: 'POST',
            json: jsonData
        })
        .then(() => {
            this.props.history.push('/my/profile');
        })
    }

    render() {
        const userProfile = this.state.userProfile;
        const franceConnectProfile = this.state.franceConnectProfile

        if (!userProfile) {
            return <section className="synchronize-fc-profile loading-container">
                <i className="fa fa-spinner fa-spin loading"/>
            </section>
        }

        return <section className="synchronize-fc-profile">
            <form ref="form" onSubmit={this.save}>
                <div className="row">
                    <p className="item title">Votre profile</p>
                    <p className="item title">FranceConnect</p>
                </div>

                {
                    Object.keys(this.state.franceConnectProfile).map((item) => {
                        return <fieldset key={item}>
                            <legend>{i18n._(t`${languageData[item]}`)}</legend>
                            <div className="row">
                                <label className="item">
                                    <input id={item} type="radio" name={item} value={userProfile[item]}/>
                                    {userProfile[item]}
                                </label>

                                <label className="item">
                                    <input id={item} type="radio" name={item} value={franceConnectProfile[item]}/>
                                    {franceConnectProfile[item]}
                                </label>
                            </div>
                        </fieldset>
                    })
                }

                <div className="row submit">
                    <input type="submit" value="save" className="btn btn-submit"/>
                </div>
            </form>
        </section>
    }
}

const SynchronizeFCProfileWithRouter = withRouter(SynchronizeFCProfile);

class SynchronizeFCProfileWrapper extends React.Component {

    render() {
        return <div className="oz-body wrapper flex-col">

            <UpdateTitle title={i18n._(t`my.profile`)}/>

            <header className="title">
                <span>{i18n._(t`my.profile`)}</span>
            </header>

            <SynchronizeFCProfileWithRouter/>

            <div className="push"/>
        </div>;
    }
}


export default SynchronizeFCProfileWrapper;
