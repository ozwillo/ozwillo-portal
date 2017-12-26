import React from 'react';
import PropTypes from 'prop-types';
import {SubmitButton} from "./form";

class FranceConnectForm extends React.Component {

    static propTypes = {
        userProfile: PropTypes.object.isRequired,
        passwordChangeEndpoint: PropTypes.string,
        unlinkFranceConnectEndpoint: PropTypes.string,
        linkFranceConnectEndpoint: PropTypes.string
    };

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    render() {
        const userProfile = this.props.userProfile || {};

        return <form id="FranceConnectForm"
                     method="post" action={this.props.linkFranceConnectEndpoint}
                     className={`oz-form ${this.props.className}`}>
            <div>
                <h2 className="sub-title">{this.context.t("franceconnect.name")}</h2>
            </div>

            {
                userProfile.franceconnect_sub && userProfile.email_verified &&
                <div className="text-center">
                    <a href={this.props.unlinkFranceConnectEndpoint} className="btn btn-submit">
                        {this.context.t("franceconnect.form.desynchronize")}
                    </a>
                </div>

            }

            {
                userProfile.franceconnect_sub && !userProfile.email_verified &&
                <div className="text-center">
                    <a href={this.props.passwordChangeEndpoint} className="btn btn-submit">
                        {this.context.t("franceconnect.form.desynchronize-without-pwd")}
                    </a>
                </div>
            }

            {
                !userProfile.franceconnect_sub && userProfile.email_verified &&
                <fieldset>
                    <input type="hidden" name="continue"
                           value={`${window.location.origin}/my/profile/franceconnect`}/>
                    <SubmitButton label={this.context.t("franceconnect.form.synchronise")}/>
                </fieldset>
            }
        </form>;
    }
}

export default FranceConnectForm;