import React from 'react';

import Header from './header';
import MyNav from './my-nav';
import Nav from './nav';
import Footer from './footer';
import UserService from '../util/user-service';

class Layout extends React.Component {

    constructor(props) {
        super(props);

        this._userService = new UserService();

        this.state = {
            isLoggedIn: false
        }
    }

    componentDidMount = async () => {
        const userInfo = await this._userService.fetchUserInfos();
        this.setState({ isLoggedIn: !!userInfo });
    }

    render() {
        return <section className="layout wrapper">
            <Header/>
            {
                (this.state.isLoggedIn && <MyNav/>) || <Nav/>
            }
            {this.props.children}
            <Footer/>
        </section>
    }
}

export default Layout;
