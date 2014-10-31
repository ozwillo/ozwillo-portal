/** @jsx React.DOM */

/**
 * Bootstrap Modal encapsulation.
 * Expected props:
 *  - title - String (will be shown as title)
 *  - successHandler - callback that is called on validation
 *  - buttonLabels (optional) - a map of strings with keys "save", "cancel". By default will map to t('ui.save'), t('ui.cancel')
 *  - large (optional) - if true, will use the modal-lg class on the dialog
 *  - infobox (optional) - if true, will display only a single inverted OK button (label key = 'ok') rather than save / cancel
 *                         also, successHandler has no meaning in that context.
 * Children as content.
 * Open explicitly by calling the open() method. The close() method is also
 * available.
 */
var Modal = React.createClass({
    componentDidMount: function () {
        $(this.getDOMNode()).modal({show: false});
    },
    componentWillUnmount: function () {
        $(this.getDOMNode()).off('hidden');
    },
    close: function () {
        $(this.getDOMNode()).modal('hide');

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
                <button key="close" className="btn btn-primary-inverse" onClick={this.close}>{label}</button>
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

            buttons = [
                <button key="cancel" className="btn btn-default" onClick={this.close}>{cancelLabel}</button>,
                <button key="success" className="btn btn-primary" onClick={this.props.successHandler}>{saveLabel}</button>
            ];
        }


        return (
            <div className="modal fade">
                <div className={'modal-dialog' + (this.props.large ? ' modal-lg' : '')}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <button
                            type="button"
                            className="close"
                            onClick={this.close}>&times;</button>
                            <h3>{this.props.title}</h3>
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
 * Not really Bootstrap, but it's Twitter anyway...
 * Creates a simple typeahead
 * input props:
 *  - source: a function(query, cb)
 *  - onSelect: a callback that is called when an item is selected
 */
var Typeahead = React.createClass({
    componentDidMount: function () {

        $(this.getDOMNode()).typeahead({
            minLength: 3,
            highlight: true
        }, {
            source: this.props.source,
            displayKey: 'fullname'
        }).on("typeahead:selected", function (event, selected) {
            if (this.props.onSelect != undefined) {
                this.props.onSelect(selected);
            }
            $(this.getDOMNode()).typeahead('val', '');
        }.bind(this));
    },
    render: function () {
        return (
            <input className="form-control" type="text" placeholder={this.props.placeholder}></input>
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
