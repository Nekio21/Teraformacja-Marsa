var client;

var playersDiv = document.getElementById("players");

function startClient(){
    return new Promise((resolve, reject) => {
        client = Stomp.client('ws://127.0.0.1:15674/ws');
        resolve(client)
        console.log("koniec...");
    })
}

async function startWS(){
    var client = await startClient()

    var on_connect = function(){
        console.log('connected')

        client.subscribe("/amq/queue/" + queueSub, function(message) {
            var msg = JSON.parse(message.body)

            console.log(msg);

            if(msg.messageType == "USER_IN"){
                let userSpan = document.getElementById(msg.msg[0]);

                if(userSpan == null){
                    let newDiv = document.createElement("div");
                    let circle = document.createElement("span");
                    circle.className = "activeCircle";
                    circle.id = "circle" + msg.msg[0];
                    let nameSpan = document.createElement("span");
                    nameSpan.id = msg.msg[0];

                    nameSpan.innerHTML = msg.msg[0];

                    newDiv.appendChild(circle);
                    newDiv.appendChild(nameSpan);
                    playersDiv.appendChild(newDiv);
                }
            }
            else if(msg.messageType == "USER_OUT"){
                let userSpan = document.getElementById(msg.msg[0]);

                if(userSpan != null){
                    players.remove(userSpan);
                }
            }
            else if(msg.messageType == "USER_ACTIVE"){
                let circle = document.getElementById("circle" + msg.msg[0]);
                circle.style["background-color"] = "#0f0";
            }
            else if(msg.messageType == "USER_INACTIVE"){
                let circle = document.getElementById("circle" + msg.msg[0]);
                circle.style["background-color"] = "#f00";
            }
        });
    }

    var error_callback = function(error){
        console.log(error);
    }

    client.connect('guest', 'guest', on_connect, error_callback, '/');
}

startWS();


document.addEventListener("visibilitychange", function() {
    let messageValue

    if (document.hidden) {
        messageValue = JSON.stringify(createMessageObject("USER_INACTIVE", ["true"]))
    } else {
        messageValue = JSON.stringify(createMessageObject("USER_ACTIVE", ["true"]))
    }

     client.send("/amq/queue/" + queueSend, {"content-type":"json"},
         messageValue
     );
});

function createMessageObject(messageTypeMethod, msgMethod){
    var obj = {
        messageType: messageTypeMethod,
        msg: msgMethod
    };

    return obj;
}
