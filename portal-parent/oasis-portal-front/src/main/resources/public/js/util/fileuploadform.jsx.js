/** @jsx React.DOM */

/* NOT USED for now, rather input */
var FileUploadForm = React.createClass({
    getInitialState: function() {
        return {file_data: null};
    },
    handleSubmit: function() {
        var formData = new FormData();
        //formData.append('name', 'iconFile'); // else HTTP error Required MultipartFile parameter 'file' is not present or Multipart boundary missing
        //formData.append('filename', 'icon.png');
        formData.append('iconFile', this.refs.fileUploadInput.getDOMNode().files[0]); // this.state.file_data); // , "icon.png"
        $.ajax({
            url: this.props.uploadUrl,
            cache: false,
            // This will override the content type header, 
            // regardless of whether content is actually sent.
            // Defaults to 'application/x-www-form-urlencoded'
            //contentType: 'multipart/form-data', 
            contentType: false,
            processData: false,
            //Before 1.5.1 you had to do this:
            /*beforeSend: function (x) {
                if (x && x.overrideMimeType) {
                    x.overrideMimeType("multipart/form-data");
                }
            },*/
            //mimeType: 'multipart/form-data',    //Property added in 1.5.1
            type: "POST",
            data: formData, // this.state.file_data, // new FormData(this.refs.fileUploadForm),
            success: function(servedImageUrlData) {
                if (this.props.success) {
                    this.props.success(servedImageUrlData);
                } else {
                    console.log("upload successful");
                }
            }.bind(this),
            error: function (xhr, status, err) {
                if (this.props.error) {
                    this.props.error(xhr, status, err);
                } else {
                    console.error(status, err.toString());
                }
            }.bind(this)
        });
        return false;
    },
    updateFileData: function(e) {
        /*this.setState({
            file_data: new FormData(e.target.parentNode) // of form
        });*/
        var reader = new FileReader();

        reader.onload = function(upload) {
            this.setState({
                file_data: upload.target.result
            });
            console.log(this.state.file_data);
        }.bind(this);

        var file = e.target.files[0];
        reader.readAsDataURL(file);
    },
    render: function() {
        return (
            <form ref="fileUploadForm" onSubmit={this.handleSubmit} encType="multipart/form-data">
                <input ref="fileUploadInput" className="btn btn-default" type="file" onChange={this.handleSubmit} name="iconFile" />
            </form>

        );
    }
});