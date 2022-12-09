com_company_demo_QuillTextEditor = function () {
    let connector =  this;
    let element = connector.getElement();
    element.innerHTML = "<div id=\"editor\"></div>";

    connector.onStateChange = function () {
        let state = connector.getState();
        let options = state.data;

        let quill = new Quill('#editor', options);
        quill.on('text-change', function (delta, oldDelta, source) {
            if (source === 'user') {
                connector.valueChanged(quill.getText());
            }
        });

        connector.deleteText = function () {
            quill.deleteText(0, quill.getLength());
        }
    }
}