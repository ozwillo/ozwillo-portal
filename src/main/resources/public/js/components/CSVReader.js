import React from 'react';
import PropTypes from 'prop-types';
import Tools from "../util/tools";
import { i18n } from "../config/i18n-config"

export default class CSVReader extends React.Component {


    constructor() {
        super();
        this._tools = new Tools();
    }

    state = {
        error: null,
        fileName: ""
    };

    componentDidMount(){
        this.props.fileName ? this.setState({fileName: this.props.fileName}) : null;
    }

    cleanInput = () => {
        this.inputFile.value = "";
        this.setState({fileName: ""});
    };

    _sanitizeEmailArray = (emailArray) => {
        return emailArray.filter(email => this._tools.checkEmail(email));
    };

    _parseCSVData = (value) => {
        let resultArray = value.trim().split(/[\r\n]+/g);
        resultArray.filter(elm => elm !== "");
        resultArray = this._sanitizeEmailArray(resultArray);
        this.props.onFileRead(resultArray);
    };

    _extractData = (e) => {
        let reader = new FileReader();
        let files = e.target.files;
        //check if that is a real csv
        try {
            if (files && files.length > 0 && files[0].name.match('.csv')) {
                this._extractFileName();
                this.props.onFileReading();
                reader.onload = () => {
                    this._parseCSVData(reader.result)
                };
                reader.readAsText(files[0]);
                this.setState({error: null})
            } else if(files && files.length > 0) {
                this.setState({error: i18n._('error.msg.csv-file-required')});
                this.cleanInput();
            }
        } catch (e) {
            this.cleanInput();
            this.setState({error: e});
        }
    };

    _extractFileName = () => {
        const fileName = this.inputFile.value.split(/.*[\/|\\]/).pop();
        this.setState({fileName: fileName})
        return fileName;
    };


    render() {
        const {error, fileName} = this.state;
        const {requiered} = this.props;
        return (
            <div className={"csv-reader"}>
                <label className="label btn btn-default" htmlFor="fileSelect">
                    {i18n._("organization.desc.choose-file")}
                </label>
                <p>
                    {fileName}
                </p>
                <input ref={inputFile => this.inputFile = inputFile}
                       className={"field"} required={requiered}
                       id="fileSelect" type="file"
                       accept=".csv" onChange={this._extractData}/>

                {error && <div className={"csv-error"}>{error}</div>}
            </div>
        )
    }

}


CSVReader.propTypes = {
    onFileRead: PropTypes.func,
    onFileReading: PropTypes.func,
    fileName: PropTypes.string,
};

