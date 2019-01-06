import React from 'react';
import PropTypes from 'prop-types';
import {SubmitButton} from "./form";

import { i18n } from "../../config/i18n-config"
import { t } from "@lingui/macro"

class FranceConnectForm extends React.Component {

    static propTypes = {
        userProfile: PropTypes.object.isRequired,
        passwordChangeEndpoint: PropTypes.string,
        unlinkFranceConnectEndpoint: PropTypes.string,
        linkFranceConnectEndpoint: PropTypes.string
    };

    render() {
        const userProfile = this.props.userProfile || {};

        return <form id="FranceConnectForm"
                     method="post" action={this.props.linkFranceConnectEndpoint}
                     className={`oz-form ${this.props.className}`}>
            <div>
                <h2 className="sub-title">{i18n._(t`franceconnect.name`)}</h2>
            </div>

            {
                userProfile.franceconnect_sub && userProfile.email_verified &&
                <div className="text-center">
                    <a href={this.props.unlinkFranceConnectEndpoint} className="btn btn-submit">
                        {i18n._(t`franceconnect.form.desynchronize`)}
                    </a>
                </div>

            }

            {
                userProfile.franceconnect_sub && !userProfile.email_verified &&
                <div className="text-center">
                    <a className="btn btn-submit disabled">
                        {i18n._(t`franceconnect.form.desynchronize-without-pwd`)}
                    </a>
                    <span className="help-block">{i18n._(t`franceconnect.form.desynchronize-without-pwd-help`)}</span>
                </div>
            }

            {
                !userProfile.franceconnect_sub && userProfile.email_verified &&
                <fieldset>
                    <input type="hidden" name="continue"
                           value={`${window.location.origin}/my/profile/franceconnect`}/>
                    <SubmitButton label={i18n._(t`franceconnect.form.synchronise`)}/>
                </fieldset>
            }
        </form>;
    }
}

export default FranceConnectForm;
