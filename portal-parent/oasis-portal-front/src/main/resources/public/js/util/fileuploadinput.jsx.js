/** @jsx React.DOM */

var FileUploadInput = React.createClass({
    getInitialState: function() {
        return {file_data: null};
    },
    handleSubmit: function() {
        var formData = new FormData();
        formData.append('iconFile', this.refs.fileUploadInput.getDOMNode().files[0]); // also fills "filename" etc. in multipart Boundary line// else HTTP error Required MultipartFile parameter 'iconFile' is not present or Multipart boundary missing
        $.ajax({
            url: this.props.uploadUrl,
            cache: false,
            contentType: false,
            processData: false,
            type: "POST",
            data: formData, // this.state.file_data,
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
            <input ref="fileUploadInput" className={this.props.className} type="file" onChange={this.handleSubmit} name="iconFile" />

        );
    }
});