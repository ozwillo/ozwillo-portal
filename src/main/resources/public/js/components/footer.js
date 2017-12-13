import React from 'react';

class Footer extends React.Component {
    render() {
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
                           {/* <div data-th-each="entry,row: ${sitemapFooter}" className="col-xs-6 col-sm-3 col-md-2 text-center">
                                <div data-th-each="sme: ${entry.getValue()}" data-th-remove="tag">
                                    <a className="footer-link" data-th-href="${sme.url}" data-th-text="${sme.label}"></a>
                                </div>
                                <span id="contact" data-th-if="${row.index == 3}">
                                    <!-- Here, we'll insert the link that calls the contact modal in React's contact.jsx module -->
                                </span>
                            </div>*/}
                        </div>
                    </div>


                    <div className="col-md-12">
                        <div className="row">
                            <div className="col-md-3">
                                <div className="logo-twitter">
                                    <a href="https://twitter.com/ozwillo">
                                        <img data-th-src="@{/img/twitter.jpg}" />
                                    </a>
                                </div>
                            </div>
                            <div className="col-md-9">
                                <div className="pull-right top">
                                    <a href="https://www.ozwillo.com/en/oz/projects">
                                        <img data-th-src="@{/img/cip.jpg}" />
                                    </a>
                                    <a href="https://www.ozwillo.com/en/oz/projects">
                                        <img data-th-src="@{/img/eu.jpg}" />
                                    </a>
                                    <a href="https://www.ozwillo.com/en/oz/projects">
                                        <img data-th-src="@{/img/investir.jpg}" />
                                    </a>
                                    <a href="https://www.ozwillo.com/en/oz/projects">
                                        <img data-th-src="@{/img/RP.jpg}" />
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

export default Footer;