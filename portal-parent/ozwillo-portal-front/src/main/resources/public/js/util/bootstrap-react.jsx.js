/** @jsx React.DOM */

/**
 * Bootstrap Modal encapsulation.
 *
 * Expected props:
 *  - title - String (will be shown as title)
 *  - successHandler (optional) - callback that is called on validation (not required if infobox is true)
 *  - cancelHandler (optional) - callback that is called on cancel
 *  - buttonLabels (optional) - a map of strings with keys "save", "cancel". By default will map to t('ui.save'), t('ui.cancel')
 *  - saveButtonClass (optional) - a additional CSS class to apply to the "save" button (defaults to oz-btn-save)
 *  - large (optional) - if true, will use the modal-lg class on the dialog
 *  - infobox (optional) - if true, will display only a single inverted OK button (label key = 'ok') rather than save / cancel
 *                         also, successHandler has no meaning in that context.
 * Children as content.
 * Open explicitly by calling the open() method. The close() method is also available.
 */
var Modal = React.createClass({
    propTypes: {
        title: React.PropTypes.string.isRequired,
        successHandler: React.PropTypes.func,
        cancelHandler: React.PropTypes.func,
        buttonLabels: React.PropTypes.object,
        saveButtonClass: React.PropTypes.string,
        large: React.PropTypes.bool,
        infobox: React.PropTypes.bool
    },
    componentDidMount: function () {
        $(this.getDOMNode()).modal({show: false});
        if (this.props.cancelHandler) {
            var handler = this.props.cancelHandler;
            $(this.getDOMNode()).on("hide.bs.modal", function () {
                handler();
            });
        }
    },
    componentWillUnmount: function () {
        $(this.getDOMNode()).off('hidden');
    },
    close: function (event) {
        $(this.getDOMNode()).modal('hide');
        if (this.props.cancelHandler) {
            this.props.cancelHandler(event);
        }
    },
    open: function () {
        $(this.getDOMNode()).modal('show');
    },
    render: function () {

        var buttons;

        if (this.props.infobox) {
            var label;
            if (this.props.buttonLabels) {
                label = this.props.buttonLabels["ok"];
            } else {
                label = t('ui.close');
            }
            buttons = [
                <button type="button" key="close" className="btn btn-default-inverse" onClick={this.close}>{label}</button>
            ];
        } else {
            var cancelLabel, saveLabel;
            if (this.props.buttonLabels) {
                cancelLabel = this.props.buttonLabels["cancel"];
                saveLabel = this.props.buttonLabels["save"];
            } else {
                cancelLabel = t('ui.cancel');
                saveLabel = t('ui.save');
            }
            var saveButtonClass = this.props.saveButtonClass ? "btn " + this.props.saveButtonClass : "btn oz-btn-save";

            buttons = [
                <button type="button" key="cancel" className="btn oz-btn-cancel" onClick={this.close}>{cancelLabel}</button>,
                <button type="submit" key="success" className={saveButtonClass} onClick={this.props.successHandler}>{saveLabel}</button>
            ];
        }

        var className = this.props.large ? "modal-dialog modal-lg" : "modal-dialog";

        return (
            <div className="modal fade oz-simple-modal" tabIndex="-1" role="dialog" aria-labelledby="modalLabel">
                <div className={className} role="document">
                    <div className="modal-content">
                        <div className="modal-header">
                            <button type="button" className="close" data-dismiss="modal" aria-label="Close" onClick={this.close}>
                                <span aria-hidden="true"><img src={image_root + "new/cross.png"} /></span>
                            </button>
                            <h4 className="modal-title" id="modalLabel">{this.props.title}</h4>
                        </div>
                        <div className="modal-body">
                            {this.props.children}
                        </div>
                        <div className="modal-footer">
                            {buttons}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
});

/**
 * Bootstrap Modal encapsulation with a form inside.
 * Expected props:
 *  - title - String (will be shown as title)
 *  - successHandler - callback that is called on validation
 *  - cancelHandler - callback that is called on cancel
 *  - buttonLabels (optional) - a map of strings with keys "save", "cancel". By default will map to t('ui.save'), t('ui.cancel')
 *
 * Children as form fields.
 * Open explicitly by calling the open() method. The close() method is also available.
 */
var ModalWithForm = React.createClass({
    propTypes: {
        title: React.PropTypes.string.isRequired,
        successHandler: React.PropTypes.func.isRequired,
        cancelHandler: React.PropTypes.func,
        buttonLabels: React.PropTypes.object
    },
    componentDidMount: function () {
        $(this.getDOMNode()).modal({show: false});
        if (this.props.cancelHandler) {
            var handler = this.props.cancelHandler;
            $(this.getDOMNode()).on("hide.bs.modal", function () {
                handler();
            });
        }
    },
    componentWillUnmount: function () {
        $(this.getDOMNode()).off('hidden');
    },
    close: function (event) {
        $(this.getDOMNode()).modal('hide');
        if (this.props.onClose) {
            this.props.onClose(event);
        }
    },
    open: function () {
        $(this.getDOMNode()).modal('show');
    },
    render: function () {

        var buttons;
        var cancelLabel, saveLabel;
        if (this.props.buttonLabels) {
            cancelLabel = this.props.buttonLabels["cancel"];
            saveLabel = this.props.buttonLabels["save"];
        } else {
            cancelLabel = t('ui.cancel');
            saveLabel = t('ui.save');
        }

        buttons = [
            <button type="button" key="cancel" className="btn oz-btn-cancel" onClick={this.close}>{cancelLabel}</button>,
            <button type="submit" key="success" className="btn oz-btn-save" onClick={this.props.successHandler}>{saveLabel}</button>
        ];

        return (
            <div className="modal fade" tabIndex="-1" role="dialog" aria-labelledby="modalLabel">
                <div className="modal-dialog" role="document">
                    <div className="modal-content">
                        <div className="modal-header">
                            <button type="button" className="close" data-dismiss="modal" aria-label="Close" onClick={this.close}>
                                <span aria-hidden="true"><img src={image_root + "new/cross.png"} /></span>
                            </button>
                            <h4 className="modal-title" id="modalLabel">{this.props.title}</h4>
                        </div>
                        <div className="modal-body">
                            <form className="form-horizontal">
                                {this.props.children}
                            </form>
                        </div>
                        <div className="modal-footer">
                            {buttons}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
});

/**
 * Not really Bootstrap, but it's Twitter anyway...
 * Creates a simple typeahead
 * input props:
 *  - source: a function(query, cb)
 *  - onSelect: a callback that is called when an item is selected
 */
var Typeahead = React.createClass({
    propTypes: {
        source: React.PropTypes.func.isRequired,
        onSelect: React.PropTypes.func.isRequired,
        onChange: React.PropTypes.func,
        display: React.PropTypes.string.isRequired,
        suggestionTemplate: React.PropTypes.func.isRequired,
        fieldId: React.PropTypes.string.isRequired,
        placeholder: React.PropTypes.string,
        minLength: React.PropTypes.number
    },
    componentDidMount: function () {

        var minLength = this.props.minLength !== undefined ? this.props.minLength : 3;
        $(this.getDOMNode()).typeahead({
            minLength: minLength,
            highlight: true,
            hint: false,
            autoselect: false
        }, {
            source: this.props.source,
            display: this.props.display,
            templates: {
                suggestion: this.props.suggestionTemplate
            },
            limit: 10
        }).on("typeahead:selected", function (event, selected) {
            this.props.onSelect(selected, $(this.getDOMNode()));
        }.bind(this))
    },
    reset: function() {
        $(this.getDOMNode()).typeahead('val', '');
    },
    render: function () {
        return (
            <input className="form-control typeahead" onChange={this.props.onChange} id={this.props.fieldId} type="text"
                   size="40" placeholder={this.props.placeholder} autoComplete="off" ></input>
        );
    }
});

/**
 * Not a Bootstrap popover button (can't deal with the magic...)
 * Usage:
 *  <MyPop className="btn btn-primary" label="Click me!">
 *      (content which can include React components, yeah)
 *  </MyPop>
 */
var MyPop = React.createClass({
    getInitialState: function () {
        return {open: false, offset: 0};
    },
    toggle: function (event) {
        this.setState({open: !this.state.open, offset: $(event.target).outerWidth()});
    },
    renderChildren: function () {
        if (this.state.open) {
            var margin = (600 - this.state.offset) / 2;
            return (
                <div className="popup" style={{"margin-left": "-" + margin + "px"}}>
                    <div className="arrow"></div>
                    <div className="popup-content">
          {this.props.children}
                    </div>
                </div>
                );
        } else return null;
    },
    render: function () {
        var children = this.renderChildren();
        return (
            <div>
                <a className={this.props.className} onClick={this.toggle}>{this.props.label}</a>
        {children}
            </div>
            );
    }
});


/**
 * Bootstrap popover button
 * Usage:
 *    <PopoverButton className="btn btn-primary" label="Click me" title="optional">
 *      <p>This is my popover content...</p>
 *      <ReactComponent name="and it can also contain React components but it doesn't work nice" />
 *    </Popover>
 */
var PopoverButton = React.createClass({
    componentDidMount: function () {
        var html = $(this.getDOMNode()).children("div").html();
        $(this.getDOMNode()).children("div").hide();
        $(this.getDOMNode()).children("button").popover({
            title: this.props.title,
            content: html,
            html: true,
            placement: "bottom",
            trigger: "click"
        });
    },
    render: function () {
        return (
            <div>
                <button className={this.props.className}>{this.props.label}</button>
                <div className="popover-content">
        {this.props.children}
                </div>
            </div>
            );
    }
});


/**
 * Bootstrap NavTab encapsulation.
 * Expected props:
 *  - currentTab - index of tabs[] to be displayed
 *  - tabList - List if tab elements to use : { 'id': 1, 'name': 'Tab 1', 'url': '#' }

 * Children (normally <div />) as content.
 * Example of use:
 *     <NavTab tabList={tabList}>
 *        <div id="tab1"><p>Test 1</p></div>
 *        <div id="tab2"><p>Test 2</p></div>
 *     </NavTab>
 */

var Tab = React.createClass({
    handleClick: function(e){
        e.preventDefault();
        this.props.handleClick();
    },

    render: function(){
        var active = "";
        if(this.props.isCurrent){
            active="active";
        }
        return (
                <li role="presentation" className={active}>
                    <a onClick={this.handleClick} href={this.props.url}>
                        {this.props.name}
                    </a>
                </li>
            );
    }
});

var Tabs = React.createClass({
    handleClick: function(tab){
        this.props.changeTab(tab);
    },

    render: function(){
        return (
            <nav>
                <ul className="nav nav-tabs">
                {this.props.tabList.map(function(tab) {
                    return (
                        <Tab
                            handleClick={this.handleClick.bind(this, tab)}
                            key={tab.id}
                            url={tab.url}
                            name={tab.name}
                            isCurrent={(this.props.currentTab === tab.id)}
                         />
                    );
                }.bind(this))}
                </ul>
            </nav>
        );
    }
});

var Content = React.createClass({
    render: function(){
        return(
            <div className="tabcontent">
                {this.props.currentTab > 0 ?
                <div className={this.props.children.id}>
                    {this.props.children[this.props.currentTab-1]}
                </div>
                :null}

            </div>
        );
    }
});

var NavTab = React.createClass({
    getInitialState: function () {
        return {
            currentTab: this.props.currentTab,
            tabList: this.props.tabList
        };
    },

    changeTab: function(tab) {
        var state = this.state;
        state.currentTab = tab.id;
        this.setState(state);
    },

    render: function(){
        return(
            <div>
                <Tabs
                    currentTab={this.state.currentTab}
                    tabList={this.state.tabList}
                    changeTab={this.changeTab}
                />
                <Content currentTab={this.state.currentTab} >
                    {this.props.children}
                </Content>
            </div>
        );
    }
});
