import React from 'react';
import PropTypes from 'prop-types';

//Components
import GeoAreaAutosuggest from '../autosuggests/geoarea-autosuggest';
import Select from 'react-select';

//Config
import Config from '../../config/config';

const instanceVisibility = Config.instanceVisibility;
const iconMaxSize = Config.iconMaxSize;

//Action
import {uploadFile} from '../../actions/file-upload';

class InstanceConfigForm extends React.Component {

    static propTypes = {
        instance: PropTypes.object.isRequired,
        onSubmit: PropTypes.func.isRequired,
        isLoading: PropTypes.bool
    };

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        const serviceSelected = (props.instance.services.length === 1) ? props.instance.services[0].catalogEntry : null;
        this.state = {
            iconError: '',
            serviceSelected,
            name: (serviceSelected && serviceSelected.name) || '',
            description: (serviceSelected && serviceSelected.description) || '',
            iconUrl: (serviceSelected && serviceSelected.icon) || '',
            area: this.readGeoNameFromUri(serviceSelected.geographical_areas[0]),
            areaUri: serviceSelected.geographical_areas[0] || '',
            visibility: serviceSelected && serviceSelected.visibility === instanceVisibility.visible
        };

        //bind methods
        this.handleChange = this.handleChange.bind(this);
        this.handleServiceChange = this.handleServiceChange.bind(this);
        this.onGeoAreaSelected = this.onGeoAreaSelected.bind(this);
        this.onGeoAreaChange = this.onGeoAreaChange.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
        this.uploadIconFile = this.uploadIconFile.bind(this);
        this.readGeoNameFromUri = this.readGeoNameFromUri.bind(this);
    }

    readGeoNameFromUri(uri) {
        if (!uri) {
            return ''
        }

        return decodeURI(uri.substring(uri.lastIndexOf("/") + 1));
    }

    onSubmit(e) {
        e.preventDefault();

        const result = Object.assign({}, this.state.serviceSelected, {
            name: this.state.name,
            description: this.state.description,
            icon: this.state.iconUrl,
            geographical_areas: this.state.areaUri && [this.state.areaUri] || []
        });

        if (this.state.serviceSelected.visibility !== instanceVisibility.never) {
            result.visibility = this.state.visibility ? instanceVisibility.visible : instanceVisibility.hidden;
        }

        result.local_id = this.state.serviceSelected.local_id || '';
        result.tos_uri = this.state.serviceSelected.tos_uri || '';

        this.props.onSubmit(this.props.instance.id, result);
    }

    handleChange(e) {
        const el = e.currentTarget;
        this.setState({[el.name]: (el.type === 'checkbox') ? el.checked : el.value});
    }

    handleServiceChange(serviceSelected) {
        this.setState({serviceSelected})
    }

    onGeoAreaChange(e) {
        const el = e.currentTarget;

        this.setState({
            area: el.value,
            areaUri: ''
        });
    }

    onGeoAreaSelected(geoArea) {
        this.setState({
            area: geoArea.name,
            areaUri: geoArea.uri
        });
    }

    uploadIconFile(e) {
        const file = e.currentTarget.files[0];

        if (file.size > iconMaxSize) {
            e.currentTarget.value = null;
            this.setState({iconError: `icon must have a max size of ${Math.round(iconMaxSize / 1024)} Ko`});
            return;
        }

        uploadFile(file, this.state.serviceSelected.id)
            .then(iconUrl => {
                this.setState({iconUrl, iconError: ''});
            })
            .catch(err => {
                this.setState({iconError: err.error});
            });
    }

    render() {
        return <form className="oz-form instance-config-form" onSubmit={this.onSubmit}>
            <fieldset className="oz-fieldset">

                <div className="flex-row">
                    <label htmlFor="serviceSelected" className="label">Service</label>
                    <Select className="select field" value={this.state.serviceSelected}
                            onChange={this.handleCountryChange}
                            options={this.props.instance.services} clearable={false}
                            valueKey="id" labelKey="name"
                            placeholder="Service" required={true}/>
                </div>

                {
                    this.state.serviceSelected &&
                    <div>
                        <div className="flex-row">
                            <label htmlFor="name" className="label">Name</label>
                            <input type="text" name="name" id="name" className="form-control field"
                                   value={this.state.name} onChange={this.handleChange} required={true}/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="description" className="label">Description</label>
                            <textarea name="description" id="description" className="form-control field"
                                      value={this.state.description} onChange={this.handleChange}/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="iconUrl" className="label">Icon</label>

                            <div className="flex-row field icon">
                                <img src={this.state.iconUrl}/>

                                <div className="desc-field text-center">
                                    <input type="text" name="iconUrl" id="iconUrl" className="form-control"
                                           value={this.state.iconUrl} onChange={this.handleChange} required={true}/>
                                    <span className="desc">{this.context.t('my.apps.upload')}</span>
                                </div>
                            </div>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="iconFile" className="label empty"> </label>
                            <div className="flex-col field">
                                <input name="iconFile" type="file"
                                       onChange={this.uploadIconFile} accept="image/*"/>
                                {
                                    this.state.iconError &&
                                    <span className="error-message">{this.state.iconError}</span>
                                }
                            </div>

                        </div>


                        <div className="flex-row">
                            <label htmlFor="area" className="label">Geographical area of interest</label>
                            <GeoAreaAutosuggest name="area" countryUri=""
                                                endpoint="/geographicalAreas"
                                                onChange={this.onGeoAreaChange}
                                                onGeoAreaSelected={this.onGeoAreaSelected}
                                                value={this.state.area}/>
                        </div>

                        {
                            this.state.serviceSelected.entryCatalog !== instanceVisibility.never &&
                            <div className="flex-row">
                                <label htmlFor="visibility" className="label">
                                    Published on the app store
                                </label>
                                <input id="visibility" name="visibility" type="checkbox"
                                       checked={this.state.visibility}
                                       onChange={this.handleChange} className="field"/>
                            </div>
                        }

                        <div className="flex-row">
                            {
                                (!this.props.isLoading &&
                                    <input type="submit" value="Save" className="btn btn-submit"/>) ||
                                <button type="button" className="btn icon">
                                    <i className="fa fa-spinner fa-spin loading"/>
                                </button>
                            }
                        </div>

                    </div>
                }

            </fieldset>
        </form>;
    }

}

export default InstanceConfigForm;