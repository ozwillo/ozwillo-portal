import React from 'react';
import PropTypes from 'prop-types';

class InstanceDropdownHeader extends React.Component {

    static propTypes = {
        instance: PropTypes.object.isRequired,
        onRemoveInstance: PropTypes.func.isRequired,
        onUpdateInstance: PropTypes.func.isRequired,
        onClickConfigIcon: PropTypes.func.isRequired,
        isAdmin: PropTypes.bool
    };

    static defaultProps = {
        isAdmin: false
    };

    constructor(props) {
        super(props);

        this.onCheckboxChange = this.onCheckboxChange.bind(this);
        this.onClickConfigIcon = this.onClickConfigIcon.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
    }

    onCheckboxChange() {
        this.props.onUpdateInstance({ isPublic: !this.props.instance.isPublic });
    }

    onSubmit(e) {
        e.preventDefault();
        this.props.onRemoveInstance(this.props.instance);
    }

    onClickConfigIcon() {
        this.props.onClickConfigIcon(this.props.instance);
    }

    render() {
        const isAdmin = this.props.isAdmin;
        const instance = this.props.instance;
        const isPending = instance.applicationInstance.status === 'PENDING';

        return <header className="dropdown-header">
            <form className="form flex-row" onSubmit={this.onSubmit}>
                <span className="dropdown-name">{instance.name}</span>
                {
                    !isPending && isAdmin &&
                    <div className="options flex-row end">
                        <label>
                            Public
                            <input className="field" name="isPublic" type="checkbox"
                                   checked={instance.isPublic} onChange={this.onCheckboxChange}/>
                        </label>
                        <button type="button" className="btn icon" onClick={this.onClickConfigIcon}>
                            <i className="fa fa-cog option-icon"/>
                        </button>
                        <button type="submit" className="btn icon">
                            <i className="fa fa-trash option-icon delete"/>
                        </button>
                    </div>
                }

                {
                    isPending &&
                    <div className="options flex-row end">
                        <i className="fa fa-spinner fa-spin option-icon"/>
                    </div>
                }
            </form>
        </header>;
    }
}

export default InstanceDropdownHeader;