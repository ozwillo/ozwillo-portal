import React from "react";

export default class ModalImage extends React.PureComponent{
    state = {
        displayModal: 'none',
        srcImage: '',
        titleImage: ''
    };

    _openModal = (srcImage, titleImage) => {
        this.setState({
            displayModal: 'flex',
            srcImage: srcImage,
            titleImage: titleImage})
    };

    _closeModal = () => {
        this.setState({displayModal: 'none'})
    };

    render() {
        const {displayModal, srcImage, titleImage} = this.state;
        return (
            <div className="modal-image"
                 onClick={this._closeModal}
                 style={{display: displayModal}}>
                <span className="close-modal">&times;</span>
                <img className="modal-image-content" src={srcImage}/>
                    <div className="caption">
                        {titleImage}
                    </div>
            </div>
        )
    }
}
