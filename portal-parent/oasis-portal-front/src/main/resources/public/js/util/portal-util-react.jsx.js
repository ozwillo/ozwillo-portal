/** @jsx React.DOM */

/** TODO LATER put there other react-ifications of web components */

/**
 * Used in store.jsx.js and create-organization.jsx.js (so added in their html templates)
 * NB. select2 CSS is added in header.html and select2 js in footer.html
 * this.props.select2Object allows to access the actual select2 from outside,
 * to access ex. conf functions that are in its .opts (but its internal methods
 * are in window.Select2.util).
 */
var Select2Mixin = {
    componentDidMount: function() {
        this.renderSelect2();
    },

    componentDidUpdate: function() {
        //this.renderSelect2(); // NO when mixins, triggers "Invalid arguments to select2 plugin:" error
    },
    
    onChange: function(event) {
        var s = this.state;
        s.value = event.val; // array in case of multi , ex. if geo URI array [ 'http://.../Valence', 'http://.../Lyon' ]
        this.setState(s);
        if (this.props.onChange) {
            this.props.onChange(event);
        }
    },

    renderSelect2: function() {
        //var placeholder = this.props.placeholder;
        //var style = this.props.style;
        var select2 = React.render(
                React.DOM.input(React.__spread({}, this.props)), // React's polyfill for Object.assign()
                // which is only supported in FF34+ #170, see also https://github.com/react-bootstrap/react-bootstrap/issues/188
                this.refs.select2div.getDOMNode()
            );
        /*
        // Alternative using spread attributes :
        // NB. this would work with JSX compiler, but not with JSXTransformer until this syntax
        // is accepted by most browsers (else SyntaxError: invalid property id), which will
        // only happen when EcmaScript7 is (Object spread attributes)
        var { onChange, params, ...other } = this.props;
        var select2 = React.render(
            <input { ...other } />,
            this.refs['select2div'].getDOMNode()
        );*/
        var $el = $(select2.getDOMNode());
        $el.select2(this.props.params);
        $el.on("change", this.onChange);
        
        this.props.select2Object = $el.data("select2"); // to access it from outside
        // NB. conf is in .opts, internal methods in window.Select2.util.
    },

    render: function() {
        return <div ref="select2div" />;
    }
};

/**
 * Base, single use select2
 * Ex. use :
 * <Select2Component params={actualSelect2Params} style={{minWidth: "300px"}} onChange={this.searchChanged} name="search" />
 */
var Select2Component = React.createClass({
    mixins: [Select2Mixin]
});

/**
 * Geo select2 component
 * (no need to provide params)
 */
var GeoSelect2Mixin = {

    /* To be called & overriden by the actual component's getDefaultProps().
     * Inits geo select2 params, followed by dependent select2 conf functions
     * NB. if only var and not function, ex. formatResult doesn't work properly because bad "this"
     * NB. JSON.parse(JSON.stringify(geoSelect2Params)) would mangle REST conf, besides being
     * less react-y than getDefaultProps() (which however can't be used because we want to
     * allow to override them in "submixins" TODO) */
    componentWillMount : function() {
        this.props.params = {
            multiple: true,
            allowClear: true,
            placeholder: t('ui.location'),
            separator: "|", // else http://...Barcenas, Las => two values
            //tags: ["Valence", "Barcelone", "Torino"],
            minimumInputLength: 3,
            ajax: {
                url: this.props.urlResources,//store_service + "/geographicalAreas",
                dataType: "json",
                quietMillis: 250,
                data: function( term, page ) {
                    return {
                        // search term
                        q: term
                    };
                },
                results: function( data, page ) {
                        // parse the results into the format expected by Select2.
                        // since we are using custom formatting functions we do not need to alter the remote JSON data
                        return { results: data.areas };
                },
                cache: true
            },
            //initSelection: function( element, callback ) { }
            
            // Formats the dropdown list of select2 alternatives to click on (which will create a tag for it)
            formatResult: function(result, container, query, escapeMarkup) {
                return this.formatResultWithTooltip(result, container, query, escapeMarkup);
            },
            /* test, not used */
            formatResultTest : function(area) {
                var markup = "<div class='select2-result-repository clearfix'>" +
                    "<div class='select2-result-repository__meta'>" +
                        "<div class='select2-result-repository__title' title='" + area.uri + "'>" + area.name + "</div>";

                if (area.detailedName) {
                    markup += "<div class='select2-result-repository__description'>" + area.detailedName + "</div>";
                }

                markup += 
                    "</div></div>";

                return markup;
            },
            tooltip : function (area) { // extended select2 option
                return area.uri;
            },
            /*formatResultWithTooltip : function(result, container, query, escapeMarkup) { // extends select2
                // inspired by select2's formatResult
                var markup=[];
                window.Select2.util.markMatch(this.text(result), query.term, markup, escapeMarkup); // accessing select2 internal function
                // additionally wrapping by titling span :
                markup.push("</span>");
                return "<span class='select2-tooltip' title='" + this.tooltip(result) + "'>"
                    + markup.join("");
            },
            formatResultWithTooltip : function(result, container, query, escapeMarkup) { // extends select2
            // inspired by select2's formatResult
                var markup=[];
                window.Select2.util.markMatch(this.select2Object().opts.text(result), query.term, markup, escapeMarkup);
                // wrap by titling span :
                markup.push("</span>"); // TOOLTIP
                return "<span class='select2-tooltip' title='" + this.tooltip(result) + "'>" + markup.join("");
            },*/
            formatResultWithTooltip : function(result, container, query, escapeMarkup) { // extends select2
                // inspired by select2's formatResult
                var markup=[];
                this.markMatchWithTooltip(this.text(result), query.term, markup, escapeMarkup, this.tooltip(result));
                return markup.join("");
            },
            markMatchWithTooltip : function(text, term, markup, escapeMarkup, tooltip) { // inspired by select2's markMatch
                var match=window.Select2.util.stripDiacritics(text.toUpperCase())
                                    .indexOf(window.Select2.util.stripDiacritics(term.toUpperCase())), // accessing select2 internal function
                    tl=term.length;

                if (match<0) {
                    markup.push(escapeMarkup(text));
                    return;
                }

                markup.push("<span class='select2-tooltip' title='" + tooltip + "'>"); // TOOLTIP
                markup.push(escapeMarkup(text.substring(0, match)));
                markup.push("</span>"); // TOOLTIP
                markup.push("<span class='select2-match select2-tooltip' title='" + tooltip + "'>"); // +TOOLTIP
                markup.push(escapeMarkup(text.substring(match, match + tl)));
                markup.push("</span>");
                markup.push("<span class='select2-tooltip' title='" + tooltip + "'>"); // TOOLTIP
                markup.push(escapeMarkup(text.substring(match + tl, text.length)));
                markup.push("</span>"); // TOOLTIP
            },
            
            // NB. selected tags can be formatted using CSS : select2-search-choice
            // or otherwise by rewriting MultiSelect2.addSelectedChoice()
            
            //formatSelection: formatSelectionTest,
            formatSelectionTest : function(area) {
                return area.name;
            },

            text: function(area) {
                return area.name;
            },
            id: function(area) {
                return area.uri;
            },
            
            /*initSelection = function(element, callback) {
                callback(this.state.geographicalAreaUris);
            }, // NO doesn't work*/
            
            //Allow manually entered text in drop down.
            /*createSearchChoice: function(term, data) {
                if ($(data).filter(function(t) {
                    return t === term; // this.text.localeCompare(term)===0;
                }).length===0) {
                    return {id:'q', text:term, name:term, uri:'q'}; // NOT id text
                }
            },*/
            // apply css that makes the dropdown taller
            dropdownCssClass: "bigdrop",
            // we do not want to escape markup since we are displaying html in results ?!
            escapeMarkup: function( m ) {
                return m;
            }
        };
    }
};

var GeoMultiSelect2Component = React.createClass({
    mixins: [GeoSelect2Mixin, Select2Mixin],
    getInitialState: function () {
        return {
            value : []
        };
    },
    componentWillMount : function() {
        this.props.params.multiple = true;
    }
});

var GeoSingleSelect2Component = React.createClass({
    mixins: [GeoSelect2Mixin, Select2Mixin],
    getInitialState: function () {
        return {
            value : null
        };
    },
    componentWillMount : function() {
        this.props.params.multiple = false;
    }
});
