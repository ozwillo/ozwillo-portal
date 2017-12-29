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

    constructor(props) {
        super(props);

        this.state = {
            tabToDisplay: this.props.tabToDisplay || Object.keys(this.props.headers)[0] || ''
        };

        //bind methods
        this.onChangeTab = this.onChangeTab.bind(this);
    }


    componentWillReceiveProps(nextProps){
        this.setState({
            tabToDisplay: nextProps.tabToDisplay || Object.keys(nextProps.headers)[0] || ''
        });
    }

    onChangeTab(e) {
        this.setState({
            tabToDisplay: e.currentTarget.dataset.tab
        });
    }

    render() {
        const tabName = Object.keys(this.props.tabs).find(attr => {
            return attr === this.state.tabToDisplay;
        });
        const Component = this.props.tabs[tabName];

        return <section className={`tabs flex-col ${this.props.className || ''}`}>
            <header className="tabs-headers">
                <ul className="undecorated-list flex-row headers-list">
                    {
                        Object.keys(this.props.headers).map((tabName) => {
                            const Component = this.props.headers[tabName];
                            return <li key={tabName} onClick={this.onChangeTab} data-tab={tabName}
                                       className={`header ${(tabName === tabName && 'active') || ''}`}>
                                <Component/>
                            </li>
                        })
                    }
                </ul>
            </header>
            <article className="tab-content">
                { tabName && <Component/> }
            </article>

        </section>
    }
}

export default Tabs;