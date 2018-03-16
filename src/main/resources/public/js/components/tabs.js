import React from 'react';
import PropTypes from 'prop-types';

class Tabs extends React.Component {

    static propTypes = {
        //Warning: Attributes of fields headers and tabs must be sames
        //ex headers { tab1: () => "toto", tab2: () => <h1>Toto 2</h1> }
        headers: PropTypes.object.isRequired,

        //ex tabs { tab1: () => "content", tab2: () => <section>content</section> }
        tabs: PropTypes.object.isRequired,

        //Name of attribute associate with the tab
        tabToDisplay: PropTypes.string
    };

    render() {
        const tabToDisplay = this.props.tabToDisplay;
        const tabName = Object.keys(this.props.tabs).find(attr => {
            return attr === tabToDisplay;
        });
        const Component = this.props.tabs[tabName];

        return <section className={`tabs flex-col ${this.props.className || ''}`}>
            <header className="tabs-headers">
                <ul className="headers-list undecorated-list flex-row">
                    {
                        Object.keys(this.props.headers).map((tabName) => {
                            const Component = this.props.headers[tabName];
                            return <li key={tabName} onClick={this.onChangeTab} data-tab={tabName}
                                       className={`header ${(tabName === tabToDisplay && 'active') || ''}`}>
                                <Component/>
                            </li>
                        })
                    }
                </ul>
            </header>
            <article className="tab-content">
                {tabName && <Component/>}
            </article>

        </section>
    }
}

export default Tabs;