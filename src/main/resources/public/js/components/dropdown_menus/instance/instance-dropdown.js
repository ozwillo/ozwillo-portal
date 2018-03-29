import React from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import Popup from 'react-popup';

//Components
import DropDownMenu from '../../dropdown-menu';
import InstanceInvitationForm from '../../forms/instance-invitation-form';
import InstanceDropdownHeader from './instance-dropdown-header';
import InstanceConfigForm from '../../forms/instance-config-form';
import CustomTooltip from '../../custom-tooltip';

//action
import {fetchDeleteAcl} from '../../../actions/acl';
import {fetchCreateSubscription, fetchDeleteSubscription} from '../../../actions/subscription';
import {fetchUpdateInstanceStatus} from '../../../actions/instance';

//Config
import Config from '../../../config/config';

const instanceStatus = Config.instanceStatus;

//Action
import {fetchUpdateServiceConfig} from '../../../actions/instance';

class InstanceDropdown extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    static propTypes = {
        instance: PropTypes.object.isRequired,
        members: PropTypes.array.isRequired,
        isAdmin: PropTypes.bool
    };

    static defaultProps = {
        isAdmin: false
    };

    constructor(props) {
        super(props);

        this.state = {
            error: null,
            status: {}
        };

        //bind methods
        this.onClickConfigIcon = this.onClickConfigIcon.bind(this);
        this.onRemoveInstance = this.onRemoveInstance.bind(this);
        this.onCancelRemoveInstance = this.onCancelRemoveInstance.bind(this);
        this.filterMemberWithoutAccess = this.filterMemberWithoutAccess.bind(this);
        this.removeUserAccessToInstance = this.removeUserAccessToInstance.bind(this);
        this.createSubscription = this.createSubscription.bind(this);
        this.deleteSubscription = this.deleteSubscription.bind(this);
        this.fetchUpdateServiceConfig = this.fetchUpdateServiceConfig.bind(this);
        this.searchSubForUser = this.searchSubForUser.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            instance: nextProps.instance,
            members: nextProps.members
        });
    }

    fetchUpdateServiceConfig(instanceId, catalogEntry) {
        return this.props.fetchUpdateServiceConfig(instanceId, catalogEntry)
            .then(() => {
                Popup.close();
            });
    }

    onClickConfigIcon(instance) {
        Popup.create({
            title: instance.name,
            content: <InstanceConfigForm instance={instance} onSubmit={this.fetchUpdateServiceConfig}/>
        }, true);
    }

    onRemoveInstance(instance) {
        this.props.fetchUpdateInstanceStatus(instance, instanceStatus.stopped);
    }

    onCancelRemoveInstance(instance) {
        this.props.fetchUpdateInstanceStatus(instance, instanceStatus.running);
    }

    filterMemberWithoutAccess(member) {
        if (!this.props.instance.users) {
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
                this.setState({
                    status: Object.assign({}, this.state.status, {
                        [member.id]: {error: null}
                    })
                });
            })
            .catch((err) => {
                this.setState({
                    status: Object.assign({}, this.state.status, {
                        [member.id]: {error: err.error}
                    })
                });
            });
    }

    createSubscription(e) {
        const el = e.currentTarget;
        const userId = el.dataset.user;
        const serviceId = el.dataset.service;

        this.setState({
            status: Object.assign({}, this.state.status, {
                [userId]: {isLoading: true}
            })
        });

        this.props.fetchCreateSubscription(this.props.instance.id, {user_id: userId, service_id: serviceId})
            .then(() => {
                this.setState({
                    status: Object.assign({}, this.state.status, {
                        [userId]: {error: null, isLoading: false}
                    })
                });
            })
            .catch((err) => {
                this.setState({
                    status: Object.assign({}, this.state.status, {
                        [userId]: {error: err.error, isLoading: false}
                    })
                });
            });
    }

    deleteSubscription(e) {
        const el = e.currentTarget;
        const userId = el.dataset.user;
        const serviceId = el.dataset.service;
        const subId = el.dataset.sub;

        this.setState({
            status: Object.assign({}, this.state.status, {
                [userId]: {isLoading: true}
            })
        });

        this.props.fetchDeleteSubscription(this.props.instance.id, {id: subId, user_id: userId, service_id: serviceId})
            .then(() => {
                this.setState({
                    status: Object.assign({}, this.state.status, {
                        [userId]: {error: null, isLoading: false}
                    })
                });
            })
            .catch((err) => {
                this.setState({
                    status: Object.assign({}, this.state.status, {
                        [userId]: {error: err.error, isLoading: false}
                    })
                });
            });
    }

    searchSubForUser(user, service) {
        if (!service.subscriptions) {
            return null;
        }

        return service.subscriptions.find((sub) => {
            return sub.user_id === user.id;
        });
    }

    render() {
        const isAdmin = this.props.isAdmin;
        const instance = this.props.instance;
        const isRunning = instance.applicationInstance.status === instanceStatus.running;
        const isAvailable = isAdmin && !instance.isPublic && isRunning;

        const membersWithoutAccess = this.props.members.filter(this.filterMemberWithoutAccess);
        const Header = <InstanceDropdownHeader
            isAdmin={isAdmin}
            instance={instance}
            onClickConfigIcon={this.onClickConfigIcon}
            onRemoveInstance={this.onRemoveInstance}
            onCancelRemoveInstance={this.onCancelRemoveInstance}/>;
        const Footer = (isAvailable && <footer>
            <InstanceInvitationForm members={membersWithoutAccess} instance={instance}/>
        </footer>) || null;

        return <DropDownMenu header={Header} footer={Footer} isAvailable={isAvailable}>
            <section className='dropdown-content'>
                <table className="oz-table">
                    <thead>
                    {/*
                            Header: size: 3+ n (services)
                            user's name; error message; n services; options
                        */}
                    <tr>
                        <th className="fill-content" colSpan={2}/>
                        {
                            instance.services.map((service) => {
                                return <th key={service.catalogEntry.id} className="center">
                                        {
                                            instance.services.length > 1 &&
                                            <span className="service" title={service.name}>{service.name.toAcronyme()}</span>
                                        }
                                </th>
                            })
                        }
                        <th/>
                    </tr>
                    </thead>
                    <tbody>
                    {
                        instance.users && instance.users.map((user, i) => {
                            const status = this.state.status[user.id];
                            return <tr key={user.id || user.email}>
                                <td className="fill-content">
                                    <article className="item flex-row">
                                        {
                                            user.id &&
                                            <span className="name">{user.name}</span>
                                        }
                                        {
                                            user.email &&
                                            <span className={`email ${(user.id && 'separator') || ''}`}>{user.email}</span>
                                        }
                                    </article>
                                </td>

                                {/* error messages */}
                                <td className="fill-content">
                                    {
                                        status && status.error &&
                                        <span className="error">{status.error}</span>
                                    }
                                </td>


                                {/* Services */}
                                {
                                    user.id &&
                                    instance.services.map((service) => {
                                        const sub = this.searchSubForUser(user, service);
                                        return <td key={service.catalogEntry.id} className="fill-content center">
                                            {
                                                 !sub &&
                                                <CustomTooltip title={this.context.t('tooltip.add.icon')}>
                                                    <button className="btn icon" onClick={this.createSubscription}
                                                            disabled={status && status.isLoading}
                                                            data-user={user.id} data-service={service.catalogEntry.id}>
                                                        <i className="fas fa-times option-icon service"/>
                                                    </button>
                                                </CustomTooltip>
                                            }

                                            {
                                                sub &&
                                                <CustomTooltip title={this.context.t('tooltip.remove.icon')}>
                                                    <button className="btn icon" onClick={this.deleteSubscription}
                                                            disabled={status && status.isLoading}
                                                            data-sub={sub.id}
                                                            data-user={user.id} data-service={service.catalogEntry.id}>
                                                        <i className="fas fa-home option-icon service"/>
                                                    </button>
                                                </CustomTooltip>
                                            }
                                        </td>
                                    })
                                }


                                {/* Options */}
                                {
                                    !user.id &&
                                    <React.Fragment>
                                        {/* empty space to replace services */}
                                        {
                                            (instance.services.length - 1) > 0 &&
                                            <td className="fill-content center empty" colSpan={instance.services.length - 1} />
                                        }

                                        <td className="fill-content center">
                                            <CustomTooltip title={this.context.t('tooltip.pending')}>
                                                <i className="fa fa-stopwatch option-icon loading"/>
                                            </CustomTooltip>
                                        </td>
                                    </React.Fragment>
                                }

                                <td className="fill-content center">
                                    <CustomTooltip title={this.context.t('tooltip.remove.member')}>
                                        <button className="btn icon" data-member={i}
                                                onClick={this.removeUserAccessToInstance}>
                                            <i className="fa fa-trash option-icon delete"/>
                                        </button>
                                    </CustomTooltip>
                                </td>
                            </tr>
                        })
                    }
                    </tbody>
                </table>
            </section>
        </DropDownMenu>;
    }
}

const mapDispatchToProps = dispatch => {
    return {
        fetchDeleteAcl(user, instance) {
            return dispatch(fetchDeleteAcl(user, instance));
        },
        fetchUpdateInstanceStatus(instance, status) {
            return dispatch(fetchUpdateInstanceStatus(instance, status));
        },
        fetchUpdateServiceConfig(instanceId, catalogEntry) {
            return dispatch(fetchUpdateServiceConfig(instanceId, catalogEntry));
        },
        fetchCreateSubscription(instanceId, sub) {
            return dispatch(fetchCreateSubscription(instanceId, sub));
        },
        fetchDeleteSubscription(instanceId, sub) {
            return dispatch(fetchDeleteSubscription(instanceId, sub));
        },
    };
};

export default connect(null, mapDispatchToProps)(InstanceDropdown);
