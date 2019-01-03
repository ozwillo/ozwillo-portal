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
import {fetchUpdateInstanceStatus} from '../../../actions/instance';

//Config
import Config from '../../../config/config';

const instanceStatus = Config.instanceStatus;

//Action
import {fetchUpdateServiceConfig} from '../../../actions/instance';
import InstanceService from "../../../util/instance-service";


import { i18n } from "../../../app.js"
import { t } from "@lingui/macro"
import { CSSTransition, TransitionGroup} from 'react-transition-group';


class InstanceDropdown extends React.Component {

    static propTypes = {
        instance: PropTypes.object.isRequired,
        organizationMembers: PropTypes.array,
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

        this._instanceService = new InstanceService();


    }


    componentWillReceiveProps(nextProps) {
        this.setState({
            instance: nextProps.instance,
            organizationMembers: nextProps.organizationMembers
        });

        if (nextProps.organizationMembers) {
            this.setState({isLoading: false})
        }

    }


    fetchUpdateServiceConfig = (instanceId, catalogEntry) => {
        return this.props.fetchUpdateServiceConfig(instanceId, catalogEntry)
            .then(() => {
                Popup.close();
            });
    };

    onClickConfigIcon = (instance) => {
        Popup.create({
            title: instance.name,
            content: <InstanceConfigForm instance={instance} onSubmit={this.fetchUpdateServiceConfig}/>
        }, true);
    };

    onRemoveInstance = (instance) => {
        this.props.fetchUpdateInstanceStatus(instance, instanceStatus.stopped);
    };

    onCancelRemoveInstance = (instance) => {
        this.props.fetchUpdateInstanceStatus(instance, instanceStatus.running);
    };

    filterMemberWithoutAccess = (member) => {
        const {members} = this.state;
        if (!members) {
            return true;
        }

        return !members.find((user) => {
            return user.id === member.id;
        })
    };


    createUserAccessToInstance = async (user) => {
        const {members, services} = this.state;
        try {
            await this._instanceService.fetchCreateAcl(user, this.props.instance);
            members.push(user);
            this.setState({members});
            services.map(async (service) => {
                await this._createSubscription(service.catalogEntry.id, user.id);
            });
            await this._refreshServices();
            return {error: false}
        } catch (e) {
            return {error: true, message: e};
        }
    };

    removeUserAccessToInstance = (e) => {
        let {members} = this.state;
        const i = e.currentTarget.dataset.member;
        const member = members[i];

        this._instanceService.fetchDeleteAcl(member, this.props.instance)
            .then(() => {
                members.splice(members.indexOf(member.id), 1);
                this.setState({
                    status: Object.assign({}, this.state.status, {
                        [member.id]: {error: null}
                    }, members)
                });
            })
            .catch((err) => {
                this.setState({
                    status: Object.assign({}, this.state.status, {
                        [member.id]: {error: err.error}
                    })
                });
            });
    };

    _createSubscription = async (serviceId, userId) => {
        this.setState({
            status: {[userId]: {isLoading: true}}
        });
        return await this.fetchCreateSubscription(serviceId, userId);
    };

    createSubscriptionFromEvent = async (e) => {
        const el = e.currentTarget;
        const userId = el.dataset.user;
        const serviceId = el.dataset.service;
        try {
            await this._createSubscription(serviceId, userId);
            await this._refreshServices();
        }catch(e){
            this.setState({
                status: Object.assign({}, this.state.status, {
                    [userId]: {error: e.error, isLoading: false}
                })
            });
        }
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
        await this._refreshServices();
    };


    fetchCreateSubscription = async (serviceId, userId) => {
        this._instanceService.fetchCreateSubscription(serviceId, userId).then(() => {
            this.setState({
                status: Object.assign({}, this.state.status, {
                    [userId]: {error: null, isLoading: false}
                })
            });
        })
    };

    fetchDeleteSubscription = async (serviceId, userId) => {
        this._instanceService.fetchDeleteSubscription(serviceId, userId).then(() => {
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


    searchSubForUser = (user, service) => {
        if (!service.subscriptions) {
            return null;
        }

        return service.subscriptions.find((sub) => {
            return sub.user_id === user.id;
        });
    };

    _refreshServices = async () => {
        const newServices = await this._instanceService.fetchInstanceServices(this.props.instance.id, true);
        this.setState({services: newServices})
    };


    handleDropDown = async (dropDownState) => {
        if (dropDownState) {
            //Fetch users for the instance
            if (this.props.isAdmin) {
                this.setState({isLoading: true});
                const newServices = await this._instanceService.fetchInstanceServices(this.props.instance.id, true);
                const members = await this._instanceService.fetchUsersOfInstance(this.props.instance.id);
                this.setState({services: newServices, isLoading: false, members: members});
            }
        }

    };

    render() {
        const isAdmin = this.props.isAdmin;
        const instance = this.props.instance;
        const {services, isLoading, members} = this.state;
        const {organizationMembers} = this.props;
        const isRunning = instance.applicationInstance.status === instanceStatus.running;
        const isAvailable = isAdmin && !instance.isPublic && isRunning;
        const isOpen = false;

        const membersWithoutAccess = organizationMembers ? organizationMembers.filter(this.filterMemberWithoutAccess) : null;
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
                <table className="table table-striped">
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
                        <th/>
                    </tr>
                    </thead>
                    <tbody>
                    <TransitionGroup component={null}>
                        {

                            members && members.map((user, i) => {
                                const status = this.state.status[user.id];
                                return  <CSSTransition
                                    timeout={50 * (i+1)}
                                    key={user.id || user.email}
                                    classNames={"fade"}
                                >
                                    <tr>
                                        <td className="fill-content">
                                            <article className="item">
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
                                                return <td key={service.catalogEntry.id} className="fill-content col-md-1">
                                                    {
                                                        !sub &&
                                                        <CustomTooltip title={i18n._(t`tooltip.add.icon`)}>
                                                            <button className="btn icon"
                                                                    onClick={this.createSubscriptionFromEvent}
                                                                    disabled={status && status.isLoading}
                                                                    data-user={user.id} data-service={service.catalogEntry.id}>
                                                                <i className="fas fa-plus option-icon service"/>
                                                            </button>
                                                        </CustomTooltip>
                                                    }

                                                    {
                                                        sub &&
                                                        <CustomTooltip title={i18n._(t`tooltip.remove.icon`)}>
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

                                                <td className="fill-content col-md-1">
                                                    <CustomTooltip title={i18n._(t`tooltip.pending`)}>
                                                        <button type="button" className="btn icon">
                                                            <i className="fa fa-stopwatch option-icon loading"/>
                                                        </button>
                                                    </CustomTooltip>
                                                </td>
                                            </React.Fragment>
                                        }

                                        <td className="fill-content col-md-1">
                                            <CustomTooltip title={i18n._(t`tooltip.remove.member`)}>
                                                <button className="btn icon delete" data-member={i}
                                                        onClick={this.removeUserAccessToInstance}>
                                                    <i className="fa fa-trash option-icon delete"/>
                                                </button>
                                            </CustomTooltip>
                                        </td>
                                    </tr>
                                </CSSTransition>
                            })
                        }
                    </TransitionGroup>
                    </tbody>
                </table>
                <InstanceInvitationForm members={membersWithoutAccess} instance={instance}
                                        sendInvitation={this.createUserAccessToInstance}/>
            </section>
        </DropDownMenu>;
    }
}

const mapDispatchToProps = dispatch => {
    return {
        fetchUpdateInstanceStatus(instance, status) {
            return dispatch(fetchUpdateInstanceStatus(instance, status));
        },
        fetchUpdateServiceConfig(instanceId, catalogEntry) {
            return dispatch(fetchUpdateServiceConfig(instanceId, catalogEntry));
        }
    };
};

export default connect(null, mapDispatchToProps)(InstanceDropdown);
