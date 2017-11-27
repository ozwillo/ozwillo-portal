import React from 'react';
import "../csrf";

import { SubmitButton } from "../util/form";


class FranceConnectForm extends React.Component {

    static PropTypes = {
        isSubscribe: React.PropTypes.boolean
    };

    render() {
        return <form id="account"
                     method="post" action="https://accounts.ozwillo-dev.eu/a/franceconnect/login"
                     className="form-horizontal">
            <div>
                <h2>France Connect</h2>
            </div>

            {
               this.props.isSubscribe &&
               <a href="https://accounts.ozwillo-dev.eu/a/franceconnect/unlink">
                   Disconnect from FranceConnect
               </a>

            }

            {
                !this.props.isSubscribe &&
                <fieldset>
                    <input type="hidden" name="continue" value="/my/profile" />
                    <SubmitButton label="Send !"/>
                </fieldset>
            }
        </form>;
    }
}

export default FranceConnectForm;