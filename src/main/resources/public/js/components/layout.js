import React from 'react';

//Components
import Header from './header';
import Nav from './nav';
import Footer from './footer';

class Layout extends React.Component {

    render() {
        return <section className="layout wrapper">
            <Header/>
            <Nav/>
            { this.props.children }
            <Footer/>
        </section>
    }
}

export default Layout;