import React from 'react';
import PropTypes from 'prop-types';

export default class CSVReader extends React.Component {


    constructor() {
        super();
    }

    state = {
        error: null
    };

    componentDidMount() {

    }


    _sanitizeEmailArray = (emailArray) => {
        let regExEmail = new RegExp(/^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/);
        return emailArray.filter(email => regExEmail.test(String(email).toLowerCase()));
    };

    _parseCSVData = (value) => {
        let resultArray = value.trim().split('\n');
        resultArray = this._sanitizeEmailArray(resultArray);
        this.props.onFileReaded(resultArray);
    };

    _extractData = (e) => {
        let reader = new FileReader();
        let files = e.target.files;
        //check if that is a real csv
        try {
            if (files && files[0].name.match('.csv')) {
                this.props.onFileReading();
                reader.onload = () => {
                    this._parseCSVData(reader.result)
                };
                reader.readAsText(files[0]);
            } else {
                this.setState({error: "Wrong type of file, only .csv accepted"})
            }
        } catch (e) {
            this.setState({error: e});
        }
    };


    render() {
        const {error} = this.state;
        return (
            <div>
                <label className="label">
                    <input className={"field"} id="fileSelect" type="file" accept=".csv" onChange={this._extractData}/>
                    {error && <div>{error}</div>}
                </label>
            </div>
        )
    }

}

CSVReader.propTypes = {
    onFileReaded: PropTypes.func,
    onFileReading: PropTypes.func
};