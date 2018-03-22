import React from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import Select from 'react-select';


//Action
import {fetchCreateAcl} from "../../actions/acl";

class InstanceInvitationForm extends React.Component {
    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    static propTypes = {
        instance: PropTypes.object.isRequired,
        members: PropTypes.array.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            selectedOption: null,
            email: '',
            isLoading: false,
            error: '',
            success: ''
        };

        //bind methods
        this.onOptionChange = this.onOptionChange.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
    }

    onOptionChange(selectedOption) {
        this.setState({selectedOption: selectedOption})
    }

    handleChange(e) {
        const el = e.currentTarget;
        this.setState({
            [el.name]: el.type === 'checkbox' ? el.checked : el.value
        });
    }

    onSubmit(e) {
        e.preventDefault();

        this.setState({isLoading: true});

        const user = this.state.selectedOption || {email: this.state.email};
        this.props.fetchCreateAcl(user, this.props.instance)
            .then(() => {
                this.setState({
                    isLoading: false,
                    selectedOption: null,
                    email: '',
                    success: this.context.t('ui.request.send'),
                    error: ''
                });
            })
            .catch((err) => {
                console.error(err);
                this.setState({
                    isLoading: false,
                    success: '',
                    error: err.error
                });
            });
    }

    render() {
        return <form className={`instance-invitation-form flex-col end ${this.props.className || ''}`}
                     onSubmit={this.onSubmit}>
            <div className="content flex-row">
                <Select
                    className="select"
                    name="members"
                    value={this.state.selectedOption}
                    labelKey="name"
                    valueKey="id"
                    onChange={this.onOptionChange}
                    options={this.props.members}
                    placeholder={this.context.t('organization.desc.members')}
                    required={!this.state.email}/>

                <em className="sep-text">or</em>

                <div className="new-user-fieldset flex-row">
                    <label className="label">
                        {this.context.t('organization.form.email')}
                        <input name="email" type="email" className="form-control field"
                               required={!this.state.selectedOption}
                               value={this.state.email} onChange={this.handleChange}/>
                    </label>
                </div>

                <button type="submit" className="btn btn-submit icon" disabled={this.state.isLoading}>
                    {
                        !this.state.isLoading &&
                        this.context.t('ui.send')
                    }

                    {
                        this.state.isLoading &&
                        <i className="fa fa-spinner fa-spin send-icon loading"/>
                    }
                </button>
            </div>


            {
                this.state.error &&
                <span className="error">
                    {this.state.error}
                </span>
            }

            {
                this.state.success &&
                <span className="success">
                    {this.state.success}
                </span>
            }
        </form>;
    }

}

const mapDispatchToProps = dispatch => {
    return {
        fetchCreateAcl(user, instance) {
            return dispatch(fetchCreateAcl(user, instance));
        }
    };
};

export default connect(null, mapDispatchToProps)(InstanceInvitationForm);