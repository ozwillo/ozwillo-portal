/** @jsx React.DOM */

// Main modal
var SearchOrganization = React.createClass({
    componentDidMount: function() {
        $(this.refs.modal.getDOMNode()).on("shown.bs.modal", function() {
            $("input", this).first().focus();
        });
    },

    show: function() {
        this.refs.form.resetSearchVals();
        this.refs.form.getProfileInfo();
        this.refs.modal.open();
    },
    close: function () {
        this.refs.modal.close();
        if (this.props.successHandler) {
            this.props.successHandler();
        }
    },
    openCreateOrModify: function(organization){
        if(organization){
            this.close();
            this.refs.createOrgDialog.show(organization);
        }else{
            /* If organization is found in DC and in Kernel, it returns a null value.
             * In this case the user cannot modified it or be assigned, so a msg is shown */
            this.refs.modalError.open();
        }
    },
    searchOrg: function () {
        this.refs.form.searchOrganization();
    },
    render: function() {
        var buttonLabels = {"cancel": t('ui.cancel'), "save": t('ui.search')};
        return (
            <div>
                <Modal ref="modal" title={t('my.network.find-or-create-organization')} successHandler={this.searchOrg} buttonLabels={buttonLabels}>
                    <SearchOrganizationForm ref="form" successHandler={this.openCreateOrModify}/>
                </Modal>
                <CreateOrModifyOrganizationModal ref="createOrgDialog"  successHandler={this.close} />

                <Modal ref="modalError" title={t('ui.something_went_wrong_title')} infobox={true} cancelHandler={null/*this.close()*/} >
                   <div><h5>{t('search.organization.cannot-be-used')}</h5></div>
                </Modal>
            </div>
            );
    }
});

// Form modal
var SearchOrganizationForm = React.createClass({
    getInitialState: function () {
        var sector_type = 'COMPANY'; //default value for the option button
        var tax_reg_num = '';//'0000000000001';//TODO TEST Only, to remove it
        var legal_name  = '';//'IPGARDE2';//TODO TEST Only, to remove it

        return {orgSearchData: {contact_name: '', contact_lastname: '', contact_email: '', sector_type : sector_type, country: '', country_uri: '',
                      legal_name: legal_name, tax_reg_num: tax_reg_num}, errors: [], searching: false,
        };
    },
    getProfileInfo: function(){
        $.ajax({
            url: network_service+'/general-user-info',
            type: 'get',
            contentType: 'json',
            success: function (data) {
                    var state = this.state;
                    state.orgSearchData.contact_name =     data.user_name;
                    state.orgSearchData.contact_lastname = data.user_lastname;
                    state.orgSearchData.contact_email =    data.user_email;
                    state.orgSearchData.country =          data.address.country;
                    this.setState(state);
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    resetSearchVals: function (event) {
        this.state.orgSearchData= {contact_name: '', contact_lastname: '', contact_email: '', sector_type : this.state.orgSearchData.sector_type, country: '', country_uri: '', legal_name: '', tax_reg_num: ''};
    },
    searchOrganization: function (event) {
        if (event) { event.preventDefault(); }
        if (this.state.searching) { return; } /* do nothing if we're already searching... */
        var org = this.state.orgSearchData;
        var errors = [];
        //Contact
        if (!org.contact_name     || org.contact_name.trim() == '')     { errors.push("name"); }
        if (!org.contact_lastname || org.contact_lastname.trim() == '') { errors.push("lastname"); }
        if (!org.contact_email    || org.contact_email.trim() == '')    { errors.push("email"); }
        // Organization
        if (!org.country_uri     || org.country_uri.trim() == '')     { errors.push("country"); }
        if (!org.legal_name  || org.legal_name.trim() == '')  { errors.push("legal_name"); }
        if (!org.tax_reg_num || org.tax_reg_num.trim() == '') { errors.push("tax_reg_num"); }
        if (!org.sector_type || org.sector_type.trim() == '') { errors.push("sector_type"); }

        if (errors.length == 0) {
            this.state.searching = true;

            $.ajax({
                url: network_service + "/search-organization",
                type: 'get',
                contentType: 'json',
                data: this.state.orgSearchData,
                success: function (data) {
                    if (this.props.successHandler) {
                        var organization = data;/* ? data
                                  : jQuery.extend(true, {}, this.state.orgTEST); // Test only - Deep copy of the TestsData to avoid overwriting */

                        var state = {searching: false, errors: []};
                        this.setState(state);

                        this.props.successHandler(organization);
                    }
                }.bind(this),
                error: function (xhr, status, err) {
                    console.error(status, err.toString());
                    var state = this.state;
                    state.errors = ["general"];
                    state.searching = false;
                    this.setState(state);
                }.bind(this)
            });
        } else {
            this.state.errors = errors;
            this.setState(this.state);
        }
    },
    changeInput: function (fieldname) {
        return function (event) {
            var org = this.state.orgSearchData;
            if(fieldname === "country"){
               org[fieldname+"_uri"] = event.target.value;
               org[fieldname] = event.target.selectedOptions[0].label;
            }else if(fieldname === "tax_reg_num" ){
               org[fieldname] = event.target.value.trim();  /*Remove whitespace*/
            }else {org[fieldname] = event.target.value;}
            this.setState({orgSearchData: org, errors: [], searching: false});
        }.bind(this);
    },
    toggleType: function (event) {
        var org = this.state.orgSearchData;
        org.sector_type = event.target.value;
        this.setState({orgSearchData: org, errors: [], searching: false});
    },
    renderLabel: function(htmlFor, class_name, label){
       var cn = ($.inArray(class_name, this.state.errors) != -1 ? 'col-sm-3 control-label error' : 'col-sm-3 control-label');
       return (<label htmlFor={htmlFor} className={cn}>{label}
                  <label className={'error'}>{'*'}</label>
               </label>);
    },

    render: function () {
        //var errorMessage = (<label className="error">{this.props.errors["general"]}</label> );
        return (
            <form onSubmit={this.searchOrganization} className="form-horizontal">
                 <h4>{t('search.contact.title')}</h4>
                 <ContactSearchFormControl renderLabel={this.renderLabel} orgSearchData={this.state.orgSearchData}
                 changeInput={this.changeInput}/>

                  <h4>{t('search.organization.title')}</h4>
                  <OrganizationSearchFormControl errors={this.state.errors} renderLabel={this.renderLabel} orgSearchData={this.state.orgSearchData}
                               changeInput={this.changeInput} toggleType={this.toggleType}/>

                {/*errorMessage*/}
            </form>
            );
    }
});

var ContactSearchFormControl  = React.createClass({
    render: function() {
        return (
           <div className="form-group">
             <div className="form-group">
                {this.props.renderLabel("contact-name", 'name', t('search.contact.name'))}
                <div className="col-sm-8"><input type="text" className="form-control" value={this.props.orgSearchData.contact_name}
                   onChange={this.props.changeInput('contact_name')} maxLength={100}  placeholder={t('search.contact.name')}/></div>
             </div>
             <div className="form-group">
                {this.props.renderLabel("contact-lastname", 'lastname', t('search.contact.lastname'))}
                <div className="col-sm-8"><input type="text" className="form-control" value={this.props.orgSearchData.contact_lastname}
                   onChange={this.props.changeInput('contact_lastname')} maxLength={100} placeholder={t('search.contact.lastname')}/></div>
             </div>
             <div className="form-group">
                {this.props.renderLabel("contact-email", 'email', t('search.contact.email'))}
                <div className="col-sm-8"><input type="text" className="form-control" value={this.props.orgSearchData.contact_email}
                       onChange={this.props.changeInput('contact_email')} maxLength={100} placeholder={t('search.contact.email')}/></div>
             </div>
          </div>
        )
    }
});

var OrganizationSearchFormControl = React.createClass({
    renderType: function () {
        var restriction = this.props.typeRestriction ? this.props.typeRestriction : {company: true, public_body: true};

        var public_body = null;
        if (restriction.public_body) {
            public_body = (
                        <label className="radio-inline col-sm-3">
                            <input type="radio" value="PUBLIC_BODY" checked={this.props.orgSearchData.sector_type == 'PUBLIC_BODY'}
                                 onChange={this.props.toggleType}>{t('search.organization.sector-type.PUBLIC_BODY')}</input>
                        </label>
            );
        }

        var company = null;
        if (restriction.company) {
            company = (
                    <label className="radio-inline col-sm-3">
                        <input type="radio" value="COMPANY" checked={this.props.orgSearchData.sector_type  == 'COMPANY'}
                            onChange={this.props.toggleType}>{t('search.organization.sector-type.COMPANY')}</input>
                    </label>
            );
        }

        var sectorTypeClassName = 'col-sm-3 control-label';
        sectorTypeClassName = ($.inArray('sector_type', this.props.errors) != -1 ? sectorTypeClassName+' error' : sectorTypeClassName);

        return (
            <div className="form-group">
                <label htmlFor="organization-sector-type" className={sectorTypeClassName}>{t('search.organization.sector-type')}</label>
                {public_body}
                {company}
            </div>
        );
    },

    render: function () {
        var label_regNum; 
        switch(this.props.orgSearchData.country){
           case 'България' : label_regNum = t('search.organization.business-id.bg'); break;
           case 'Italia'   : label_regNum = t('search.organization.business-id.it'); break;
           case 'France'   : label_regNum = t('search.organization.business-id.fr'); break;
           case 'España'   : label_regNum = t('search.organization.business-id.es'); break;
           case 'Türkiye'  : label_regNum = t('search.organization.business-id.tr'); break;
           default         : label_regNum = t('search.organization.business-id.en'); break;
        }
        if ( (!this.props.orgSearchData.country_uri || this.props.orgSearchData.country_uri === "") && this.refs.orgCountrySelect){
           this.props.orgSearchData.country_uri = this.refs.orgCountrySelect.getValue(this.props.orgSearchData.country);
        }

        return (
             <div className="form-group">
                {this.renderType()}
                <div className="form-group">
                    {this.props.renderLabel("organization-country-name", 'country', t('search.organization.country'))}
                    <div className="col-sm-5">
                         <CountrySelect ref="orgCountrySelect" className="form-control" url={store_service + "/dc-countries"} defLabel={this.props.orgSearchData.country}
                             onChange={this.props.changeInput('country')} />
                    </div>
                </div>
                <div className="form-group">
                    {this.props.renderLabel("organization-name", 'legal_name', t('search.organization.legal-name'))}
                    <div className="col-sm-8"><input type="text" className="form-control" value={this.props.orgSearchData.legal_name}
                             onChange={this.props.changeInput('legal_name')} maxLength={100}
                             placeholder={t('search.organization.legal-name')}/></div>
                </div>
                <div className="form-group">
                   {this.props.renderLabel("organization-business-id", 'tax_reg_num', label_regNum)}
                   <div className="col-sm-8"><input type="text" className="form-control" value={this.props.orgSearchData.tax_reg_num}
                         onChange={this.props.changeInput('tax_reg_num')} maxLength={20}
                         placeholder={t(label_regNum)}/></div>
                </div>
            </div>
        )
   }
});

//http://stackoverflow.com/questions/25793918/creating-select-elements-in-react-js
/** PROPS: onChange(), url */
var CountrySelect = React.createClass({
    propTypes: { url: React.PropTypes.string.isRequired },
    getInitialState: function() { return { options: [], countries: [] } },
    onChange: function(event) {this.props.onChange(event);},
    componentDidMount: function() {
        //var userCurrentLanguge = currentLanguage;
        if(this.props.url){
            // get country dc data
            $.ajax({
                url: this.props.url,
                type: 'get',
                dataType: 'json',
                data: {q:' '},
                success: function (data) {
                    var areas = data.areas;
                    var options = [{ value: '', label: '' }];
                    areas = areas.filter(function(n){return n !== null; });
                    for (var i = 0; i < areas.length; i++) {
                       options.push({ value: areas[i].uri, label: areas[i].name })
                    }
                    this.state.countries = options;
                    this.successHandler(options); //set the list of countries
                }.bind(this),
                error: function (xhr, status, err) {
                    console.error(status, err.toString());
                }.bind(this)
            });
        }/*else{ // for TEST only
            var options = [
                   { value: '',       label: ''       },
                   { value: 'France', label: 'France' },
                   { value: 'Italy',  label: 'Italy'  },
                   { value: 'Spain',  label: 'Spain'  },
                   { value: 'Turkey', label: 'Turkey' }
               ];
            this.successHandler(options);
        }*/
    },
    successHandler: function(data) {
        // assuming data is an array of {name: "foo", value: "bar"}
        for (var i = 0; i < data.length; i++) {
            var option = data[i];
            this.state.options.push( <option className="action-select-option" key={i} value={option.value}>{option.label}</option> );
        }
        this.setState(this.state);
        this.forceUpdate();
    },
    getValue: function(label) {
       if (!label || label !== "") {
          for (var i = 0; i < this.state.countries.length; i++) {
             if (this.state.countries[i].label === label) {
               return this.state.countries[i].value; break;
             }
          }
       }
       return null;
    },

    render: function() {
        var label = this.props.defLabel;
        if(label && (!this.props.value || this.props.value === "") ){
           //This is to load the country_uri that couldn't be set |
           this.props.value = (this.getValue(label)); // decodeURIComponent()
        }
        // the parameter "value=" is selected option. Default selected option can either be set here. Using browser-base fonctuion decodeURIComponent()
        return ( <select className="btn btn-default dropdown-toggle" onChange={this.onChange}
                           value={this.props.value} disabled={this.props.disabled}>
                    {this.state.options}
                 </select>
        );
    }
});
