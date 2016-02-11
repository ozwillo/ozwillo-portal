/** @jsx React.DOM */

var UsersList = React.createClass({
    propTypes: {
        users: React.PropTypes.array.isRequired,
        removeUser: React.PropTypes.func.isRequired
    },
    render: function() {
        var usersList = this.props.users.map(function(user) {
            var userId = user.status === 'new_from_email' ? user.email : user.userid;
            return <User key={userId} user={user} removeUser={this.props.removeUser(userId)} />;
        }.bind(this));

        return (
            <div className="users-table">
                <table className="table table-striped table-responsive">
                    <thead>
                    <tr>
                        <th>{t('name')}</th>
                        <th>{t('status')}</th>
                        <th>{t('actions')}</th>
                    </tr>
                    </thead>
                    <tbody>
                        {usersList}
                    </tbody>
                </table>
            </div>
        );
    }
});

var User = React.createClass({
    propTypes: {
        user: React.PropTypes.object.isRequired,
        removeUser: React.PropTypes.func.isRequired
    },
    displayStatus: function(user) {
        if (user.status === 'new_from_organization' || user.status === 'new_from_email')
            return t('settings.status.new');
        else
            return t('settings.status.member');
    },
    render: function() {
        return (
            <tr>
                <td>{this.props.user.fullname || this.props.user.email}</td>
                <td>{this.displayStatus(this.props.user)}</td>
                <td>
                    <button className="btn oz-btn-danger" onClick={this.props.removeUser}>
                        <i className="fa fa-trash"></i>
                    </button>
                </td>
            </tr>
        );
    }
});

var OrgUserPicker = React.createClass({
    propTypes: {
        addUser: React.PropTypes.func.isRequired,
        queryUsers: React.PropTypes.func.isRequired
    },
    renderSuggestionTemplate: function(data) {
        return '<div>' + data.fullname + '</div>';
    },
    addUser: function(user, typeaheadRef) {
        this.props.addUser(user);
        typeaheadRef.typeahead('val', '');
    },
    render: function() {
        return (
            <div className="row">
                <label htmlFor="search-user" className="control-label col-sm-2">{t('setting-add-from-organization')}</label>
                <div className="col-sm-9">
                    <Typeahead onSelect={this.addUser} source={this.props.queryUsers} placeholder={t('settings-add-a-user')}
                               display="fullname" suggestionTemplate={this.renderSuggestionTemplate}
                               fieldId="search-user" minLength={0} />
                </div>
            </div>
        );
    }
});
