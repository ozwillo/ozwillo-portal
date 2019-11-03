import React from 'react';

import DropDownMenu from '../../dropdown-menu';
import AddInstanceDropdownHeader from './add-instance-dropdown-header';
import { addInstanceToOrg } from "../../../util/store-service";

class AddInstanceDropdown extends React.Component {


    constructor(props) {
        super(props);

        this.state = {
            app: null
        };

        this.onAddInstance = this.onAddInstance.bind(this);
        this.onChangeApp = this.onChangeApp.bind(this);
    }

    onAddInstance() {
        if (!this.state.app) {
            return;
        }

        const orgId = this.props.organization.id;
        return addInstanceToOrg(orgId, this.state.app)
            .then(() => {
                this.setState({
                    app: null
                })
            });
    }

    onChangeApp(app) {
        this.setState({app});
    }

    render() {
        const app = this.state.app;

        const Header = <AddInstanceDropdownHeader
            organization={this.props.organization}
            app={app}
            onAddInstance={this.onAddInstance}
            onChangeInstance={this.onChangeApp}/>;

        return <DropDownMenu header={Header} isAvailable={false} className="action-header">
        </DropDownMenu>;
    }
}

export default AddInstanceDropdown;
