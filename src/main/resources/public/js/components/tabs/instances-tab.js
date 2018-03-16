import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';

//Components
import AddInstanceDropdown from '../dropdown_menus/instance/add-instance-dropdown';
import InstanceDropdown from '../dropdown_menus/instance/instance-dropdown';

class InstancesTabHeader extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    render() {
        return <Link className="undecorated-link" to={`/my/organization/${this.props.organization.id}/instances`}>
            <header className="tab-header">
                <span>{this.context.t('organization.desc.applications')}</span>
            </header>
        </Link>;
    }
}

const InstancesTabHeaderWithRedux = connect(state => {
    return {
        organization: state.organization.current
    };
})(InstancesTabHeader);


class InstancesTab extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired,
    };

    constructor(props) {
        super(props);

        this.state = {
            instancesFilter: ''
        };

        //bind methods
        this.handleChange = this.handleChange.bind(this);
        this.filterInstances = this.filterInstances.bind(this);
    }

    handleChange(e) {
        const el = e.currentTarget;
        this.setState({
            [el.name]: el.value
        });
    }

    filterInstances(instances, filter) {
        if (!filter) {
            return instances;
        }
        const regex = new RegExp(`(\\w|\\S)*${filter.toUpperCase()}(\\w|\\S)*`);

        return instances.filter((instance) => {
            return regex.test(instance.name.toUpperCase());
        });
    }

    render() {
        const org = this.props.organization;
        const instancesFilter = this.state.instancesFilter;

        return <article className="instances-tab">

            {
                org.admin &&
                <section className="add-instance">
                    <header className="sub-title">{this.context.t('organization.desc.add-application')}</header>
                    <AddInstanceDropdown/>
                </section>
            }


            <section className="search-instance">
                <form className="search oz-form">
                    <input name="instancesFilter" className="field form-control" type="text" value={instancesFilter}
                           placeholder={this.context.t('ui.search')} onChange={this.handleChange}/>
                </form>

                <ul className="instances-list undecorated-list flex-col">
                    {
                        this.filterInstances(org.instances, instancesFilter).map((instance) => {
                            return <li key={instance.id} className="instance">
                                <InstanceDropdown instance={instance} members={org.members || []} isAdmin={org.admin}/>
                            </li>
                        })
                    }
                </ul>
            </section>

        </article>;
    }
}

const mapStateToProps = state => {
    return {
        organization: state.organization.current,
    };
};

const InstanceTabWithRedux = connect(mapStateToProps)(InstancesTab);


export {
    InstanceTabWithRedux as InstancesTab,
    InstancesTabHeaderWithRedux as InstancesTabHeader
};