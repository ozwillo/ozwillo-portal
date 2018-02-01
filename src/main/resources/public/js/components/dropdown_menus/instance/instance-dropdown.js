import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

//Components
import DropDownMenu from '../../dropdown-menu';
import InstanceInvitationForm from '../../forms/instance-invitation-form';
import InstanceDropdownHeader from './instance-dropdown-header';


//action
import { fetchDeleteAcl } from "../../../actions/acl";

class InstanceDropdown extends React.Component {

    static propTypes = {
        instance: PropTypes.object.isRequired,
        members: PropTypes.array.isRequired,
        isAdmin: PropTypes.bool
    };

    static defaultProps = {
        isAdmin: false
    };

    constructor(props){
        super(props);

        this.state = {
            error: null
        };

        //bind methods
        this.onClickConfigIcon = this.onClickConfigIcon.bind(this);
        this.onRemoveInstance = this.onRemoveInstance.bind(this);
        this.onUpdateInstance = this.onUpdateInstance.bind(this);
        this.filterMemberWithoutAccess = this.filterMemberWithoutAccess.bind(this);
        this.removeUserAccessToInstance = this.removeUserAccessToInstance.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            instance: nextProps.instance,
            members: nextProps.members
        });
    }

    onClickConfigIcon(instance) {
        console.log('onClickConfigIcon ', instance);
    }

    onRemoveInstance(instance) {
        console.log('onRemoveInstance ', instance);
    }

    onUpdateInstance(instance) {
        this.setState({ instance: Object.assign({}, this.props.instance, instance)});
    }

    filterMemberWithoutAccess(member) {
        if(!this.props.instance.users) {
            return true;
        }

        return !this.props.instance.users.find((user) => {
            return user.id === member.id;
        })
    }

    removeUserAccessToInstance(e) {
        const i = e.currentTarget.dataset.member;
        const member = this.props.instance.users[i];

        this.props.fetchDeleteAcl(member, this.props.instance)
            .then(() => {
                this.setState({ error: null});
            })
            .catch((err) => {
                this.setState({
                    error: { memberIndex: i, message: err.error}
                });
            });
    }

    render() {
        const isAdmin = this.props.isAdmin;
        const instance = this.props.instance;
        const membersWithoutAccess = this.props.members.filter(this.filterMemberWithoutAccess);

        const Header = <InstanceDropdownHeader
                            isAdmin={isAdmin}
                            instance={instance}
                            onClickConfigIcon={this.onClickConfigIcon}
                            onRemoveInstance={this.onRemoveInstance}
                            onUpdateInstance={this.onUpdateInstance}/>;

        const Footer = (isAdmin && !instance.isPublic && <footer>
            <InstanceInvitationForm members={membersWithoutAccess} instance={instance}/>
        </footer>) || null;

        return <DropDownMenu header={Header} footer={Footer} isAvailable={isAdmin && !instance.isPublic}>
            <section className='dropdown-content'>
                <ul className="list undecorated-list flex-col">
                    {
                        instance.users && instance.users.map((user, i) => {
                            return <li key={user.id || user.email}>
                                <article className="item flex-row">
                                    <p className="name">{`${(user.id && user.name) || user.email}`}</p>

                                    {
                                        this.state.error && i === this.state.error.memberIndex &&
                                        <span className="error">
                                            {this.state.error.message}
                                        </span>
                                    }

                                    <div className="options">
                                        {
                                            !user.id &&
                                            <i className="fa fa-spinner fa-spin option-icon"/>
                                        }

                                        <button className="btn icon" data-member={i}
                                                onClick={this.removeUserAccessToInstance}>
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

const mapDispatchToProps = dispatch => {
    return {
        fetchDeleteAcl(user, instance) {
            return dispatch(fetchDeleteAcl(user, instance));
        }
    };
};

export default connect(null, mapDispatchToProps)(InstanceDropdown);
