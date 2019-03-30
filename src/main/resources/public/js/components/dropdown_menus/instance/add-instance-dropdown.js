import React from 'react';
import {connect} from 'react-redux';

//Components
import DropDownMenu from '../../dropdown-menu';
import AddInstanceDropdownHeader from './add-instance-dropdown-header';

//Action
import {fetchAddInstanceToOrg} from '../../../actions/app-store';

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

    render() {
        const app = this.state.app;

        const Header = <AddInstanceDropdownHeader
            organization={this.props.organization}
            app={app}
            onAddInstance={this.onAddInstance}
            onChangeInstance={this.onChangeApp}/>;

        return <DropDownMenu header={Header} isAvailable={false} className="action-header">
        </DropDownMenu>;
    }
}


const mapStateToProps = state => {
    return {
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
