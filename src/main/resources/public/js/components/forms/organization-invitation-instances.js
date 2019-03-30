import React from "react";
import PillButton from "../pill-button";
import PropTypes from "prop-types";
import {i18n} from "../../config/i18n-config";
import { t } from "@lingui/macro"



export default class OrganizationInvitationInstances extends React.Component {

    state = {
        instancesArray: [],
        instancesSelected: this.props.instancesSelected ? [...this.props.instancesSelected] :[]
    };


    componentDidMount = async () => {
        this.setState({instancesArray: this.props.instancesSelected});
    };

    _sortByName = (a, b) => {
        let textA = a.applicationInstance.name.toUpperCase();
        let textB = b.applicationInstance.name.toUpperCase();
        return (textA < textB) ? -1 : (textA > textB) ? 1 : 0;
    };

    _handleInstance = (instance) => {
        let {instancesArray} = this.state;
        let index = instancesArray.findIndex((elem) => elem.id === instance.id);
        if (!(index > -1)) {
            instancesArray.push(instance);
        } else {
            instancesArray.splice(index, 1);
        }
        this.setState({instancesArray});
        this.props.callBackInstances(instancesArray);
    };

    _handlePillButtonHistory = (instance) => {
        let {instancesSelected} = this.state;
        if (instancesSelected && instancesSelected.length > 0) {
            let index = instancesSelected.findIndex((elem) => elem.id === instance.id);
            if ((index > -1)) {
                instancesSelected.splice(index, 1);
                this.setState({instancesSelected});
                return true;
            }else{
                return false;
            }
        }
    };

    _displayPillButtons = () => {
        const {instances} = this.props;
        instances.sort(this._sortByName);
        return instances.map(instance => {
            return (
                <PillButton key={instance.id}
                            text={instance.applicationInstance.name}
                            onClick={() => this._handleInstance(instance)}
                            isActive={this._handlePillButtonHistory(instance)}
                />
            )
        });
    };


    render() {
        return (
            <div className={"organization-invitation-instance"}>
                <p>{i18n._(t`organization.desc.add-instances-to-members`)} : </p>
                <div className={"content"}>
                    {this._displayPillButtons()}
                </div>
            </div>
        )

    }
}

OrganizationInvitationInstances.propTypes = {
    instances: PropTypes.array,
    callBackInstances: PropTypes.func,
    instancesSelected: PropTypes.array
};
