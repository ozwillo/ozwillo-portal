import React from 'react';
import {BrowserRouter} from 'react-router-dom';
import Router from './config/router';


class App extends React.Component {

    render() {
        return <BrowserRouter>
            <Router/>
        </BrowserRouter>;
    }

}

export default App;