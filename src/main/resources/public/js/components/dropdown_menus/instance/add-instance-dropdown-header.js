import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';
import { DropdownBlockError } from '../../notification-messages';
import { i18n } from "../../../app.js"

class AddInstanceDropdownHeader extends React.Component {
    

    static propTypes = {
        apps: PropTypes.array.isRequired,
        app: PropTypes.object,
        onAddInstance: PropTypes.func.isRequired,
        onChangeInstance: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            error: {message: '', http_status: 200},
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

        this.setState({error: '',isLoading: true});

        this.props.onAddInstance()
            .then(() => this.setState({error: '', isLoading: false}))
            .catch(err => {
                console.error(err);
                let message = '';
                if(err.status.toString().startsWith('4')) {
                    message = i18n._('could-not-install-app-400')
                }else if(err.status.toString().startsWith('5')){
                    message = i18n._('could-not-install-app-500')
                }else{
                    message = i18n._('ui.error')
                }

                this.setState({error: {message: message, http_status: err.status}, isLoading: false});
            });
    }

    render() {
        let {error} = this.state;
        return <header className="dropdown-header">
            <form className="form flex-row" onSubmit={this.onSubmit}>
                <Select
                    className="select add-instance-select"
                    name="app"
                    value={this.props.app}
                    labelKey="name"
                    onChange={this.onOptionChange}
                    options={this.props.apps}
                    placeholder={i18n._('organization.desc.applications')}
                    required={true}/>

                <div className="options flex-row">
                    {
                        !this.state.isLoading ?
                        <button type="submit" className="btn btn-submit" disabled={this.state.isLoading}>
                            {i18n._('store.install')}
                        </button> : null
                    }

                    {
                        this.state.isLoading &&
                        <i className="fa fa-spinner fa-spin option-icon" style={{ 'marginLeft': '1em' }}/>
                    }
                </div>

            </form>
            {
                error.message &&
                    <DropdownBlockError errorMessage={error.message + ' ('+ i18n._('error-code')+' : ' + error.http_status+')'}/>
            }

        </header>;
    }
}

export default AddInstanceDropdownHeader;