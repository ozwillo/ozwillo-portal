import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';

import { DropdownBlockError, DropdownBlockSuccess } from '../../notification-messages';
import { i18n } from "../../../config/i18n-config"
import { t } from "@lingui/macro"

class MemberDropdownFooter extends React.Component {

    static propTypes = {
        instances: PropTypes.array.isRequired,
        onAddAccessToInstance: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            selectedOption: null,
            error: '',
            isLoading: false
        };

        //binds methods
        this.onSubmit = this.onSubmit.bind(this);
        this.onOptionChange = this.onOptionChange.bind(this);
    }

    componentWillReceiveProps(nextProps){
        if(nextProps.instances){
            this.setState({selectedOption: null})
        }
    }

    onSubmit(e) {
        e.preventDefault();

        this.setState({isLoading: true});
        this.props.onAddAccessToInstance(this.state.selectedOption)
            .then(() => {
                this.setState({ error: '', isLoading: false });
            })
            .catch(err => {
                this.setState({ error: err.error, isLoading: false });
            });
    }

    onOptionChange(selectedOption) {
        this.setState({selectedOption});
    }

    render() {
        return <header className="dropdown-footer">
            <form className="form flex-row" onSubmit={this.onSubmit}>
                <Select
                    className="select add-instance-member-select"
                    name="app"
                    value={this.state.selectedOption}
                    onChange={this.onOptionChange}
                    options={this.props.instances}
                    clearable={false}
                    valueKey="id"
                    labelKey="name"
                    placeholder="Instances"/>

                <div className="flex-row end">
                    <button type="submit" className="btn btn-submit">
                        {
                            !this.state.isLoading && i18n._(t`ui.add`)
                        }
                        {
                            this.state.isLoading &&
                                <i className="fa fa-spinner fa-spin action-icon" style={{ 'marginLeft': '1em' }}/>
                        }
                    </button>
                </div>
            </form>

            {
                this.state.error && <DropdownBlockError errorMessage={this.state.error} />
            }
        </header>;
    }

}


export default MemberDropdownFooter;
