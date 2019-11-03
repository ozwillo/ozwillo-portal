import React from 'react';
import ConfigService from '../util/config-service';

class Footer extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            env: '',
            siteMapFooter: {}
        }

        this._configService = new ConfigService();
    }

    componentDidMount = async () => {
        const siteMapFooter = await this._configService.fetchSiteMapFooter();
        this.setState({ env: localStorage.getItem("env"), siteMapFooter: siteMapFooter });
    }

    render() {
        const siteMapFooter = this.state.siteMapFooter;
        return <footer className="oz-footer">
            <div className="flex-row">
                <section className="logo">
                    <div className="image" />
                </section>
                <section className="flex-row outer-links">
                    {
                        siteMapFooter && Object.keys(siteMapFooter).map((colKey) => {
                            const col = siteMapFooter[colKey];

                            return <ul key={colKey} className="outer-links-list undecorated-list">
                                {
                                    col.map((row, index) => {
                                        return <li key={index}>
                                            <a className="link" href={row.href}>{row.label}</a>
                                        </li>
                                    })
                                }
                            </ul>
                        })
                    }
                </section>
            </div>
            { this.state.env === 'ozwillo' &&
                <div className="flex-row additional-oz-footer">
                    <section className="social-network">
                        <a href="https://twitter.com/ozwillo">
                            <i className="fab fa-twitter image twitter"/>
                        </a>
                    </section>
                    <section className="partners flex-row end">
                        <a href="https://www.ozwillo.com/en/oz/projects">
                            <img src="/img/cip.png" className="image"/>
                        </a>
                        <a href="https://www.ozwillo.com/en/oz/projects">
                            <img src="/img/eu.jpg" className="image"/>
                        </a>
                        <a href="https://www.ozwillo.com/en/oz/projects">
                            <img src="/img/invest.png" className="image"/>
                        </a>
                        <a href="https://www.ozwillo.com/en/oz/projects">
                            <img src="/img/RP.png" className="image"/>
                        </a>
                    </section>
                </div>
            }
        </footer>;
    }
}

export default Footer;
