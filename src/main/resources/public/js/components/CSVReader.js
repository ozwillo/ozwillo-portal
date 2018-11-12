import React from 'react';
import PropTypes from 'prop-types';
import Tools from "../util/tools";
import OrganizationCard from "./organization-card";

export default class CSVReader extends React.Component {


    constructor() {
        super();
        this._tools = new Tools();
    }

    state = {
        error: null
    };

    cleanInput = () => {
        this.inputFile.value = "";
    };

    _sanitizeEmailArray = (emailArray) => {
        return emailArray.filter(email => this._tools.checkEmail(email));
    };

    _parseCSVData = (value) => {
        let resultArray = value.trim().split(/[\r\n]+/g);
        resultArray.filter(elm =>  elm !== "");
        resultArray = this._sanitizeEmailArray(resultArray);
        this.props.onFileRead(resultArray);
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
                this.setState({error: null})
            } else {
                this.setState({error: this.context.t('error.msg.csv-file-required')});
                this.cleanInput();
            }
        } catch (e) {
            this.setState({error: e});
        }
    };


    render() {
        const {error} = this.state;
        const {requiered} = this.props;
        return (
            <div>
                <label className="label">
                    <input ref={inputFile => this.inputFile = inputFile}
                           className={"field"} required={requiered}
                           id="fileSelect" type="file"
                           accept=".csv" onChange={this._extractData}/>
                </label>
                {error && <div className={"csv-error"}>{error}</div>}
            </div>
        )
    }

}


CSVReader.propTypes = {
    onFileRead: PropTypes.func,
    onFileReading: PropTypes.func,
};


CSVReader.contextTypes = {
    t: PropTypes.func.isRequired
};
