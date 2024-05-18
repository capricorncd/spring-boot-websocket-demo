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
    const uid = Date.now().toString(16) + '-' + Math.random().toString(16).slice(2);
    const source = new EventSource(`/sse/user/${uid}`);
    source.addEventListener('message', (e) => {
      console.log('Received message:', JSON.parse(e.data));
    })

    // onBeforeUnmount/onUnmounted
    window.addEventListener('beforeunload', () => {
        source.close();
        fetch(`/sse/disconnect/${uid}`, {
            method: 'post',
        }).then(res => {
            console.log("close", res);
        }).catch(console.error);
    })
});
