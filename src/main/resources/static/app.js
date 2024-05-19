const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/gs-guide-websocket'
});

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/test', (greeting) => {
        showGreeting(JSON.parse(greeting.body).content);
    });
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    stompClient.publish({
        destination: "/app/hello",
        body: JSON.stringify({'name': $("#name").val()})
    });
    $("#name").val('')
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $( "#connect" ).click(() => connect());
    $( "#disconnect" ).click(() => disconnect());
    $( "#send" ).click(() => sendName());

    /**
     * EventSource
     *
     * Vue3 lifecycle
     * https://vuejs.org/guide/essentials/lifecycle#lifecycle-diagram
     * https://vuejs.org/api/composition-api-lifecycle
     */
    // onBeforeMount/onMounted
    const channel = '00001';
    let connectId = '';
    const source = new EventSource(`/sse/channel/${channel}`);
    source.addEventListener('message', (e) => {
      const res = JSON.parse(e.data);
      console.log('Received message:', res);
      if (res.type === 'CONNECT') {
        connectId = res.id;
      }
    })

    source.addEventListener('error', (err) => {
        console.log('EventSource Error: ', err);
        source.close();
    })

    // onBeforeUnmount/onUnmounted
    window.addEventListener('beforeunload', () => {
        source.close();
        fetch(`/sse/disconnect/${connectId}`, {
            method: 'post',
        }).then(res => {
            console.log("close", res);
        }).catch(console.error);
    })
});
