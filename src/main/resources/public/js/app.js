import React from 'react';
import { BrowserRouter } from 'react-router-dom';
import Router from './config/router';

//Load redux

//Components
import Layout from './components/layout';

class App extends React.Component {

    render() {
        return <BrowserRouter>
            <Layout>
                <Router/>
            </Layout>
        </BrowserRouter>;
    }

}

export default App;