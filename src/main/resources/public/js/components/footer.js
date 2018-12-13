import React from 'react';
import {connect} from 'react-redux';


class Footer extends React.Component {
    state = {
        env: ''
    }

    componentDidMount = async () => {
        this.setState({ env: localStorage.getItem("env") });
    }

    render() {
        const siteMapFooter = this.props.siteMapFooter;
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

const mapStateToProps = state => {
    return {
        siteMapFooter: state.config.currentSiteMapFooter
    }
}

export default connect(mapStateToProps)(Footer);