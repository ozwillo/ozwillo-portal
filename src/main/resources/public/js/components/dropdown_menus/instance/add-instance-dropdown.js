import React from 'react';
import { connect } from 'react-redux';

//Components
import DropDownMenu from "../../dropdown-menu";
import AddInstanceDropdownHeader from './add-instance-dropdown-header';
import AddInstanceDropdownFooter from './add-instance-dropdown-footer';



class AddInstanceDropdown extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            instances: this.props.instances,
            instance: (this.props.instances.length && this.props.instances[0]) || this.defaultInstance
        };

        //bind methods
        this.onAddInstance = this.onAddInstance.bind(this);
        this.onAddMember = this.onAddMember.bind(this);
        this.onRemoveMember = this.onRemoveMember.bind(this);
        this.onUpdateInstance = this.onUpdateInstance.bind(this);
        this.onChangeInstance = this.onChangeInstance.bind(this);
        this.filterMembersWithoutAccess = this.filterMembersWithoutAccess.bind(this);
    }

   get defaultInstance() {
        return {
            isPublic: true,
            members: []
        }
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            instances: nextProps.instances
        })
    }

    onAddInstance(instance) {
        console.log('TODO onAddInstance :', instance);
    }

    onAddMember(member) {
        const members = Object.assign([], this.state.instance.members);
        members.push(member);
        this.setState({
            instance: Object.assign({}, this.state.instance, { members })
        });
    }

    onRemoveMember(e) {
        const memberIndex = parseInt(e.currentTarget.dataset.member, 10);
        const members = Object.assign([], this.state.instance.members);
        members.splice(memberIndex, 1);
        this.setState({
            instance: Object.assign({}, this.state.instance, { members })
        });
    }

    onChangeInstance(id) {
        const instance = this.state.instances.find((instance) => {
            return instance.id === id;
        });


        this.setState({ instance });
    }

    onUpdateInstance(instance) {
        this.setState({
            instance:  Object.assign({}, this.state.instance, instance)
        });
    }

    filterMembersWithoutAccess(member) {
        return !this.state.instance.members.find((m) => {
            return member.id === m.id;
        });
    }

    render() {
        const instances =  this.state.instances;
        const instance = this.state.instance;
        const membersWithoutAccess = this.props.members.filter(this.filterMembersWithoutAccess);

        const Header = <AddInstanceDropdownHeader
            instances={instances}
            instance={instance}
            onAddInstance={this.onAddInstance}
            onUpdateInstance={this.onUpdateInstance}
            onChangeInstance={this.onChangeInstance}/>;

        const Footer = (!instance.isPublic &&
            <AddInstanceDropdownFooter
                members={membersWithoutAccess}
                onAddMember={this.onAddMember}/>) || null;

        return <DropDownMenu header={Header} footer={Footer} isAvailable={!instance.isPublic}>
            <section className='dropdown-content'>
                <ul className="list undecorated-list flex-col">
                    {
                        instance.members.map((member, i) => {
                            return <li key={member.id}>
                                <article className="item flex-row">
                                    <p className="name">{`${member.firstname} ${member.lastname}`}</p>

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
            </section>
        </DropDownMenu>;
    }
}


const mapStateToProps = state => {
    return {
        instances: state.instance.instances,
        members: state.member.members
    };
};

const mapDispatchToProps = dispatch => {
    return {};
};


export default connect(mapStateToProps, mapDispatchToProps) (AddInstanceDropdown);
