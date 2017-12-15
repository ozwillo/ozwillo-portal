'use strict';

import React from "react";
import ReactDOM from "react-dom";
import "../util/csrf";
import "../util/my";

import {
    Form,
    InputText
} from "../util/form";


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
            url: franceconnect_service
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
                window.location.assign('/my/profile');
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
            url: profile_service,
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(jsonData)
        }).done(() => {
            window.location.assign('/my/profile');
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
                            <legend>{t(languageData[item])}</legend>
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

ReactDOM.render(<SynchronizeFCProfile />, document.getElementById("synchronize-fc-profile"));
