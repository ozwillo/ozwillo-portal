/** @jsx React.DOM */

var AppModal = React.createClass({
    getInitialState: function () {
        return {
            app: { rating: 0, rateable: true, tos: '', policy: '', longdescription: '', screenshots: null },
            orgs: [],
            selectedOrg: jQuery.extend(true, {}, default_org_data.organization),
            createOrg: false,
            buying: false,
            installing: false,
            isInstalled: false,
            error: false
        };
    },
    componentDidMount: function () {
        $(this.refs.modal.getDOMNode()).on("hide.bs.modal", function (event) {
            history.pushState({}, "store", store_root);
        }.bind(this));
    },
    componentDidUpdate: function () {
        var desc = $(this.getDOMNode()).find(".app-description table");
        desc.addClass("table table-bordered table-condensed table-striped");
    },
    loadApp: function () {
        $.ajax({
            url: store_service + "/details/" + this.props.app.type + "/" + this.props.app.id,
            type: 'get',
            dataType: 'json',
            success: function (data) {
                var state = this.state;
                state.app = data;
                this.setState(state);
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    loadOrgs: function () {
        $.ajax({
            url: store_service + "/organizations/" + this.props.app.type + "/" + this.props.app.id,
            type: 'get',
            dataType: 'json',
            success: function (data) {
                var state = this.state;
                state.orgs = data;
                this.setState(state);
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    open: function () {
        this.setState(this.getInitialState());
        var href = store_root + "/" + this.props.app.type + "/" + this.props.app.id;
        if (typeof history.pushState == "function") {
            history.pushState({}, "application", href);
        }

        this.loadApp();
        if (logged_in) { this.loadOrgs(); }

        this.refs.modal.open();
    },
    close: function(){
        if(this.state.isInstalled){
           // redirect to the Dashboard
           window.location = "/my/dashboard";
        }
    },
    doInstallApp: function (organization, updateUserData) {
        var state = this.state;
        state.installing = false;
        state.buying = true; //set it to display the spinner until any below ajax response is received.

        this.setState(state);

        var request;
        if(updateUserData){
           request = updateUserData;
           request.appId = this.props.app.id;
           request.appType = this.props.app.type;
        }
        else{request = {appId: this.props.app.id, appType: this.props.app.type};}
        if (organization) {
           request.organizationId = organization;
        }

        $.ajax({
            url: store_service + "/buy",
            type: 'post',
            data: JSON.stringify(request),
            contentType: 'application/json',
            success: function (data) {
                if (data.success) {
                    this.displaySucessfulInstallForm();
                } else {
                    var state = this.state;
                    state.buying = false;
                    state.error = true;
                    this.setState(state);
                }
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
                var state = this.state;
                state.buying = false;
                this.setState(state);
            }.bind(this)
        });

    },
    orgCreated: function (org) {
        var state = this.state;
        state.createOrg = false;
        if (org) { this.doInstallApp(org.id); }
        this.setState(state);
    },
    doCreateOrg: function () {
        if (this.refs.tabbedForm){this.refs.tabbedForm.createOrModifOrg();}
    },
    cancelCreateOrg: function () {
        var state = this.state;
        state.createOrg = false;
        this.setState(state);
    },
    rateApp: function (rate) {
        $.ajax({
            url: store_service + "/rate/" + this.props.app.type + "/" + this.props.app.id,
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify({rate: rate}),
            success: function () {
                var state = this.state;
                state.app.rateable = false;
                state.app.rating = rate;
                this.setState(state);
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    displayInstallForm: function(){
        var state = this.state;
        state.installing = true;
        this.setState(state);
    },
    displaySucessfulInstallForm: function(){
        var state = this.state;
        state.buying = false;
        state.isInstalled = true;
        this.setState(state);
    },
    continueInstallProcess: function(){ /* set data and display CreateOrgForm OR Install for personal apps */
        var state = this.state;
        state.installType = this.refs.instalForm.getInstallType();

        // preparing data to be transmitted
        var installData = this.refs.instalForm.getInstallData();

        if(state.installType === 'ORG'){

           var orgSearchData = this.refs.instalForm.getOrgSearchData();
           orgSearchData.contact_name     = installData.contact.contact_name;
           orgSearchData.contact_lastname = installData.contact.contact_lastname;
           orgSearchData.contact_email    = installData.contact.contact_email;
           //orgSearchData.country = orgSearchData.country_uri;

           state.selectedOrg.typeInstallOrg = orgSearchData.typeInstallOrg;

           if(orgSearchData.typeInstallOrg === 'NEW-ORGS'){
               $.ajax({
                   url: network_service + "/search-organization",
                   type: 'get',
                   contentType: 'json',
                   data: orgSearchData,
                   success: function (data) {
                      // Verify that the received object is actually the one we requested (a brand new one)
                      // after the server validated that it doesn't exist in Datacore / Kernel
                      if(data && !data.exist
                              && (data.sector_type === orgSearchData.sector_type)
                              && (data.tax_reg_num === orgSearchData.tax_reg_num)
                              && (data.legal_name  === orgSearchData.legal_name)){
                         state.installing = false;
                         state.createOrg = true;
                         state.selectedOrg = data;
                         state.errors = [];
                         this.setState(state);
                      }else{
                         this.refs.modalError.open();
                         this.setState(state);
                      }
                   }.bind(this),
                   error: function (xhr, status, err) {
                       console.error(status, err.toString());
                       var state = this.state;
                       state.errors = ["general"];
                       this.setState(state);
                   }.bind(this)
               });
           } else {
               state.installing = false;
               this.doInstallApp(orgSearchData.selectedOrgId, orgSearchData);
           }
        } else { // PERSONAL
            var orgSearchData = this.refs.instalForm.state.installData.address;//this.refs.addressComponent.props.addressContainer;
            orgSearchData.contact_name     = installData.contact.contact_name;
            orgSearchData.contact_lastname = installData.contact.contact_lastname;
            orgSearchData.contact_email    = installData.contact.contact_email;

            // call method to update data user while calling the Installation Process
            this.doInstallApp(null, orgSearchData);
        }
    },
    orgTypeRestriction: function () {
        return {
            company: this.props.app.target_companies,
            public_body: this.props.app.target_publicbodies
        };
    },
    renderCreateNewOrganization: function () {
        return (
            <div>
                <h3>{ !this.state.selectedOrg.exist ? t('create-new-org') : t('modify-org')}</h3>
                <CreateOrModifyOrganizationForm ref="tabbedForm" successHandler={this.orgCreated}
                     typeRestriction={this.orgTypeRestriction()} organization={this.state.selectedOrg}/>

                    <div className="col-sm-4 col-sm-offset-8">
                        <a className="btn btn-default" onClick={this.cancelCreateOrg}>{t('ui.cancel')}</a>
                        <a className="btn btn-primary" onClick={this.doCreateOrg}>{t('create')}</a>
                    </div>

            </div>
            );
    },
    renderBuying: function () {
        return ( <h3> <i className="fa fa-spinner fa-spin"></i> {t('buying')}</h3> );
    },
    renderInstallingForm: function(){
        return (<InstallForm ref='instalForm'
                     installApp={this.installApp} url={this.state.app.serviceUrl}
                     app={this.props.app}  orgs={this.state.orgs}
                     continueInstallProcess={this.continueInstallProcess}
                />);
    },
    renderAppDescription: function(){
       return (<AppDescriptionComponent app={this.props.app} stateApp={this.state.app}
                     rateApp={this.rateApp} onInstallButton={this.displayInstallForm} error={this.state.error}/>);
    },
    renderSucessfulInstallationForm: function(){
        return (<div>
                 <div className='form-horizontal' >
                    <i id="success-app-install" className="fa fa-check pull-left col-sm-offset-1"></i>
                    <div className='form-group'>
                       <h5 className="col-sm-offset-2">{t('install.org.success-msg-1')}</h5><br/>
                    </div>
                    { !this.props.app.paid ? '' :
                       <div className='form-group'>
                          <h5 className="col-sm-offset-2">{t('install.org.success-msg-2')}</h5><br/>
                       </div>
                    }
                    { this.state.installType === 'PERSONAL' ? '' :
                       <div className='form-group'>
                          <h5 className="col-sm-offset-2">{t('install.org.success-msg-3')}</h5><br/>
                       </div>
                    }
                    <div className='form-group'>
                       <h5 className="col-sm-offset-2">{t('install.org.success-msg-4')}</h5><br/>
                    </div>
                 </div>
                </div>
        );
     },

    render: function () {
        var content = null;
        if (this.state.buying){                content = this.renderBuying();
        } else if (this.state.createOrg){      content = this.renderCreateNewOrganization();
        } else if (this.state.installing){     content = this.renderInstallingForm();
        } else if (this.state.isInstalled){    content = this.renderSucessfulInstallationForm();
        } else {                               content = this.renderAppDescription();
        }

        return (
          <div>
            <Modal ref="modal" large={true} infobox={true} title={this.props.app.name} cancelHandler={this.close}>
               {content}
            </Modal>
            <Modal ref="modalError" title={t('ui.something_went_wrong_title')} infobox={true} cancelHandler={null} >
               <div><h5>{t('search.organization.cannot-be-used')}</h5></div>
            </Modal>
          </div>);
    }
});


var converter = new Showdown.converter({extensions: ['table']});

/** PROPS: app{}, stateApp{}, rateApp(), onInstallButton(), errors[] */
var AppDescriptionComponent =  React.createClass({
        render: function () {
            var stateApp = this.props.stateApp;

            var carousel = (stateApp.screenshots && stateApp.screenshots.length > 0)
                ? ( <div className="row">
                        <Carousel images={stateApp.screenshots} />
                    </div> )
                : null;

            var error = (this.props.error)
                ? ( <div className="alert alert-danger alert-dismissible" role="alert">
                        <button type="button" className="close" data-dismiss="alert">
                            <span aria-hidden="true">&times;</span>
                            <span className="sr-only">{t('ui.close')}</span>
                        </button>
                        <strong>{t('sorry')}</strong> {t('could-not-install-app')}
                    </div> )
                : null;

            var rateInfo = null;
            if (logged_in && !stateApp.rateable) { rateInfo =  (<p>{t('already-rated')}</p>); }

            var description = converter.makeHtml(stateApp.longdescription);

            var launchOrInstallButton;
            if (this.props.app.type == "service" && this.props.app.installed) {
               if (this.props.stateApp && this.props.stateApp.serviceUrl) {
                       launchOrInstallButton = <a className="btn btn-primary" href={this.props.stateApp.serviceUrl} target="_new">{t('launch')}</a>;
                   /*} else {
                       launchOrInstallButton = (<label >{t('installed')}</label>);
                   }*/
               } else {
                    launchOrInstallButton = (<label > <i className="fa fa-spinner fa-spin"></i> </label> );
               }
            }else{
                var installButton = !logged_in
                    ? (<a className="btn btn-primary-inverse"
                         href={store_root + "/login?appId=" + this.props.app.id + "&appType=" + this.props.app.type}>{t('install')}</a>)
                    : (<button className="btn btn-primary" onClick={this.props.onInstallButton} >{t('install')}</button>);
                launchOrInstallButton = installButton
            }


            return (
                <div>
                    <div className="row">
                        <div className="col-sm-1">
                            <img src={this.props.app.icon} alt={this.props.app.name} />
                        </div>
                        <div className="col-sm-7">
                            <div>
                                <p className="appname">{this.props.app.name}</p>
                                <p>{t('by')} {this.props.app.provider}</p>
                            </div>
                        </div>
                        <div className="col-sm-4 center-container install-application">
                            {launchOrInstallButton}
                        </div>
                    </div>
                    <div className="row">
                        <Rating rating={stateApp.rating} rateable={stateApp.rateable} rate={this.props.rateApp} />
                        {rateInfo}
                    </div>
                    {error}
                    {carousel}
                    <div className="row">
                        <div className="col-md-6 app-description" dangerouslySetInnerHTML={{__html: description}}></div>
                        <div className="col-md-6">
                            <p>{t('agree-to-tos')}</p>
                            <p><a href={stateApp.tos} target="_new">{t('tos')}</a></p>
                            <p><a href={stateApp.policy} target="_new">{t('privacy')}</a></p>
                        </div>
                    </div>
                </div>
                );
        }
});

// INSTALLATION PROCESS

/** PROPS: app{}, errors[], url, orgs[], continueInstallProcess() */
var InstallForm =  React.createClass({
    getInitialState: function(){
        this.getProfileInfo();
        return ( {installType: 'PERSONAL',  errors: [],
                  installData: {
                     contact: {contact_name:'', contact_lastname:'', contact_email:''},
                     address:{exist:false, street_and_number:'', additional_address_field:'', city:'', zip:'', country_uri:'', cedex:'', po_box:''}
                  }
        })
    },
    getProfileInfo: function(){
        $.ajax({
            url: network_service+'/general-user-info',
            type: 'get',
            contentType: 'json',
            success: function (data) {
                    var state = this.state;
                    state.installData.contact.contact_name =      data.user_name;
                    state.installData.contact.contact_lastname =  data.user_lastname;
                    state.installData.contact.contact_email =     data.user_email;
                    if(data.address){
                       state.installData.address.street_and_number = data.address.street_address;
                       state.installData.address.city =              data.address.locality;
                       state.installData.address.zip =               data.address.postal_code;
                       state.installData.address.country =           data.address.country;
                    }
                    this.setState(state);
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    getInstallType: function(){
        var installTypeRestrictions = {personal: this.hasCitizens(), org: this.hasOrganizations()};
        if(this.state.installType === 'PERSONAL' && installTypeRestrictions.personal === false){ return 'ORG';}
        else if(this.state.installType === 'ORG' && installTypeRestrictions.org      === false){ return 'PERSONAL';}
        else{ return this.state.installType;}
    },
    getInstallData: function(){return this.state.installData},
    getOrgSearchData: function(){ return this.refs.setOrgComponent.getOrgSearchData()},
    isOnlyForCitizens: function () {
        return (  this.props.app.target_citizens
                     && !(this.props.app.target_companies) && !(this.props.app.target_publicbodies) );
    },
    hasCitizens: function () { return this.props.app.target_citizens; },
    hasOrganizations: function () { return (this.props.app.target_companies) || (this.props.app.target_publicbodies); },
    toggleType: function(event){
        var installType = this.state.installType;
        installType = event.target.value;
        this.setState({installType: installType});
    },
    renderInstallType: function () {
        var installTypeRestrictions = {personal: this.hasCitizens(), org: this.hasOrganizations()};
        var installType = this.getInstallType();

        var personal = !installTypeRestrictions.personal ? null
                     : (<div className='radio col-sm-offset-2'>  <label>
                            <input type="radio" value="PERSONAL" checked={installType == 'PERSONAL'}
                                 onChange={this.toggleType}>{t('install.org.type.PERSONAL')}</input>
                        </label>  </div>
                       );


        var org = !installTypeRestrictions.org ? null
                : (<div className='radio col-sm-offset-2'>  <label>
                       <input type="radio" value="ORG" checked={installType == 'ORG'}
                            onChange={this.toggleType}>{t('install.org.type.ORG')}</input>
                   </label>  </div>
                  );

        var sectorTypeClassName = 'col-sm-3 control-label ';
        sectorTypeClassName = ($.inArray('sector_type', this.props.errors) != -1 ? sectorTypeClassName+' error' : sectorTypeClassName);

        return (
            <div className="form-group">
                {/*<label htmlFor="install-type" className={sectorTypeClassName}>{t('install.personal.type')}</label>*/}
                {personal}
                {org}
            </div>
        );
    },
    renderLabel: function(htmlFor, class_name, label){
        var cn = ($.inArray(class_name, this.state.errors) != -1 ? 'col-sm-3 control-label error' : 'col-sm-3 control-label');
        return (<label htmlFor={htmlFor} className={cn}>{label}
                   <label className={'error'}>{'*'}</label>
                </label>);
    },
    changeInputContact: function (fieldname) {
         return function (event) {
             var org = this.state.installData;
             org.contact[fieldname] = event.target.value;
             this.setState({installData: org, errors: []});
         }.bind(this);
    },
    changeInputAddress: function (fieldname, value, isNumericField) {
             var org = this.state.installData;
             if(isNumericField && value !== ''){
                org.address[fieldname] =  isInteger(value) ? value.trim() : org.address[fieldname];
             } else {
                org.address[fieldname] = value;
             }
             this.setState({installData: org, errors: []});
    },
    validateContact: function(){
        var errs = [];
        var contact = this.state.installData.contact;
        if (!contact.contact_name     || contact.contact_name.trim() == '')       { errs.push('name'); }
        if (!contact.contact_lastname || contact.contact_lastname.trim() == '')   { errs.push('lastname'); }
        if (!contact.contact_email    || contact.contact_email.trim() == '')      { errs.push('email'); }

        this.setState({errors: errs});
        if (errs.length > 0) {return false;}else{return true;}
    },
    validateAddress: function(){
        var errs = this.state.errors;
        var address = this.state.installData.address;
        if (!address.city_uri    || address.city.trim() == '')       { errs.push('city_uri'); errs.push('city');}
        if (!address.zip         || address.zip.trim() == '')        { errs.push('zip'); }
        if (!address.country_uri || address.country_uri.trim() == ''){ errs.push('country_uri'); }

        this.setState({errors: errs});
        if (errs.length > 0) {return false;}else{return true;}
    },
    validateAndContinue: function(){
        var installType = this.getInstallType();
        if (installType === 'PERSONAL'){
           if ( (this.validateContact() && this.validateAddress()) ){
              this.props.continueInstallProcess();
           }
        }else if(installType === 'ORG' && this.validateContact() && (this.refs.setOrgComponent && this.refs.setOrgComponent.validate()) ){
           this.props.continueInstallProcess();
        }
    },

    render: function () {
        var installType = this.getInstallType();

        return (
            <div className='form-horizontal'>
                {this.renderInstallType()}
                <h4>{t('search.contact.title')}</h4>
                <ContactSearchFormControl renderLabel={this.renderLabel} orgSearchData={this.state.installData.contact}
                    changeInput={this.changeInputContact} />

                { (installType === 'PERSONAL') || (this.props.app.type === "service")
                    ? (<div>
                          <h4>{t('search.contact.address.title')}</h4>
                          <AddressComponent ref='addressComponent' errors={this.state.errors} addressContainer={this.state.installData.address}
                                changeInput={this.changeInputAddress} />
                       </div>)
                    : (<div>
                           <h4>{t('search.organization.title')}</h4>
                           <SetOrganizationComponent ref='setOrgComponent' url={this.props.url} app={this.props.app} orgs={this.props.orgs}
                                isOnlyForCitizens={this.isOnlyForCitizens} onChangeSelectedOrg={this.onChangeSelectedOrg}/>
                       </div>)
                }

                <a className="btn btn-primary pull-right" onClick={this.validateAndContinue}>{t('ui.next')}</a>
            </div>
        );
    }
});

/** PROPS: app{}, orgs[], url, isOnlyForCitizens() */
SetOrganizationComponent = React.createClass({
    getInitialState: function() { return {
                 orgSearchData: {contact_name: '', contact_lastname: '', contact_email: '', sector_type : '', country: 'France',
                       legal_name: '', tax_reg_num: '', typeInstallOrg: 'NEW-ORGS', selectedOrgId:''}, errors: []
    }},
    getOrgSearchData: function(){ return this.state.orgSearchData},
    orgTypeRestriction: function () {
        return {
            company: this.props.app.target_companies,
            public_body: this.props.app.target_publicbodies
        };
    },
    onChangeOrgInput: function(fieldname){
        return function (event) {
            var org = this.state.orgSearchData;
            if(fieldname === "country"){
               org[fieldname+"_uri"] = event.target.value;
               org[fieldname] = event.target.selectedOptions[0].label;
            }else if(fieldname === "tax_reg_num" ){
               org[fieldname] = ''+event.target.value.replace(/\s+/g, ''); /*Remove whitespace avoiding setting undefined*/
            }else{ org[fieldname] = event.target.value; }
            this.setState({orgSearchData: org, errors:[]});
        }.bind(this);
    },
    toggleInstallOrgType: function (event) {
        var org = this.state.orgSearchData;
        org.typeInstallOrg = event.target.value;
        org.selectedOrgId = this.props.orgs[0].id;
        this.setState({orgSearchData: org, errors:[]});
  },
    toggleSectorType: function (event) {
        var org = this.state.orgSearchData;
        org.sector_type = event.target.value;
        this.setState({orgSearchData: org, errors:[]});
    },
    renderOrganizations: function(){
        var opts = [];
        this.props.orgs.map(function (org) {
             opts.push(<option key={org.id} className="action-select-option" value={org.id}>{org.name}</option>);
        }.bind(this));

        return (<select className="btn btn-default dropdown-toggle" onChange={this.onChangeOrgInput('selectedOrg')}>{opts}</select>);
    },
    validate: function(){
       var state = this.state;
       var errors = [];
       var orgSearchData = state.orgSearchData;
       if(orgSearchData.typeInstallOrg === 'EXISTING-ORGS'){
           if(orgSearchData.selectedOrgId.trim() === '') { errors.push('typeInstallOrg'); }
       }else{
           if (!orgSearchData.sector_type || orgSearchData.sector_type.trim() == '') {errors.push('sector_type')}
           if (!orgSearchData.country_uri || orgSearchData.country_uri.trim() == '') {errors.push('country')}
           if (!orgSearchData.legal_name  || orgSearchData.legal_name.trim() == '')  {errors.push('legal_name')}
           if (!orgSearchData.tax_reg_num || orgSearchData.tax_reg_num.trim() == '') {errors.push('tax_reg_num')}
       }

       state.errors = errors;
       this.setState(state);
       if (state.errors.length > 0) { return false; } else {return true;}
    },
    renderLabel: function(htmlFor, class_name, label){
        var cn = ($.inArray(class_name, this.state.errors) != -1 ? 'col-sm-3 control-label error' : 'col-sm-3 control-label');
        return (<label htmlFor={htmlFor} className={cn}>{label}
                   <label className={'error'}>{'*'}</label>
                </label>);
    },

    render: function(){
            return (<div className="form-group">
                       { (!this.props.isOnlyForCitizens() && this.props.orgs && this.props.orgs.length >0 )
                            ?
                               <div className="radio col-sm-offset-2">
                                  <label>
                                     <input type="radio" value="EXISTING-ORGS" checked={this.state.orgSearchData.typeInstallOrg == 'EXISTING-ORGS'}
                                        onChange={this.toggleInstallOrgType}/>{t('search.organization.selection.existing')} &nbsp;&nbsp;&nbsp;
                                  </label>
                                  { this.renderOrganizations()}
                               </div>
                            : ''
                       }
                       <div className="radio col-sm-offset-2">
                           <label>
                               <input type="radio" value="NEW-ORGS" checked={this.state.orgSearchData.typeInstallOrg == 'NEW-ORGS'}
                                   onChange={this.toggleInstallOrgType} />{t('search.organization.selection.new')}
                           </label>
                           <OrganizationSearchFormControl errors={this.state.errors} renderLabel={this.renderLabel} typeRestriction={this.orgTypeRestriction()}
                                orgSearchData={this.state.orgSearchData} changeInput={this.onChangeOrgInput} toggleType={this.toggleSectorType}/>
                       </div>
                    </div>
            );
    }
});
//END NEW INSTALL PROCESS

/** PROPS: images */
var Carousel = React.createClass({
    getInitialState: function () { return {index: 0}; },
    back: function () {
        var index = this.state.index;
        index = Math.max(0, index - 1);
        this.setState({index: index});
    },
    forward: function () {
        var index = this.state.index;
        index = Math.min(this.props.images.length, index + 1);
        this.setState({index: index});
    },

    render: function () {
        if (!this.props.images) { return null; }

        var back = null;
        if (this.state.index > 0) {
            back = <a className="back" onClick={this.back}>
                <i className="fa fa-chevron-left"></i>
            </a>;
        }

        var forward = null;
        if (this.state.index < this.props.images.length - 1) {
            forward = <a className="forward" onClick={this.forward}>
                <i className="fa fa-chevron-right"></i>
            </a>;
        }

        return (
            <div className="carousel">
                {back}
                <img src={this.props.images[this.state.index]} alt={this.state.index}/>
                {forward}
            </div>
            );
    }
});

/** PROPS: rate(), rating, rateable, */
var Rating = React.createClass({
    getInitialState: function () { return {}; },
    startEditing: function () {
        if (this.props.rateable) {
            this.setState({editing: true, rating: 0});
        }
    },
    stopEditing: function () {
        this.setState({editing: false, rating: 0});
    },
    rate: function () {
        if (this.props.rateable) {
            this.props.rate(this.state.rating);
        }
    },
    mouseMove: function (event) {
        if (this.state.editing) {
            var rect = this.getDOMNode().getBoundingClientRect();
            var x = Math.floor(8 * (event.clientX - rect.left) / (rect.width)) / 2;
            if (rect.right - event.clientX < 5) {
                // the last 5 pixels are a cheat for the max grade
                x = 4;
            }
            this.setState({editing: true, rating: x});
        }
    },
    render: function () {
        var className;
        var rating;
        if (this.state.editing) {
            rating = this.state.rating;
        } else {
            rating = this.props.rating;
        }
        var rt = rating < 1 ? "0" + (rating * 10) : (rating * 10);
        className = "rating-static rating-" + rt;
        return (
            <div className={className}
                onMouseEnter={this.startEditing}
                onMouseLeave={this.stopEditing}
                onMouseMove={this.mouseMove}
                onClick={this.rate}>
            </div>
        );
    }
});
