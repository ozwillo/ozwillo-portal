'use strict';

import React from "react";
import { withRouter } from 'react-router';
import "../util/csrf";

import PropTypes from "prop-types";


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

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    componentDidMount() {
        //Load FranceConnect data
        $.ajax({
            url: '/my/api/profile/franceconnect'
        })
        .done(data => {
            //Search different data between user profile and franceConnect
            const franceConnectProfile = {};
            Object.keys(data.franceConnectProfile).forEach((field) => {
                if(data.userProfile[field] !== data.franceConnectProfile[field] &&
                    field !== "displayName"){
                    franceConnectProfile[field] = data.franceConnectProfile[field];
                }
            });

            if(!Object.keys(franceConnectProfile).length){
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

        for(let pair of formData.entries()){
            jsonData[pair[0]] = pair[1];
        }

        //Update user's data
        $.ajax({
            url: '/my/api/profile',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(jsonData)
        }).done(() => {
            this.props.history.push('/my/profile');
        }).fail((xhr, status, err) => {
            console.error('Update user\'s data: ', err);
        })

    }

    render() {
        const userProfile = this.state.userProfile;
        const franceConnectProfile = this.state.franceConnectProfile

        if(!userProfile){
            return <section className="synchronize-fc-profile">
                <i className="fa fa-spinner fa-spin spinner"></i>
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
                            <legend>{this.context.t(languageData[item])}</legend>
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
                    <input type="submit" value="save" className="btn oz-btn-save"/>
                </div>
            </form>
        </section>
    }
}
const SynchronizeFCProfileWithRouter = withRouter(SynchronizeFCProfile);

class SynchronizeFCProfileWrapper extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    render() {
        return <div className="oz-body page-row page-row-expanded">

            <div className="container-fluid">
                <div className="row">
                    <div className="col-md-12">
                        <h1 className="text-center">
                            <img src="/img/profile-lg.png" />
                            <span>{this.context.t('my.profile')}</span>
                        </h1>
                    </div>
                </div>
            </div>

            <div className="oz-body-content">
                <SynchronizeFCProfileWithRouter/>
            </div>

            <div className="push"></div>
        </div>;
    }
}


export default SynchronizeFCProfileWrapper;