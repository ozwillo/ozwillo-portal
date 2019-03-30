import React from 'react';
import PropTypes from 'prop-types';

//Components
import CustomTooltip from '../../custom-tooltip';


//Config
import Config from '../../../config/config';
import {fetchServicesOfInstance} from "../../../actions/instance";
import customFetch from '../../../util/custom-fetch';

import { i18n } from "../../../config/i18n-config"
import {plural} from '@lingui/macro'
import { t } from "@lingui/macro"
import NotificationMessageBlock from '../../notification-message-block';
import moment from 'moment';

const instanceStatus = Config.instanceStatus;

class InstanceDropdownHeader extends React.Component {

    static propTypes = {
        instance: PropTypes.object.isRequired,
        onRemoveInstance: PropTypes.func.isRequired,
        onCancelRemoveInstance: PropTypes.func.isRequired,
        onClickConfigIcon: PropTypes.func.isRequired,
        isAdmin: PropTypes.bool,
    };

    state = {
        error: {status : false, http_status : 200, message: ''}
    };

    static defaultProps = {
        isAdmin: false
    };

    constructor(props) {
        super(props);

        this.onClickConfigIcon = this.onClickConfigIcon.bind(this);
        this.onRemoveInstance = this.onRemoveInstance.bind(this);
        this.onCancelRemoveInstance = this.onCancelRemoveInstance.bind(this);
    }

    onRemoveInstance(e) {
        e.preventDefault();
        this.props.onRemoveInstance(this.props.instance);
    }

    onRemovePendingInstance = (e) => {
        e.preventDefault();
        customFetch(`/my/api/instance/${this.props.instance.id}`, {
            method: 'DELETE',
        }).then(() => {
            //TODO check if the complete pipe is correct
            this.props.onRemoveInstance(this.props.instance);
        }).catch(err => {
            let message = '';
            if(err.status !== 409){
                message = 'ui.error';
            }else{
                message = 'error.msg.delete-pending-instance';
            }
            const error = {status: true ,http_status: err.status, message: message};
            this.setState({error: error});
        });
    };

    onCancelRemoveInstance(e) {
        e.preventDefault();
        this.props.onCancelRemoveInstance(this.props.instance);
    }

    onClickConfigIcon() {
        fetchServicesOfInstance(this.props.instance.applicationInstance.id)
            .then((services) => {
                this.props.instance.services = services;
                this.props.onClickConfigIcon(this.props.instance)
            });
    }

    get numberOfDaysBeforeDeletion() {
        const now = moment();
        const deletionDate = moment(this.props.instance.deletion_planned);
        const days = Math.round(deletionDate.diff(now, 'days', true));

        return i18n._(plural({
          value: days,
          one: `Will be deleted`,
          other: `Will be deleted in ${days} days`
        }));
    }

    render() {
        const {error} = this.state;
        const defaultError = {status: false, http_status: 200};
        const isAdmin = this.props.isAdmin;
        const instance = this.props.instance;
        const isPending = instance.applicationInstance.status === instanceStatus.pending;
        const isStopped = instance.applicationInstance.status === instanceStatus.stopped;
        const isRunning = instance.applicationInstance.status === instanceStatus.running;

        return <header className="dropdown-header">
            <form className="form flex-row"
                  onSubmit={(isRunning && this.onRemoveInstance) ||
                  (isStopped && this.onCancelRemoveInstance) || (isPending && this.onRemovePendingInstance)}>
                <span className="dropdown-name">{instance.name}</span>

                <div className="options flex-row end">

                    {
                        !isStopped && !isPending && isAdmin &&
                        <CustomTooltip title={i18n._(t`tooltip.config`)}>
                            <button type="button" className="btn icon" onClick={this.onClickConfigIcon}>
                                <i className="fa fa-cog option-icon"/>
                            </button>
                        </CustomTooltip>
                    }

                    {
                        isPending &&
                        <CustomTooltip title={i18n._(t`tooltip.pending`)}>
                            <button type="button" className="btn icon">
                                <i className="fa fa-stopwatch option-icon loading"/>
                            </button>
                        </CustomTooltip>
                    }

                    <CustomTooltip title={i18n._(t`tooltip.remove.instance`)}
                                   className={`${(isStopped || !isAdmin) ? 'invisible' : ''}`}>
                        <button type="submit" className="btn icon delete">
                            <i className="fa fa-trash option-icon delete"/>
                        </button>
                    </CustomTooltip>

                    {
                        isStopped &&
                        [

                            <span key={`${instance.id}-message`} className="message delete">
                                {this.numberOfDaysBeforeDeletion}
                            </span>,
                            <button key={`${instance.id}-submit`} type="submit" className="btn btn-default-inverse">
                                {i18n._(t`ui.cancel`)}
                            </button>
                        ]
                    }
                </div>
            </form>

            <NotificationMessageBlock type={'danger'}
                                      message={i18n._(`${error.message}`) + ' (' + i18n._(t`error-code`) + ' : ' + error.http_status + ')'}
                                      display={error.status} close={() => this.setState({error: defaultError})}/>

        </header>;
    }
}

export default InstanceDropdownHeader;
