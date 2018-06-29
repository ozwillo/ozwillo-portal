import React from 'react';
import {connect} from 'react-redux';

class Footer extends React.Component {
    render() {
        const siteMapFooter = this.props.siteMapFooter;
        return <footer className="oz-footer">
            <div className="flex-row">
                <section className="logo">
                    <img className="image" />
                </section>
                <section className="flex-row outer-links">
                    {
                        siteMapFooter && Object.keys(siteMapFooter).map((colKey) => {
                            const col = siteMapFooter[colKey];

                            return <ul key={colKey} className="outer-links-list undecorated-list">
                                {
                                    col.map((row, index) => {
                                        return <li key={index}>
                                            <a className="link" href={row.url}>{row.label}</a>
                                        </li>
                                    })
                                }
                            </ul>
                        })
                    }
                </section>
            </div>
            <div className="flex-row">
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
        </footer>;
    }
}

const mapStateToProps = state => {
    return {
        siteMapFooter: state.config.currentSiteMapFooter
    }
}

export default connect(mapStateToProps)(Footer);