var client;

var inputMsg = document.getElementById("messageContainer");
var messageContainer = document.getElementById("msgContainer");
var main = document.getElementById("mainPart");
var cards = document.getElementById("cards");
var t;
var r;

const resources = new Map();

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




           if(msg.messageType == "MESSAGE_SEND"){
                let section = document.createElement("section");

                if(msg.msg.length == 2){
                    let name = document.createElement("span");
                    let msgSpan = document.createElement("span");

                    section.className = "message";
                    msgSpan.innerHTML = msg.msg[0]
                    name.innerHTML = msg.msg[1]

                    name.style.color = "#f00";

                    section.appendChild(name);
                    section.appendChild(msgSpan);
                }
                else if(msg.msg.length == 1){
                    section.className = "messageYourself";
                    section.innerHTML = msg.msg[0]
                }

                messageContainer.appendChild(section);
           }
           else if(msg.messageType == "CLOCK"){
                if(msg.msg[0]=="true"){
                    //console.log("twoj ruch: " + msg.msg[1]);
                } else if(msg.msg[0]=="false"){
                    //console.log("czyjs ruch: " + msg.msg[1]);
                }
           }
            else if(msg.messageType == "RECOVER"){
                if(msg.recoverType == "CHAT"){

                    messageContainer.innerHTML = "";

                    for(let i=0; i<msg.chatMessage.length; i++){
                        let section = document.createElement("section");

                        if(msg.chatMessage[i].user != playerName){
                            let name = document.createElement("span");
                            let msgSpan = document.createElement("span");

                            section.className = "message";
                            msgSpan.innerHTML = msg.chatMessage[i].msg
                            name.innerHTML = msg.chatMessage[i].user

                            name.style.color = "#f00";

                            section.appendChild(name);
                            section.appendChild(msgSpan);
                        }
                        else if(msg.chatMessage[i].user == playerName){
                            section.className = "messageYourself";
                            section.innerHTML = msg.chatMessage[i].msg
                        }

                        messageContainer.appendChild(section);
                    }

                }
				else if(msg.recoverType == "PLAYERS"){}
				else if(msg.recoverType == "MAIN_CARD"){}
				else if(msg.recoverType == "USED_CARDS_BLUE"){}
				else if(msg.recoverType == "USED_CARDS_RED"){}
				else if(msg.recoverType == "USED_CARDS_GREEN"){}
				else if(msg.recoverType == "CARD"){}
				else if(msg.recoverType == "RESOURCES"){
					resourceInput()
				}
				else if(msg.recoverType == "PLANET"){console.log(msg)}
				else if(msg.recoverType == "LEVEL"){console.log(msg)}
				else if(msg.recoverType == "OTHERS"){console.log(msg)}
            }
            else if(msg.messageType == "GAME_STATE"){
                if(msg.msg[0] == "WAITING"){
					
                }
                else if(msg.msg[0] == "CHOSE_CARDS"){
					main.style.display = 'none';
					cards.style.display = 'flex';
                }
				else if(msg.msg[0] == "ROUND"){
					main.style.display = 'flex';
					cards.style.display = 'none';
                }
            }
            else if(msg.messageType == "MAIN_CARDS"){   
                let img = document.createElement("img");
                img.classList.add("cardMain");
                img.src = "data:image/png;base64," + msg.cards[0].image;

                img.addEventListener("click", function(){
                    sendToServer("MAIN_CARDS", [msg.cards[0].index])
                });

                cards.appendChild(img);
            }
            else if(msg.messageType == "CARDS10"){
                cards.innerHTML = "";

				let div = document.createElement("div");
				let button = document.createElement("div");

                for(let i=0;i<msg.cards.length;i++){
                    let img = document.createElement("img");
                    img.classList.add("cardNormal");
                    img.src = "data:image/png;base64," + msg.cards[i].image;4
					img.setAttribute('id', msg.cards[i].index);
					img.addEventListener("click", function(){
						img.classList.toggle("cardChosed");
						
						let imgs = document.getElementsByClassName("cardChosed")
						let lastGold = resources.get(playerName).gold
						resources.get(playerName).gold = lastGold - imgs.length*3
							
						resourceInput()
						resources.get(playerName).gold = lastGold
					});

                    cards.appendChild(img);
                }
				
				button.innerHTML = "send";
				button.classList.add("buttonSend");
				
				button.addEventListener("click", function(){
					
					
					let imgs = document.getElementsByClassName("cardChosed")
					const ids = [];
					for(let i=0;i<imgs.length;i++){
						ids[i] = imgs[i].getAttribute('id')
						
						
					}
					console.log(ids);
					
					let gold = resources.get(playerName).gold - imgs.length*3
					if(gold < 0){
					
					}
					else{
						sendToServer("CARDS10", ids)
					}
				});
				
				div.appendChild(button);
				cards.appendChild(div);
            }
			else if(msg.messageType == "RESOURCES"){
				console.log(msg.resources)
				r = msg.resources
				
				for(let i=0;i<msg.owners.length;i++){
					resources.set(msg.owners[i], msg.resources[i])
				}
				
				
				resourceInput()
			}
			else if(msg.messageType == "USER_STATE"){
				if(msg.msg[0] == "WAITING"){
					console.log('wait')
				}
			}
            else if(msg.messageType == "CARDS"){
                console.log("cards " + msg)
            }
//			else if(msg.messageType == "CARDS10"){
//				alert("karty są :)");
//			}
		
        });
    }

    var error_callback = function(error){
        console.log(error);
    }

    client.connect('guest', 'guest', on_connect, error_callback, '/');

}

startWS();

function sendMessage(){
     let news = inputMsg.value

     messageValue = JSON.stringify(createMessageObject("MESSAGE_SEND", [news]))

     client.send("/amq/queue/" + queueSend, {"content-type":"json"},
         messageValue
     );
}

function sendToServer(typ, tab){
    messageValue = JSON.stringify(createMessageObject(typ, tab))

    client.send("/amq/queue/" + queueSend, {"content-type":"json"},
        messageValue
    );
}

function createMessageObject(messageTypeMethod, msgMethod){
    var obj = {
        messageType: messageTypeMethod,
        msg: msgMethod
    };

    return obj;
}

function resourceInput(){
	let r = resources.get(playerName)
	
	document.getElementById('goldR').innerHTML = r.gold + "[+" + r.goldProd + "]";
	document.getElementById('metalR').innerHTML = r.metal + "[+" + r.metalProd + "]";
	document.getElementById('titaniumR').innerHTML = r.titanium + "[+" + r.titaniumProd + "]";
	document.getElementById('plantsR').innerHTML = r.plants + "[+" + r.plantsProd + "]";
	document.getElementById('energyR').innerHTML = r.energy + "[+" + r.energyProd + "]";
	document.getElementById('heatR').innerHTML = r.heat + "[+" + r.heatProd + "]";
}