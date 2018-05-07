import React from 'react';
import {connect} from 'react-redux';

//Components
import DropDownMenu from '../../dropdown-menu';
import AddInstanceDropdownHeader from './add-instance-dropdown-header';
/*import AddInstanceDropdownFooter from './add-instance-dropdown-footer';*/

//Action
import {fetchAddInstanceToOrg} from '../../../actions/app-store';

//config
import Config from '../../../config/config';
import PropTypes from 'prop-types';

const AppTypes = Config.appTypes;

class AddInstanceDropdown extends React.Component {


    constructor(props) {
        super(props);

        this.state = {
            app: null,
            members: []
        };

        //bind methods
        this.onAddInstance = this.onAddInstance.bind(this);
        this.onAddMember = this.onAddMember.bind(this);
        this.onRemoveMember = this.onRemoveMember.bind(this);
        this.onChangeApp = this.onChangeApp.bind(this);
        this.filterMembersWithoutAccess = this.filterMembersWithoutAccess.bind(this);
        this.filterApps = this.filterApps.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            instances: nextProps.instances
        })
    }

    onAddInstance() {
        if (!this.state.app) {
            return;
        }

        const orgId = this.props.organization.id;
        return this.props.fetchAddInstanceToOrg(orgId, this.state.app, this.state.members)
            .then(() => {
                this.setState({
                    app: null,
                    members: []
                })
            });
    }

    onAddMember(member) {
        const members = Object.assign([], this.state.members);
        members.push(member);
        this.setState({members});
    }

    onRemoveMember(e) {
        const memberIndex = parseInt(e.currentTarget.dataset.member, 10);
        const members = Object.assign([], this.state.members);
        members.splice(memberIndex, 1);
        this.setState({members: members});
    }

    onChangeApp(app) {
        this.setState({app});
    }

    filterMembersWithoutAccess(member) {
        return !this.state.members.find((m) => {
            return member.id === m.id;
        });
    }

    filterApps(app) {
        const org = this.props.organization;

        // only applications
        if (AppTypes.application !== app.type) {
            return false;
        }

        //Check types
        return (app.target_publicbodies && org.type === 'PUBLIC_BODY') ||
            (app.target_companies && org.type === 'COMPANY') ||
            (app.target_citizens && !org.type);
    }

    render() {
        const apps = this.props.apps.filter(this.filterApps);
        const app = this.state.app;
        const org = this.props.organization;
        //const membersWithoutAccess = org.members.filter(this.filterMembersWithoutAccess);

        const Header = <AddInstanceDropdownHeader
            apps={apps}
            app={app}
            onAddInstance={this.onAddInstance}
            onChangeInstance={this.onChangeApp}/>;

        //TODO may be for next feature: Add an instance to an organization and create in same time ACLs for users
        /*const Footer = <AddInstanceDropdownFooter
                members={membersWithoutAccess}
                onAddMember={this.onAddMember}/>;*/

        return <DropDownMenu header={Header} /*footer={Footer}*/ isAvailable={false}>
            {/*
            <section className='dropdown-content'>
                <ul className="list undecorated-list flex-col">
                    {
                        this.state.members.map((member, i) => {
                            return <li key={member.id}>
                                <article className="item flex-row">
                                    <p className="name">{member.name}</p>

                                    <div className="options">
                                        <button className="btn icon" onClick={this.onRemoveMember} data-member={i}>
                                            <i className="fa fa-trash option-icon delete"/>
                                        </button>
                                    </div>
                                </article>
                            </li>;
                        })
                    }
                </ul>
            </section>*/}
        </DropDownMenu>;
    }
}


const mapStateToProps = state => {
    return {
        apps: state.appStore.apps,
        organization: state.organization.current
    };
};

const mapDispatchToProps = dispatch => {
    return {
        fetchAddInstanceToOrg(orgId, app, members) {
            return dispatch(fetchAddInstanceToOrg(orgId, app, members));
        }
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(AddInstanceDropdown);
