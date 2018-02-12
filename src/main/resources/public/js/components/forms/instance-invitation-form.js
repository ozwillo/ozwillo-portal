import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import Select from 'react-select';


//Action
import { fetchCreateAcl } from "../../actions/acl";

class InstanceInvitationForm extends React.Component {

    static propTypes = {
        instance: PropTypes.object.isRequired,
        members: PropTypes.array.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            selectedOption: null,
            email: '',
            addToOrganization: false,
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
        this.setState({ selectedOption: selectedOption })
    }

    handleChange(e) {
        const el = e.currentTarget;
        this.setState({
            [el.name]: el.type === 'checkbox' ? el.checked : el.value
        });
    }

    onSubmit(e) {
        e.preventDefault();

        this.setState({ isLoading: true });

        const user = this.state.selectedOption || { email: this.state.email };
        this.props.fetchCreateAcl(user, this.props.instance)
            .then(() => {
                this.setState({
                    isLoading: false,
                    selectedOption: null,
                    email: '',
                    success: 'Your request has been sent !',
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
                    placeholder="Members"
                    required={!this.state.email}/>

                <em className="sep-text">or</em>

                <div className="new-user-fieldset flex-row">
                    <label className="label">
                        Email
                        <input name="email" type="email" className="form-control field" required={!this.state.selectedOption}
                               value={this.state.email} onChange={this.handleChange} />
                    </label>


                    <label className="label">
                        <input name="addInOrganization" type="checkbox" className="field"
                               checked={this.state.addToOrganization} onChange={this.handleChange}/>
                        Add in organization
                    </label>
                </div>

                <button type="submit" className="submit btn icon" disabled={this.state.isLoading}>
                    {
                        !this.state.isLoading &&
                        <i className="fa fa-paper-plane send-icon"/>
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