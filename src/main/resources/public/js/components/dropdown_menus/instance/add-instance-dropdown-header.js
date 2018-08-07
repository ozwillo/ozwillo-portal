import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';

class AddInstanceDropdownHeader extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    static propTypes = {
        apps: PropTypes.array.isRequired,
        app: PropTypes.object,
        onAddInstance: PropTypes.func.isRequired,
        onChangeInstance: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            error: '',
            isLoading: false
        };

        //bind methods
        this.onOptionChange = this.onOptionChange.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
    }

    onOptionChange(selectedOption) {
        this.props.onChangeInstance(selectedOption)
    }

    onSubmit(e) {
        e.preventDefault();

        this.setState({isLoading: true});

        this.props.onAddInstance()
            .then(() => this.setState({error: '', isLoading: false}))
            .catch(err => {
                console.error(err);
                let message = '';
                if(err.status.toString().startsWith('4')) {
                    message = this.context.t('could-not-install-app-400')
                }else if(err.status.toString().startsWith('5')){
                    message = this.context.t('could-not-install-app-500')
                }else{
                    message = this.context.t('ui.error')
                }

                this.setState({error: message, isLoading: false});
            });
    }

    render() {
        return <header className="dropdown-header">
            <form className="form flex-row" onSubmit={this.onSubmit}>
                <Select
                    className="select"
                    name="app"
                    value={this.props.app}
                    labelKey="name"
                    onChange={this.onOptionChange}
                    options={this.props.apps}
                    placeholder={this.context.t('organization.desc.applications')}
                    required={true}/>

                {
                    this.state.error &&
                    <span className="error-message">{this.state.error}</span>
                }

                <div className="options flex-row end">
                    {
                        !this.state.isLoading ?
                        < button type="submit" className="btn btn-submit icon" disabled={this.state.isLoading}>
                            {this.context.t('ui.send')}
                        </button> : null
                    }

                    {
                        this.state.isLoading &&
                        <i className="fa fa-spinner fa-spin option-icon"/>
                    }
                </div>
            </form>
        </header>;
    }
}

export default AddInstanceDropdownHeader;