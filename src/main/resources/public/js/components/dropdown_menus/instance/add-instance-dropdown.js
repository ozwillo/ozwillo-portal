import React from 'react';
import {connect} from 'react-redux';

//Components
import DropDownMenu from '../../dropdown-menu';
import AddInstanceDropdownHeader from './add-instance-dropdown-header';

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
        return this.props.fetchAddInstanceToOrg(orgId, this.state.app)
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

        const Header = <AddInstanceDropdownHeader
            apps={apps}
            app={app}
            onAddInstance={this.onAddInstance}
            onChangeInstance={this.onChangeApp}/>;

        return <DropDownMenu header={Header} isAvailable={false} className="action-header">
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
        fetchAddInstanceToOrg(orgId, app) {
            return dispatch(fetchAddInstanceToOrg(orgId, app));
        }
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(AddInstanceDropdown);
