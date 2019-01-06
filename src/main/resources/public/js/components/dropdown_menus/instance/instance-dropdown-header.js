import React from 'react';
import PropTypes from 'prop-types';

//Components
import CustomTooltip from '../../custom-tooltip';


//Config
import Config from '../../../config/config';
import {fetchServicesOfInstance} from "../../../actions/instance";
import customFetch from '../../../util/custom-fetch';

import { i18n } from "../../../config/i18n-config"
import { t } from "@lingui/macro"

const instanceStatus = Config.instanceStatus;

const TIME_DAY = 1000 * 3600 * 24; // millisecondes

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
        const now = Date.now();
        const deletionDate = new Date(this.props.instance.deletion_planned).getTime();

        const days = Math.round((deletionDate - now) / TIME_DAY);

        return (days > 0) ? i18n._(t`ui.message.will-be-deleted-plural`).format(days) :
            i18n._(t`ui.message.will-be-deleted`);
    }

    _displayError = () => {
        const {status, http_status, message} = this.state.error;
        const defaultError = {status: false, http_status: 200};

        if (status) {
            return (
                <div className="alert alert-danger" role="alert"
                     style={{marginBottom: 0, alignItems: 'center'}}>
                    <strong>{i18n._(t`sorry`)}</strong>
                    &nbsp;
                    {i18n._(t`${message}`) + ' (' + i18n._(t`error-code`) + ' : ' + http_status + ')'}
                    &nbsp;
                    <button type="button" className="close" data-dismiss="alert"
                            onClick={() => this.setState({error: defaultError})}>
                        <span aria-hidden="true">&times;</span>
                        <span className="sr-only">{i18n._(t`ui.close`)}</span>
                    </button>
                </div>
            )
        }
    };

    render() {
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

            {this._displayError()}

        </header>;
    }
}

export default InstanceDropdownHeader;
