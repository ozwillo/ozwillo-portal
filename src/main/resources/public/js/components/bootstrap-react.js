'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import createClass from 'create-react-class';
import PropTypes from 'prop-types';


/**
 * Bootstrap Modal encapsulation.
 *
 * Expected props:
 *  - title - String (will be shown as title)
 *  - successHandler (optional) - callback that is called on validation (not required if infobox is true)
 *  - cancelHandler (optional) - callback that is called on cancel
 *  - buttonLabels (optional) - a map of strings with keys "save", "cancel". By default will map to this.context.t('ui.save'), this.context.t('ui.cancel')
 *  - saveButtonClass (optional) - a additional CSS class to apply to the "save" button (defaults to btn-default)
 *  - large (optional) - if true, will use the modal-lg class on the dialog
 *  - infobox (optional) - if true, will display only a single inverted OK button (label key = 'ok') rather than save / cancel
 *                         also, successHandler has no meaning in that context.
 * Children as content.
 * Open explicitly by calling the open() method. The close() method is also available.
 */
const Modal = createClass({
    propTypes: {
        title: PropTypes.string.isRequired,
        successHandler: PropTypes.func,
        cancelHandler: PropTypes.func,
        buttonLabels: PropTypes.object,
        saveButtonClass: PropTypes.string,
        large: PropTypes.bool,
        infobox: PropTypes.bool
    },
    componentDidMount: function () {
        $(ReactDOM.findDOMNode(this)).modal({show: false});
        if (this.props.cancelHandler) {
            var handler = this.props.cancelHandler;
            $(ReactDOM.findDOMNode(this)).on("hide.bs.modal", function () {
                handler();
            });
        }
    },
    componentWillUnmount: function () {
        $(ReactDOM.findDOMNode(this)).off('hidden');
    },
    close: function (event) {
        $(ReactDOM.findDOMNode(this)).modal('hide');
        // cancel handler will be called on the hide event registered in componentDidMount
    },
    open: function () {
        $(ReactDOM.findDOMNode(this)).modal('show');
    },
    render: function () {

        var buttons;

        if (this.props.infobox) {
            var label;
            if (this.props.buttonLabels) {
                label = this.props.buttonLabels["ok"];
            } else {
                label = this.context.t('ui.close');
            }
            buttons = [
                <button type="button" key="close" className="btn btn-default-inverse"
                        onClick={this.close}>{label}</button>
            ];
        } else {
            var cancelLabel, saveLabel;
            if (this.props.buttonLabels) {
                cancelLabel = this.props.buttonLabels["cancel"];
                saveLabel = this.props.buttonLabels["save"];
            } else {
                cancelLabel = this.context.t('ui.cancel');
                saveLabel = this.context.t('ui.save');
            }
            var saveButtonClass = this.props.saveButtonClass ? "btn " + this.props.saveButtonClass : "btn btn-submit";

            buttons = [
                <button type="button" key="cancel" className="btn btn-default-inverse"
                        onClick={this.close}>{cancelLabel}</button>,
                <button type="submit" key="success" className={saveButtonClass}
                        onClick={this.props.successHandler}>{saveLabel}</button>
            ];
        }

        var className = this.props.large ? "modal-dialog modal-lg" : "modal-dialog";

        return (
            <div className="modal fade oz-simple-modal" tabIndex="-1" role="dialog" aria-labelledby="modalLabel">
                <div className={className} role="document">
                    <div className="modal-content">
                        <div className="modal-header">
                            <button type="button" className="close" data-dismiss="modal" aria-label="Close"
                                    onClick={this.close}>
                                <span aria-hidden="true"><i className="fas fa-times icon"/></span>
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
Modal.contextTypes = {
    t: PropTypes.func.isRequired
};

/**
 * Bootstrap Modal encapsulation with a form inside.
 * Expected props:
 *  - title - String (will be shown as title)
 *  - successHandler - callback that is called on validation
 *  - cancelHandler - callback that is called on cancel
 *  - buttonLabels (optional) - a map of strings with keys "save", "cancel". By default will map to this.context.t('ui.save'), this.context.t('ui.cancel')
 *
 * Children as form fields.
 * Open explicitly by calling the open() method. The close() method is also available.
 */
const ModalWithForm = createClass({
    propTypes: {
        title: PropTypes.string.isRequired,
        successHandler: PropTypes.func.isRequired,
        cancelHandler: PropTypes.func,
        buttonLabels: PropTypes.object
    },
    componentDidMount: function () {
        $(ReactDOM.findDOMNode(this)).modal({show: false});
        if (this.props.cancelHandler) {
            var handler = this.props.cancelHandler;
            $(ReactDOM.findDOMNode(this)).on("hide.bs.modal", function () {
                handler();
            });
        }
    },
    componentWillUnmount: function () {
        $(ReactDOM.findDOMNode(this)).off('hidden');
    },
    close: function (event) {
        $(ReactDOM.findDOMNode(this)).modal('hide');
        if (this.props.onClose) {
            this.props.onClose(event);
        }
    },
    open: function () {
        $(ReactDOM.findDOMNode(this)).modal('show');
    },
    render: function () {

        var buttons;
        var cancelLabel, saveLabel;
        if (this.props.buttonLabels) {
            cancelLabel = this.props.buttonLabels["cancel"];
            saveLabel = this.props.buttonLabels["save"];
        } else {
            cancelLabel = this.context.t('ui.cancel');
            saveLabel = this.context.t('ui.save');
        }

        buttons = [
            <button type="button" key="cancel" className="btn btn-default-inverse"
                    onClick={this.close}>{cancelLabel}</button>,
            <button type="submit" key="success" className="btn btn-submit"
                    onClick={this.props.successHandler}>{saveLabel}</button>
        ];

        return (
            <div className="modal fade" tabIndex="-1" role="dialog" aria-labelledby="modalLabel">
                <div className="modal-dialog" role="document">
                    <div className="modal-content">
                        <div className="modal-header">
                            <button type="button" className="close" data-dismiss="modal" aria-label="Close"
                                    onClick={this.close}>
                                <span aria-hidden="true"><i className="fas fa-times icon"/></span>
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
ModalWithForm.contextTypes = {
    t: PropTypes.func.isRequired
};

module.exports = {Modal, ModalWithForm};
