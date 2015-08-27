/** @jsx React.DOM */

var default_org_data = {
        organization: { exist: false, legal_name: '', sector_type: '', in_activity: true, alt_name: '', org_type:'',
            tax_reg_num: '', tax_reg_ofical_id:'', tax_reg_activity_uri:'', jurisdiction_uri:'', jurisdiction:'', phone_number:'',
            web_site:'', email:'', street_and_number:'', additional_address_field: '', po_box: '', city: '', city_uri: '',
            zip: '', cedex: '', country_uri: ''
         }, errors: [[],[]], typeRestriction: ''
};

// Main modal
var CreateOrModifyOrganizationModal = React.createClass({
    getInitialState: function () { return default_org_data },

    componentDidMount: function() {
        $(this.refs.modalcreateOrModifyOrg.getDOMNode()).on("shown.bs.modal", function() {
            $("input", this).first().focus();
        });
    },
    show: function(org) {
        if(org){
           var state = this.state;
           state.organization = org;
           this.setState(state);
        }
        this.refs.modalcreateOrModifyOrg.open();
        //this.refs.tabbedForm.switchTab(2);
        //this.refs.tabbedForm.switchTab(1);
    },
    close: function (org) {
        this.refs.modalcreateOrModifyOrg.close();
        if (this.props.successHandler) {
            this.props.successHandler(org);
        }
    },
    onError: function (data) {
        this.refs.modalError.open();
    },
    createOrModifOrg: function(event){
        return (this.refs.tabbedForm.createOrModifOrg(event) ); // return true if the organization has been created successful
    },

    render: function() {
        var org = this.state.organization; //Note: objects are passed by ref so all changes will visible for all (if rendered)
        var typeRestriction = this.props.typeRestriction;

        var saveButton = org.exist ? t('ui.save') : t('my.network.create-org');
        var buttonLabels = {"cancel": t('ui.cancel'), "save": saveButton };
        var modalTitle = org.exist ? t('my.network.modify-org') : t('my.network.create-org') ;


        return (<div>
                <Modal large={true} ref="modalcreateOrModifyOrg" title={modalTitle}
                       successHandler={this.createOrModifOrg} buttonLabels={buttonLabels}>
                    <CreateOrModifyOrganizationForm ref="tabbedForm" successHandler={this.close} errorHandler={this.onError} 
                       organization={org} typeRestriction={typeRestriction}/>
                </Modal>

                <Modal ref="modalError" title={t('ui.something_went_wrong_title')} infobox={true} cancelHandler={null/*this.close()*/} >
                    <div><h5>{t('ui.unexpected_error')}</h5></div>
                    <br/><div><h5>Err: Possibly the organization is already assigned in kernel</h5></div>
                </Modal>
                </div>
            );
    }
});

var CreateOrModifyOrganizationForm = React.createClass({
    getDefaultProps: function () { return { organization: default_org_data.organization, errors: default_org_data.errors } },
    getInitialState: function () { return { organization: this.props.organization, errors: default_org_data.errors, activeTab: 1}},
    switchTab: function (idx) {  this.refs.navtab.changeTab({id: idx}); },
    validateFields: function(org){
        var errors = [];
        // tab 1
        var errTab1 = [];
        if (!org.legal_name  || org.legal_name.trim() == '')       { errTab1.push('legal_name'); }
        if (!org.tax_reg_num || org.tax_reg_num.trim() == '')      { errTab1.push('tax_reg_num'); }
        if ((org.sector_type === 'PUBLIC_BODY') &&
            (!org.jurisdiction_uri || org.jurisdiction_uri.trim() == '')) { errTab1.push('jurisdiction_uri'); }
        errors.push(errTab1);
        // tab 2
        var errTab2 = [];
        if (!org.street_and_number || org.street_and_number.trim() == '')       { errTab2.push('street_and_number'); }
        if (!org.zip               || org.zip.trim() == '')                     { errTab2.push('zip'); }
        if (!org.city_uri          || org.city_uri.trim() == '')                { errTab2.push('city_uri'); errTab2.push('city'); }
        if (!org.exist && (!org.country_uri || org.country_uri.trim() == ''))   { errTab2.push('country_uri'); errTab2.push('country'); }
        errors.push(errTab2);

        this.setState({errors: errors});
        return errors;
     },
    callCreateOrganization: function(org){
        $.ajax({
            url: network_service + '/create-dc-organization',
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify(org),
            success: function (data) {
                if(data || data !== ''){ this.props.successHandler(data);
                }else{ /* show message */
                    console.error('No organziation was created with'+ org + '. Result :', data);
                    this.props.errorHandler(data);
                }
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
                this.props.errorHandler();
            }.bind(this)
        });
    },
    callUpdateOrganization: function(org){
        $.ajax({
            url: network_service + '/update-dc-organization',
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify(org),
            success: function (data) {
                if(data){ this.props.successHandler(data);
                }else{ /* show message */
                    console.error('No organziation was created with'+ org + '. Result :', data);
                    this.props.errorHandler(data);
                }
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
                this.props.errorHandler();
            }.bind(this)
        });
    },
    createOrModifOrg: function (event) {
        if (event) { event.preventDefault(); }
        var org = this.props.organization;
        var errs = this.validateFields(org);
        if ( (errs[0].length + errs[1].length) > 0) {
            if(errs[0].length > 0){ this.switchTab(1);
            } else{ this.switchTab(2);}
            return false;
        } else {
           //if variable "exist" from Organization (was found) then call updateOrganization, otherwise createOrganization
           if(org.exist){
              this.callUpdateOrganization(org);
           } else {
              this.callCreateOrganization(org);
           }
           // Reinitialize some values
           this.switchTab(1);
           // If everything was done correctly
           if(this.props.successHandler){this.props.successHandler()}
           return true;
        }
    },

    render: function () {
        var tabList = [
                       { 'id': 1, 'name': t('my.network.organization.tab1.general_information'), 'url': '#' },
                       { 'id': 2, 'name': t('my.network.organization.tab2.address_geolocation'), 'url': '#' },
        ];
        var organization = this.props.organization;

        return (
              <div className="form-horizontal">
                  <NavTab ref="navtab"  tabList={tabList} currentTab={this.state.activeTab}>
                     <Tab1 id={1} orgData={organization} errors={this.state.errors[0]} switchTab={this.switchTab}
                             valdiate={this.validateFields} typeRestriction={this.props.typeRestriction}/>
                     <Tab2 id={2} orgData={organization} errors={this.state.errors[1]} switchTab={this.switchTab}
                             valdiate={this.validateFields} typeRestriction={this.props.typeRestriction}/>
                  </NavTab>
              </div>
            );
    }
});

/* PROPS: name, className, class_name_div, error, isRequired, (children)*/
var Field = React.createClass({
     renderLabel: function(htmlFor, class_name, label, isRequired){
        var cn = isRequired ? <label className={'error'}>{'*'}</label> : ''
        return (<label htmlFor={htmlFor} className={class_name}>{label} {cn}</label>);
     },
    render: function() {
        var className = "control-label col-sm-3";
        var classNameDiv = "col-sm-7";
        if (this.props.class_name) {className = this.props.class_name; }
        if (this.props.error) { className = className + " error"; }
        if (this.props.class_name_div) {classNameDiv = this.props.class_name_div; }
        return (
            <div className="form-group">
                {this.renderLabel(this.props.name, className, t('my.network.organization.'+this.props.name), this.props.isRequired)}
                <div className={classNameDiv}>
                   {this.props.children}
                </div>
            </div>
            );
    }
});

var iconClassName = "control-label col-sm-2";
function getSectorTypeLabel(sector_type, type_restriction){
    if (type_restriction) { // if user has organization type restriction
        if (!type_restriction.company) { sector_type = 'PUBLIC_BODY';
        } else if (!type_restriction.public_body) { sector_type = 'COMPANY'; }
    }// if not, keeps the user selection
    return (  (sector_type === 'COMPANY' || sector_type === 'Private' )
                  ? t('search.organization.sector-type.COMPANY')
                  : t('search.organization.sector-type.PUBLIC_BODY')
    );
}

var Tab1 = React.createClass({
    getInitialState: function() { return {organization: this.props.orgData, errors: [], errorMsg:'' }; },
    moveTabRight: function() {
        var errors = this.props.valdiate(this.state.organization);
        if(errors[0].length == 0){ this.props.switchTab(this.props.id+1); }
    },
    changeInput: function (fieldname, isCheckbox) {
        return function (event) {
           var org = this.state.organization;
           if (isCheckbox) {
               org[fieldname] = event.target.checked;
           } else { //org[fieldname] = this.refs.geoSearchJurisdiction.state.value;
               org[fieldname] = event.target.value;
               if(fieldname === 'jurisdiction_uri'){ org['jurisdiction'] = event.added ? event.added.name : ''; }
           }
           this.setState({organization: org});
        }.bind(this);
    },
    updateUploadedIcon: function (servedImageUrlData){
        var org = this.state.organization;

        org.iconUrl = servedImageUrlData; // not required, inited by server to the Kernel service's
        this.setState({organization: org, errorMsg:''});
    },
    error: function (xhr, status, err) {
        this.setState({errorMsg: t("ui.unexpected_error")});
    },

    render: function() {
        if ($.inArray("icon", this.props.errors) != -1) { iconClassName = iconClassName + " error"; }
        this.state.organization = this.props.orgData;
        var sectorType = getSectorTypeLabel(this.state.organization.sector_type, this.props.typeRestriction);
        var label_regNum; var label_regOfficialId = ''; var label_regActivity = '';
        var n = this.state.organization.country_uri ? this.state.organization.country_uri.lastIndexOf('/') : -1;
        var acronymCountry = n > 0 ? this.state.organization.country_uri.substring(n + 1) : '';
        switch(acronymCountry){
           case 'BG' : label_regNum = 'tax_reg_num.bg'; label_regActivity = 'tax_reg_activity.bg'; break;
           case 'IT' : label_regNum = 'tax_reg_num.it'; label_regActivity = 'tax_reg_activity.it'; break;
           case 'FR' : label_regNum = 'tax_reg_num.fr';
                             label_regOfficialId = 'tax_reg_ofical_id.fr';
                             label_regActivity = 'tax_reg_activity.fr';
                             break;
           case 'ES' : label_regNum = 'tax_reg_num.es'; label_regActivity = 'tax_reg_activity.es'; break;
           case 'TR' : label_regNum = 'tax_reg_num.tr';
                             label_regOfficialId = 'tax_reg_ofical_id.tr';
                             label_regActivity = 'tax_reg_activity.tr';
                             break;
           default   : label_regNum = 'tax_reg_num.en'; break;
        }


        return (
             <div id="tab1">
               <div className="container-fluid">
               <div className="row">
               <br/>
                <div className="col-sm-15">
                   <Field name="legal_name" class_name={'control-label col-sm-2'} error={$.inArray("legal_name", this.props.errors) != -1} isRequired={true}>
                      <input className="form-control" id="legal_name" type="text" value={this.state.organization.legal_name}
                               onChange={this.changeInput('legal_name')} disabled={this.state.organization.exist}/>
                      <label>{sectorType}</label>

                      <label className="col-sm-offset-3">{t('my.network.organization.in_activity')}: &nbsp;
                         <input id="in_activity" type="checkbox" checked={this.state.organization.in_activity}
                               onChange={this.changeInput('in_activity', true)}/></label>
                   </Field><br/>

                   <Field name="alt_name" class_name={'control-label col-sm-2'}>
                      <input className="form-control" id="alt_name" type="text" value={this.state.organization.alt_name}
                      onChange={this.changeInput('alt_name')} />
                   </Field>

               </div></div> { /* End of col 1 & row 1*/}

               <div className="row">
                  <div className="col-sm-8">

                  <Field name="org_type" class_name_div='col-sm-5'>
                     <input className="form-control" id="org_type" type="text" value={this.state.organization.org_type}
                              onChange={this.changeInput('org_type')} placeholder={t('my.network.organization.org_type.placeholder')}/>
                  </Field>
                  <Field name={label_regNum} error={$.inArray("tax_reg_num", this.props.errors) != -1} isRequired={true}>
                     <input className="form-control" id="tax_reg_num" type="text" value={this.state.organization.tax_reg_num}
                              onChange={this.changeInput('tax_reg_num')} disabled={true}/>
                  </Field>
                  { // Show the field if is defined (normaly only for public orgs in France or Turkey).
                     (label_regOfficialId !== '' && this.state.organization.sector_type !== 'COMPANY') ?  (
                        <Field name={label_regOfficialId}>
                           <input className="form-control" id="tax_reg_ofical_id" type="text" value={this.state.organization.tax_reg_ofical_id}
                                   onChange={this.changeInput('tax_reg_ofical_id')} />
                        </Field>
                      ) : ''
                  }
                  <Field name={label_regActivity} class_name_div='col-sm-3'>
                     {/*<input className="form-control" id="tax_reg_activity" type="text" value={this.state.organization.tax_reg_activity}
                               onChange={this.changeInput('tax_reg_activity')} /> */}
                     <GeoSingleSelect2Component ref="geoSearchtaxRegActivity" className="form-control" key={this.state.organization.tax_reg_activity_uri}
                         name="geoSearchRegActivity" urlResources={store_service + "/dc-taxRegActivity"}
                         onChange={this.changeInput('tax_reg_activity_uri')} minimumInputLength={2}
                         countryFilter={ {country_uri:this.state.organization.country_uri} /*{country_uri:''}*/ }
                         placeholder={ !this.state.organization.tax_reg_activity_uri ? ' '
                               : this.state.organization.tax_reg_activity_uri.substring(this.state.organization.tax_reg_activity_uri.lastIndexOf("/")+1)
                         }
                     />
                  </Field><br/>
                  { // Show the field only for public orgs
                     (this.state.organization.sector_type === 'COMPANY') ? "" :(
                        <Field name="jurisdiction" error={$.inArray("jurisdiction_uri", this.props.errors) != -1} isRequired={true}>
                           <GeoSingleSelect2Component ref="geoSearchJurisdiction" className="form-control" key={this.state.organization.jurisdiction}
                              name="geoSearch" urlResources={store_service + "/geographicalAreas"}
                              onChange={this.changeInput('jurisdiction_uri')}
                              countryFilter={ {country_uri:this.state.organization.country_uri} /*{country_uri:''}*/ }
                              placeholder={this.state.organization.jurisdiction /*t('my.network.organization.jurisdiction.placeholder')*/}/>
                        </Field>
                  )}
                  <Field name="phone_number">
                     <input className="form-control" id="phone_number" type="text" value={this.state.organization.phone_number}
                              onChange={this.changeInput('phone_number')}/>
                  </Field>
                  <Field name="web_site">
                     <input className="form-control" id="web_site" type="text" value={this.state.organization.web_site}
                     onChange={this.changeInput('web_site')}/>
                  </Field>
                  <Field name="email">
                     <input className="form-control" id="email" type="email" value={this.state.organization.email}
                     onChange={this.changeInput('email')} placeholder={t('my.network.organization.email.placeholder')}/>
                  </Field>

                </div>{/* End of col 1 */}

                <div className="col-sm-2">
                  {/* util/fileuploadinput.jsx.js */}
                  <div className="form-group">
                   <div className="control">
                      <img src={this.state.organization.iconUrl}/>
                      <div className="btn btn-success-inverse btn-edit">
                         <i className="pull-left fa fa-pencil" />
                         <label>{t('my.network.organization.change-icon')}</label>
                         <FileUploadInput className="edit" uploadUrl={"/media/objectIcon/"+this.state.organization.tax_reg_num}
                              success={this.updateUploadedIcon} error={this.error}/>
                      </div>
                   </div>
                  </div>

                </div></div>{/* End of row2 */}
                </div>{/* End of container */}
                <div className="error"><label>{this.state.errorMsg}</label> </div>
                <button key="next" className="control pull-right btn btn-primary-inverse" onClick={this.moveTabRight}>{t('ui.next')}</button>

             </div>
            );
    }
});/** end tab1 */

var isInteger = function(obj){ return (jQuery.isNumeric(obj) && obj.indexOf('.') <0); }

var Tab2 = React.createClass({
    getInitialState: function() { return {organization: this.props.orgData, errors: [] }; },
    moveTabLeft: function() {
        var errors = this.props.valdiate(this.state.organization);
        if(errors[1].length == 0){ this.props.switchTab(this.props.id - 1); }
    },
    changeInput: function (fieldname, isNumericField) {
        return function (event) {
            this.changeInputAddress(fieldname, event.target.value, isNumericField);
        }.bind(this);
    },
    changeInputAddress: function (fieldname, value, isNumericField) {
        var org = this.state.organization;
        if(isNumericField && value !== ''){
           org[fieldname] =  isInteger(value) ? value.trim() : org[fieldname];
        } else {
           org[fieldname] = value;
        }
        this.setState({organization: org});
    },

    render: function() {
        if ($.inArray("icon", this.props.errors) != -1) { iconClassName = iconClassName + " error"; }
        this.state.organization = this.props.orgData;
        var sectorType = getSectorTypeLabel(this.state.organization.sector_type, this.props.typeRestriction);

        return (
             <div id="tab2">
               <div className="container-fluid">
               <div className="row">
               <br/>
               <div className="col-sm-15">
                <Field name="legal_name" class_name={'control-label col-sm-2'} error={$.inArray("name", this.props.errors) != -1}>
                   <input className="form-control" id="legal_name" type="text" value={this.state.organization.legal_name}
                            onChange={this.changeInput('legal_name')} disabled={true}/>
                   <label>{sectorType}</label>

                   <label className="col-sm-offset-3">{t('my.network.organization.in_activity')}: &nbsp;
                      <input id="in_activity" type="checkbox" checked={this.state.organization.in_activity} disabled={true} /></label>
                </Field><br/>
              </div></div> {/*end of col 1 row 1*/}

              <div className="row">
              <div className="col-sm-8">

                  <AddressComponent errors={this.props.errors} addressContainer={this.state.organization}
                      changeInput={this.changeInputAddress} addressType='ORG' disabled={true}/>

                </div> {/* end of col1*/}
                <div className="col-sm-4">
                {/**
                  geo maps
                **/}
                </div>
                </div></div> {/* end of row2 & container*/}

                <button key="next" className="control pull-right btn btn-primary-inverse" onClick={this.moveTabLeft}>{t('ui.previous')}</button>

             </div>
            );
    }
});/** end tab2 */

var AddressComponent = React.createClass({
    //getInitialState: function() { return {geoSearchCity: null }; },
    componentDidMount: function() {
       //if(this.refs.geoSearchCity)this.state.geoSearchCity = this.refs.geoSearchCity;
    },
    changeInput: function (fieldname, isNumericField) {
        changeInput = this.props.changeInput;
        return function (event) {
           if(event.added){
              changeInput(fieldname, event.added.name, isNumericField);
              changeInput(fieldname+"_uri", event.target.value, isNumericField);
           }else if(fieldname === "country"){
               changeInput(fieldname, event.target.selectedOptions[0].label, isNumericField);
               changeInput(fieldname+"_uri", event.target.value, isNumericField);
               //If country has changed, the city is not longer valid
               changeInput("city", "", isNumericField);
               changeInput("city_uri", "", isNumericField);
               if(this.refs.geoSearchCity) this.refs.geoSearchCity.clear(); //works only to remove tags in current component state, geoSelect placeholder still there.
           }else {
               changeInput(fieldname, event.target.value, isNumericField);
           }
        }.bind(this)
    },

    render: function() {
        var address = this.props.addressContainer;
        var addressType = this.props.addressType ? this.props.addressType : '';

        return (
           <div>

              <Field name="street_and_number" error={$.inArray("street_and_number", this.props.errors) != -1}
                        isRequired={( addressType == 'ORG') ? true : false } >
                 <input className="form-control" id="street_and_number" type="text" value={address.street_and_number}
                    onChange={this.changeInput('street_and_number')} />
              </Field>
              <Field name="additional_address_field">
                 <input className="form-control" id="additional_address_field" type="text" value={address.additional_address_field}
                    onChange={this.changeInput('additional_address_field')} />
              </Field>
              { ( addressType !== 'ORG') ? '' : //personal address
                 <Field name="po_box">
                    <input className="form-control" id="po_box" type="text" value={address.po_box}
                       onChange={this.changeInput('po_box')} />
                 </Field>
              }
              <Field name="city" error={$.inArray("city", this.props.errors) != -1} isRequired={true}>
                 <GeoSingleSelect2Component ref="geoSearchCity" className="form-control" name="geoSearchCity" key={address.city}
                     urlResources={store_service + "/dc-cities"}
                     onChange={this.changeInput('city')}   countryFilter={ {country_uri: address.country_uri} }
                     placeholder={address.city} />
              </Field>
              <Field name="zip" class_name_div='col-sm-3' error={$.inArray("zip", this.props.errors) != -1} isRequired={true}>
                 <input className="form-control" id="zip" type="text" maxLength={6} value={address.zip}
                    onChange={this.changeInput('zip', true)} />
              </Field>
              { ( addressType !== 'ORG') ? '' : //personal address
                  <Field name="cedex" class_name_div='col-sm-2'>
                     <input className="form-control" id="cedex" type="cedex" maxLength={3} value={address.cedex}
                        onChange={this.changeInput('cedex', true)} />
                  </Field>
              }
              <Field name="country" class_name_div='col-sm-5' error={$.inArray("country_uri", this.props.errors) != -1} isRequired={true}>
                 <CountrySelect className="form-control" url={store_service + "/dc-countries"} value={address.country_uri}
                     onChange={this.changeInput('country')} disabled={this.props.disabled}/>
              </Field>

    </div>
     );
    }
});
