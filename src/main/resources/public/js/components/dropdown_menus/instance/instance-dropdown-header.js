import React from 'react';
import PropTypes from 'prop-types';

//Config
import Config from '../../../config/config';
const instanceStatus = Config.instanceStatus;

const TIME_DAY = 1000 * 3600 * 24; // millisecondes

class InstanceDropdownHeader extends React.Component {

    static propTypes = {
        instance: PropTypes.object.isRequired,
        onRemoveInstance: PropTypes.func.isRequired,
        onCancelRemoveInstance: PropTypes.func.isRequired,
        onClickConfigIcon: PropTypes.func.isRequired,
        isAdmin: PropTypes.bool
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

    onCancelRemoveInstance(e) {
        e.preventDefault();
        this.props.onCancelRemoveInstance(this.props.instance);
    }

    onClickConfigIcon() {
        this.props.onClickConfigIcon(this.props.instance);
    }

    get numberOfDaysBeforeDeletion() {
        const now = Date.now();
        const deletionDate = new Date(this.props.instance.deletion_planned).getTime();

        const days = Math.round((deletionDate - now ) / TIME_DAY);

        return days;
    }

    render() {
        const isAdmin = this.props.isAdmin;
        const instance = this.props.instance;
        const isPending = instance.applicationInstance.status === instanceStatus.pending;
        const isStopped = instance.applicationInstance.status === instanceStatus.stopped;
        const isRunning = instance.applicationInstance.status === instanceStatus.running;

        return <header className="dropdown-header">
            <form className="form flex-row"
                  onSubmit={(isRunning && this.onRemoveInstance) ||
                            (isStopped && this.onCancelRemoveInstance) || null}>
                <span className="dropdown-name">{instance.name}</span>
                {
                    !isStopped && !isPending && isAdmin &&
                    <div className="options flex-row end">
                        <button type="button" className="btn icon" onClick={this.onClickConfigIcon}>
                            <i className="fa fa-cog option-icon"/>
                        </button>
                        <button type="submit" className="btn icon">
                            <i className="fa fa-trash option-icon"/>
                        </button>
                    </div>
                }

                {
                    isPending &&
                    <div className="options flex-row end">
                        <i className="fa fa-spinner fa-spin option-icon loading"/>
                    </div>
                }

                {
                    isStopped &&
                    <div className="options flex-row end">
                        <span className="message delete">Will be deleted in {this.numberOfDaysBeforeDeletion} days</span>
                        <button type="submit" className="btn oz-btn-danger">Cancel</button>
                    </div>
                }
            </form>
        </header>;
    }
}

export default InstanceDropdownHeader;