document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('#message-form');
    const inputForm = form.querySelector('input[type="text"]');
    const submitButton = form.querySelector('input[type="submit"]');
    const messagesTable = document.querySelector('#messages');
    const messagesTableHeader = messagesTable.querySelector('tr');

    function main() {
        disableForm();

        const ws = connectChat();
        form.addEventListener('submit', event => {
            event.preventDefault();
            try {
                submit(ws);
            } catch (err) {
                error(`Submit error: ${err}`);
            }
        });
    }

    function connectChat() {
        const ws = new WebSocket(`ws://${location.host}/chat/ws${location.search || ''}`);
        ws.onopen = event => {
            console.log(event);
            info(`WebSocket Open`);
            enableForm();
        };
        ws.onclose = event => {
            console.log(event);
            info(`WebSocket Close`);
            disableForm();
        };
        ws.onmessage = event => {
            console.log(event);
            message(JSON.parse(event.data));
        };
        ws.onerror = event => {
            console.log(event);
            error(`WebSocket Error`);
        };
        return ws;
    }

    function enableForm() {
        setFormDisabled(false);
    }

    function disableForm() {
        setFormDisabled(true);
    }

    function setFormDisabled(isDisabled) {
        Array.from(form.querySelectorAll('input')).forEach(i => {
            i.disable = isDisabled;
        });
    }

    function submit(ws) {
        const msg = inputForm.value;
        if (msg) {
            ws.send(JSON.stringify({
                message: msg,
                timestamp: Date.now()
            }));
            inputForm.value = '';
        }
    }

    function info(msg) {
        log('info', msg);
    }

    function error(msg) {
        log('err', msg);
    }

    function message(data) {
        log('message', data.message, new Date(data.timestamp));
    }

    function log(type, msg, timestamp) {
        addRow({
            type,
            message: msg,
            timestamp: timestamp || new Date(),
        });
    }

    function addRow(rowData) {
        const html = `<tr>
  <td>${rowData.timestamp.toLocaleString()}</td>
  <td>${rowData.type}</td>
  <td>${rowData.message}</td>
</tr>`;
        messagesTableHeader.insertAdjacentHTML('AfterEnd', html);
    }

    main();
});
