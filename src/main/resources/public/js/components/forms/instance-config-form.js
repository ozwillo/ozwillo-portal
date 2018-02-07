import React from 'react';
import PropTypes from 'prop-types';

//Components
import GeoAreaAutosuggest from '../autosuggests/geoarea-autosuggest';
import Select from 'react-select';

//Config
import Config from '../../config/config';
const instanceVisibility = Config.instanceVisibility;

class InstanceConfigForm extends React.Component {

    static propTypes = {
        instance: PropTypes.object.isRequired,
        onSubmit: PropTypes.func.isRequired,
        isLoading: PropTypes.bool
    };

    constructor(props) {
        super(props);

        const serviceSelected = (props.instance.services.length === 1)? props.instance.services[0].catalogEntry : null;
        this.state = {
            serviceSelected,
            name: (serviceSelected && serviceSelected.name) || '',
            description: (serviceSelected && serviceSelected.description) || '',
            iconUrl: (serviceSelected && serviceSelected.icon) || '',
            icon: null,
            area: '',
            areaUri: '',
            visibility: serviceSelected && serviceSelected.visibility === instanceVisibility.visible
        };

        //bind methods
        this.handleChange = this.handleChange.bind(this);
        this.handleServiceChange = this.handleServiceChange.bind(this);
        this.onGeoAreaSelected = this.onGeoAreaSelected.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
    }

    onSubmit(e) {
        e.preventDefault();

        const result = Object.assign({}, this.state.serviceSelected, {
            name: this.state.name,
            description: this.state.description,
            icon: this.state.iconUrl,
        });

        if(this.state.serviceSelected.visibility !== instanceVisibility.never) {
            result.visibility = this.state.visibility ? instanceVisibility.visible : instanceVisibility.hidden;
        }

        result.local_id = this.state.serviceSelected.local_id || '';
        result.tos_uri = this.state.serviceSelected.tos_uri || '';

        this.props.onSubmit(this.props.instance.id, result);
    }

    handleChange(e) {
        const el = e.currentTarget;
        this.setState({ [el.name]: ( el.type === 'checkbox') ?  el.checked : el.value });
    }

    handleServiceChange(serviceSelected) {
        this.setState({ serviceSelected })
    }

    onGeoAreaSelected(geoArea) {
        debugger;
        this.setState({
            area: geoArea.name,
            areaUri: geoArea.uri
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
                            placeholder="Service" required={true} />
                </div>

                {
                    this.state.serviceSelected &&
                    <div>
                        <div className="flex-row">
                            <label htmlFor="name" className="label">Name</label>
                            <input type="text" name="name" id="name" className="form-control field"
                                   value={this.state.name} onChange={this.handleChange} />
                        </div>

                        <div className="flex-row">
                            <label htmlFor="description" className="label">Description</label>
                            <textarea name="description" id="description" className="form-control field"
                                   value={this.state.description} onChange={this.handleChange} />
                        </div>

                        <div className="flex-row">
                            <label htmlFor="iconUrl" className="label">Icon</label>
                            <input type="text" name="iconUrl" id="iconUrl" className="form-control field"
                                   value={this.state.iconUrl} onChange={this.handleChange} />
                        </div>


                        <div className="flex-row">
                            <label htmlFor="area" className="label">Geographical area of interest</label>
                            <GeoAreaAutosuggest name="area" countryUri=""
                                                endpoint="/geographicalAreas"
                                                onChange={this.handleChange}
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
                                (!this.props.isLoading && <input type="submit" value="Save" className="submit btn"/> ) ||
                                <button type="button" className="submit btn icon">
                                    <i className="fa fa-spinner fa-spin" />
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