import React from 'react';
import createClass from 'create-react-class';

var FileUploadInput = createClass({
    handleSubmit: function() {
        var formData = new FormData();
        // also fills "filename" etc. in multipart Boundary line
        // else HTTP error Required MultipartFile parameter 'iconFile' is not present or Multipart boundary missing
        formData.append('iconFile', this.refs.fileUploadInput.getDOMNode().files[0]);
        $.ajax({
            url: this.props.uploadUrl,
            cache: false,
            contentType: false,
            processData: false,
            type: "POST",
            data: formData,
            success: function(servedImageUrlData) {
                if (this.props.success) {
                    this.props.success(servedImageUrlData);
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
    render: function() {
        return (
            <input ref="fileUploadInput" className={this.props.className} type="file" onChange={this.handleSubmit} name="iconFile" />
        );
    }
});

export default FileUploadInput;
