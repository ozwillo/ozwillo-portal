import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

//Components
import DropDownMenu from '../../dropdown-menu';
import ServiceInvitationForm from '../../service-invitation-form';


class ServiceDropdown extends React.Component {

    static propTypes = {
        service: PropTypes.object.isRequired
    };

    render() {

        const Header = <header>Header</header>;

        const Footer = <footer>
            <ServiceInvitationForm members={this.props.members} service={this.props.service}/>
        </footer>;

        return <DropDownMenu header={Header} footer={Footer}>
            <p>Service Dropdown</p>
        </DropDownMenu>;
    }
}

const mapStateToProps = state => {
    return {
        members: state.member.members
    }
};

export default connect(mapStateToProps)(ServiceDropdown);
