/** @jsx React.DOM */

/** TODO LATER put there other react-ifications of web components */

/**
 * Used in store.jsx.js and create-organization.jsx.js (so added in their html templates)
 * NB. select2 CSS is added in header.html and select2 js in footer.html
 * this.props.select2Object allows to access the actual select2 from outside,
 * to access ex. conf functions that are in its .opts (but its internal methods
 * are in window.Select2.util).
 */
var Select2Component = React.createClass({
    componentDidMount: function() {
        this.renderSelect2();
    },

    componentDidUpdate: function() {
        this.renderSelect2();
    },

    renderSelect2: function() {
        var placeholder = this.props.placeholder;
        var style = this.props.style;
        var select2 = React.renderComponent(
                React.DOM.input(React.__spread({}, this.props)), // React's polyfill for Object.assign()
                // which is only supported in FF34+ #170, see also https://github.com/react-bootstrap/react-bootstrap/issues/188
                this.refs['select2div'].getDOMNode()
            );
        /*
        // Alternative using spread attributes :
        // NB. this would work with JSX compiler, but not with JSXTransformer until this syntax
        // is accepted by most browsers (else SyntaxError: invalid property id), which will
        // only happen when EcmaScript7 is (Object spread attributes)
        var { onChange, params, ...other } = this.props;
        var select2 = React.renderComponent(
            <input { ...other } />,
            this.refs['select2div'].getDOMNode()
        );*/
        var $el = $(select2.getDOMNode());
        $el.select2(this.props.params);
        $el.on("change", this.props.onChange);
        
        this.props.select2Object = $el.data("select2"); // to access it from outside
        // NB. conf is in .opts, internal methods in window.Select2.util.
    },

    render: function() {
        return <div ref="select2div" />;
    }
});