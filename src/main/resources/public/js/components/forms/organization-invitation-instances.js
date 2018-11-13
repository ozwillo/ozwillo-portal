import React from "react";
import PillButton from "../pill-button";
import PropTypes from "prop-types";


export default class OrganizationInvitationInstances extends React.Component {


    componentDidMount = async () => {

    };

    state = {
      instancesArray: []
    };

    _sortByName = (a,b) => {
        let textA = a.applicationInstance.name.toUpperCase();
        let textB = b.applicationInstance.name.toUpperCase();
        return (textA < textB) ? -1 : (textA > textB) ? 1 : 0;
    };

    _handleInstance = (instance) => {
        let {instancesArray} = this.state;
        let index = instancesArray.findIndex((elem) => elem.id === instance.id);
        if(!(index > -1)){
            instancesArray.push(instance);
        }else{
            instancesArray.splice(index,1);
        }
        this.setState({instancesArray});
    };

    _displayPillButtons = () => {
        const {instances} = this.props;
        instances.sort(this._sortByName);
        return instances.map(instance => {
           return (
               <PillButton key={instance.id}
                           alt={instance.applicationInstance.name}
                           text={instance.applicationInstance.name}
                           onClick={() => this._handleInstance(instance)}/>
           )
        });
    };


    render(){
        return(
            <div className={"organization-invitation-instance"}>
                <div className={"content"}>
                    {this._displayPillButtons()}
                </div>
            </div>
        )

    }
}

OrganizationInvitationInstances.propTypes = {
    instances: PropTypes.array
};