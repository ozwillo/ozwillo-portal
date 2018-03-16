import React from 'react';
import {connect} from 'react-redux';

//Components
import Header from './header';
import MyNav from './my-nav';
import Nav from './nav';
import Footer from './footer';

class Layout extends React.Component {

    render() {
        const isLogged = !!this.props.userInfo.sub;
        return <section className="layout wrapper">
            <Header/>
            {
                (isLogged && <MyNav/>) || <Nav/>
            }
            {this.props.children}
            <Footer/>
        </section>
    }
}

const mapStateToProps = state => {
    return {
        userInfo: state.userInfo
    };
};

export default connect(mapStateToProps)(Layout);