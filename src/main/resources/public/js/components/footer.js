import React from 'react';
import { connect } from 'react-redux';

class Footer extends React.Component {
    render() {
        const siteMapFooter = this.props.siteMapFooter;
        return <footer className="page-row">
            <div className="container-fluid">
                <div className="row footer">
                    <div className="col-md-12 footer-top-block">
                        <div className="row flex-row">
                            <div className="col-md-4 col-xs-12">
                                <div className="footer-logo">
                                    <img src="/img/logo-ozwillo-footer.png" />
                                </div>
                            </div>
                            {
                                siteMapFooter && Object.keys(siteMapFooter).map((colKey) => {
                                    const col = siteMapFooter[colKey];

                                    return <div key={colKey} className="col-xs-6 col-sm-3 col-md-2 text-center">
                                        {
                                            col.map((row, index) => {
                                                return <section key={index}>
                                                    <div>
                                                        <a className="footer-link" href={row.url}>{row.label}</a>
                                                    </div>
                                                    {
                                                        index === 3 && <span id="contact">
                                                            {/*Here, we'll insert the link that calls the contact modal in React's contact.jsx module*/}
                                                        </span>
                                                    }
                                                </section>

                                            })
                                        }
                                    </div>
                                })
                            }
                        </div>
                    </div>


                    <div className="col-md-12">
                        <div className="row">
                            <div className="col-md-3">
                                <div className="logo-twitter">
                                    <a href="https://twitter.com/ozwillo">
                                        <img src="/img/twitter.jpg" />
                                    </a>
                                </div>
                            </div>
                            <div className="col-md-9">
                                <div className="pull-right top">
                                    <a href="https://www.ozwillo.com/en/oz/projects">
                                        <img src="/img/cip.jpg" />
                                    </a>
                                    <a href="https://www.ozwillo.com/en/oz/projects">
                                        <img src="/img/eu.jpg" />
                                    </a>
                                    <a href="https://www.ozwillo.com/en/oz/projects">
                                        <img src="/img/investir.jpg" />
                                    </a>
                                    <a href="https://www.ozwillo.com/en/oz/projects">
                                        <img src="/img/RP.jpg" />
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </footer>;
    }
}

const mapStateToProps = state => {
    return {
        siteMapFooter: state.config.siteMapFooter
    }
}

export default connect(mapStateToProps)(Footer);