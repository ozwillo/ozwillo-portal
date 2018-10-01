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
import {fetchUpdateInstanceStatus} from '../../../actions/instance';

//Config
import Config from '../../../config/config';

//Service
import {
    fetchCreateSubscription,
    fetchDeleteSubscription,
    fetchInstanceServicesSubscriptions,
    fetchUsersOfInstance
} from "../../../util/instance-service";

const instanceStatus = Config.instanceStatus;

//Action
import {fetchUpdateServiceConfig} from '../../../actions/instance';
import customFetch from '../../../util/custom-fetch';

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
            status: {},
            isLoading: false,
            services: [],
            members: null
        };

        //bind methods
        this.onClickConfigIcon = this.onClickConfigIcon.bind(this);
        this.onRemoveInstance = this.onRemoveInstance.bind(this);
        this.onCancelRemoveInstance = this.onCancelRemoveInstance.bind(this);
        this.filterMemberWithoutAccess = this.filterMemberWithoutAccess.bind(this);
        this.removeUserAccessToInstance = this.removeUserAccessToInstance.bind(this);
        this.fetchUpdateServiceConfig = this.fetchUpdateServiceConfig.bind(this);
        this.searchSubForUser = this.searchSubForUser.bind(this);
    }


    componentWillReceiveProps(nextProps) {
        this.setState({
            instance: nextProps.instance,
            members: nextProps.members
        });

        if (nextProps.members) {
            this.setState({isLoading: false})
        }

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
        const {members} = this.state;
        if (!members) {
            return true;
        }

        return !members.find((user) => {
            return user.id === member.id;
        })
    }

    removeUserAccessToInstance(e) {
        const {members} = this.state;
        const i = e.currentTarget.dataset.member;
        const member = members[i];

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


    createSubscription = async (e) => {
        const el = e.currentTarget;
        const userId = el.dataset.user;
        const serviceId = el.dataset.service;

        this.setState({
            status: Object.assign({}, this.state.status, {
                [userId]: {isLoading: true}
            })
        });

        await this.fetchCreateSubscription(serviceId, userId);
        const newServices = await fetchInstanceServicesSubscriptions(this.props.instance.id);
        this.setState({services: newServices})

    };

    deleteSubscription = async (e) => {
        const el = e.currentTarget;
        const userId = el.dataset.user;
        const serviceId = el.dataset.service;

        this.setState({
            status: Object.assign({}, this.state.status, {
                [userId]: {isLoading: true}
            })
        });

        await this.fetchDeleteSubscription(serviceId, userId);
        const newServices = await fetchInstanceServicesSubscriptions(this.props.instance.id);
        this.setState({services: newServices});
    };



    fetchCreateSubscription = async (serviceId, userId) => {
        fetchCreateSubscription(serviceId,userId).then(() => {
            this.setState({
                status: Object.assign({}, this.state.status, {
                    [userId]: {error: null, isLoading: false}
                })
            });
        }).catch((err) => {
            this.setState({
                status: Object.assign({}, this.state.status, {
                    [userId]: {error: err.error, isLoading: false}
                })
            });
        });
    };

    fetchDeleteSubscription = async (serviceId, userId) => {
        fetchDeleteSubscription(serviceId,userId).then(() => {
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
    };


    searchSubForUser(user, service) {
        if (!service.subscriptions) {
            return null;
        }

        return service.subscriptions.find((sub) => {
            return sub.user_id === user.id;
        });
    }

    handleDropDown = async (dropDownState) => {
        if (dropDownState) {
            //Fetch users for the instance
            if (this.props.isAdmin) {
                this.setState({isLoading: true});
                const newServices = await fetchInstanceServicesSubscriptions(this.props.instance.id);
                const members = await fetchUsersOfInstance(this.props.instance.id);
                this.setState({services: newServices, isLoading: false, members: members});
            }
        }

    };

    render() {
        const isAdmin = this.props.isAdmin;
        const instance = this.props.instance;
        const {services, isLoading, members} = this.state;
        const isRunning = instance.applicationInstance.status === instanceStatus.running;
        const isAvailable = isAdmin && !instance.isPublic && isRunning;
        const isOpen = false;

        const membersWithoutAccess = this.props.members.filter(this.filterMemberWithoutAccess);
        const Header = <InstanceDropdownHeader
            isAdmin={isAdmin}
            instance={instance}
            onClickConfigIcon={this.onClickConfigIcon}
            onRemoveInstance={this.onRemoveInstance}
            onCancelRemoveInstance={this.onCancelRemoveInstance}/>;
        const usersIconDropDown = <i className="fa fa-users"/>;

        return <DropDownMenu header={Header} isAvailable={isAvailable} isOpen={isOpen}
                             dropDownIcon={usersIconDropDown}
                             dropDownChange={this.handleDropDown}>
            <section className='dropdown-content'>
                {
                    !members && isLoading &&
                    <div className="container-loading text-center">
                        <i className="fa fa-spinner fa-spin loading"/>
                    </div>
                }
                <table className="oz-table">
                    <thead>
                    {/*
                            Header: size: 3+ n (services)
                            user's name; error message; n services; options
                        */}
                    <tr>
                        <th className="fill-content" colSpan={1}/>
                        {
                            services && services.length > 0 ?
                                services.map((service) => {
                                    return <th key={service.catalogEntry.id} className="center">
                                        {
                                            services.length > 1 &&
                                            <span className="service"
                                                  title={service.name}>{service.name.toAcronyme()}</span>
                                        }
                                    </th>
                                })
                                : null
                        }
                        {
                            status && status.error &&
                            <th/>
                        }
                    </tr>
                    </thead>
                    <tbody>
                    {

                        members && members.map((user, i) => {
                            const status = this.state.status[user.id];
                            return <tr key={user.id || user.email}>
                                <td className="fill-content">
                                    <article className="item flex-row-mobile-column">
                                        {
                                            user.id &&
                                            <span className="name">{user.name}</span>
                                        }
                                        {
                                            user.email &&
                                            <span
                                                className={`email ${(user.id && 'separator') || ''}`}>{user.email}</span>
                                        }
                                    </article>
                                </td>

                                {/* error messages */}
                                {
                                    status && status.error &&
                                    <td className="fill-content">
                                        <span className="error">{status.error}</span>
                                    </td>
                                }


                                {/* Services */}
                                {
                                    user.id && services &&
                                    services.map((service) => {
                                        const sub = this.searchSubForUser(user, service);
                                        return <td key={service.catalogEntry.id} className="fill-content center">
                                            {
                                                !sub &&
                                                <CustomTooltip title={this.context.t('tooltip.add.icon')}>
                                                    <button className="btn icon" onClick={this.createSubscription}
                                                            disabled={status && status.isLoading}
                                                            data-user={user.id} data-service={service.catalogEntry.id}>
                                                        <i className="fas fa-plus option-icon service"/>
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
                                    !user.id && services &&
                                    <React.Fragment>
                                        {/* empty space to replace services */}
                                        {
                                            (services.length - 1) > 0 &&
                                            <td className="fill-content center empty"
                                                colSpan={services.length - 1}/>
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
                <InstanceInvitationForm members={membersWithoutAccess} instance={instance}/>
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
        }

    };
};

export default connect(null, mapDispatchToProps)(InstanceDropdown);
