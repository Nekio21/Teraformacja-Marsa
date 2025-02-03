var client;

var inputMsg = document.getElementById("messageContainer");
var messageContainer = document.getElementById("msgContainer");
var main = document.getElementById("mainPart");
var cards = document.getElementById("cards");
var t;
var r;

const selectPlayer = document.getElementById("selectPlayer");
const blueCardCheck = document.getElementById("blueCardCheck");
const redCardCheck = document.getElementById("redCardCheck");
const greenCardCheck = document.getElementById("greenCardCheck");
const yourCardCheck = document.getElementById("yourCardCheck");
var cardTypeSelect = "your";

var msgTab = [];

const resources = new Map();
const levels = new Map();
var tempWK = 0;
var o2WK = 0;
var oceanWK = 0;
var gameState;
const userStates = new Map();
const cardUsedBlue = new Map();
const cardUsedRed = new Map();
const cardUsedGreen = new Map();
const mainCard = new Map();
var cardsTab = [];
var namesOther = [];

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
                document.getElementById("timerText").innerHTML = msg.msg[0] + ": " + msg.msg[1] + "sec";
           }
            else if(msg.messageType == "RECOVER"){
                msgTab[msg.recoverType] = msg
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
				else if(msg.recoverType == "PLAYERS"){
                    
                    let x = 0;

                    for(let i=0;i<msg.msg.length;i++){
                        if(msg.msg[i] != playerName){
                            namesOther[x] = msg.msg[i];
                            x++;
                        }
                    }

                    updatePlayers()
                }
				else if(msg.recoverType == "MAIN_CARD"){
                    for(let i=0;i<msg.owners.length;i++){
                        mainCard.set(msg.owners[i], msg.cards[i]);
                    }
                    updateMainCard();
                }
				else if(msg.recoverType == "USED_CARDS_BLUE"){
                    
                }
				else if(msg.recoverType == "USED_CARDS_RED"){}
				else if(msg.recoverType == "USED_CARDS_GREEN"){}
				else if(msg.recoverType == "CARD"){
                    for(let i=0;i<msg.owners.length;i++){
                        if(msg.owners[i] == playerName){
                            cardsTab = msg.cardsList[i];
                        }
                    }

                    updateCardConstainer();
                }
				else if(msg.recoverType == "RESOURCES"){
                    console.log(msg.resources)
				    r = msg.resources
				
				    for(let i=0;i<msg.owners.length;i++){
					    resources.set(msg.owners[i], msg.resources[i])
				    }
                    
					resourceInput()
				}
				else if(msg.recoverType == "PLANET"){
                    console.log(msg)
                    makePlanet(msg.msg);
                }
				else if(msg.recoverType == "LEVEL"){
                    console.log(msg)
                    makeLevels(msg.dataLong, msg.owners)
                    useLevels(msg.owners)
                }
				else if(msg.recoverType == "OTHERS"){
                    console.log(msg)

                }
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
            else if(msg.messageType == "CARDS10" || msg.messageType == "CARDS"){
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
                        alert("za malo pieniedzy");
					}
					else{
						sendToServer(msg.messageType, ids)
                        main.style.display = 'flex';
					    cards.style.display = 'none';
                        document.getElementById("timerText").innerHTML = "Prosze Czekać :)"
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
            else if(msg.messageType == "CARDS"){
                for(let i=0;i<msg.cards.length; i++){
                    cardsTab.push(msg.cards[i])
                }
            }
            else if(msg.messageType == "USER_STATE"){
                for(let i=0;i<msg.owners.length;i++){
                    userStates.set(msg.owners[i], msg.msg[i]);
                }

                
                useLevels(msg.owners)

                if(msg.msg[0] == "CHOSE_CARD"){
					main.style.display = 'none';
					cards.style.display = 'flex';
				}
                
                if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
                    document.getElementById("endRound").classList.add("endRoundActive");
                }else{
                    document.getElementById("endRound").classList.remove("endRoundActive");  
                }
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

    for(let i=0;i<namesOther.length;i++){
        let r = resources.get(namesOther[i])
        let sections = document.getElementById("playerName" + namesOther[i]).parentElement.getElementsByClassName("infoPlayerContainer")[0].getElementsByTagName("section");

        sections[0].getElementsByTagName("span")[0].innerHTML = r.gold + "[+" + r.goldProd + "]";
        sections[1].getElementsByTagName("span")[0].innerHTML = r.metal + "[+" + r.metalProd + "]";
        sections[2].getElementsByTagName("span")[0].innerHTML = r.titanium + "[+" + r.titaniumProd + "]";
        sections[3].getElementsByTagName("span")[0].innerHTML = r.plants + "[+" + r.plantsProd + "]";
        sections[4].getElementsByTagName("span")[0].innerHTML = r.energy + "[+" + r.energyProd + "]";
        sections[5].getElementsByTagName("span")[0].innerHTML = r.heat + "[+" + r.heatProd + "]";
    }
}

function updatePlayers(){
    
    let options = selectPlayer.getElementsByTagName("option");
    options[0].innerHTML =  playerName;
    options[0].value =  playerName;

    let playerNameDiv = document.getElementsByClassName('playerName');

    for(let i=0;i<playerNameDiv.length;i++){
        playerNameDiv[i].innerHTML = namesOther[i];
        playerNameDiv[i].id = "playerName" + namesOther[i];
        options[i+1].innerHTML = namesOther[i];
        options[i+1].value = namesOther[i];
    }
}

function makePlanet(tab){
    let rows = document.getElementsByClassName("row");
    let w=0;

    for(let x=0;x<rows.length;x++){
        let divs = rows[x].getElementsByTagName('div');
        for(let y=0;y<divs.length;y++){
            divs[y].innerHTML = "";
            let img = document.createElement("img");
            img.style.width = "63px";
            img.style.height = "73px";
            
            if(tab[w]=="NOTHING"){
                img.src = "../../assets/rect.svg";
            }
            else if(tab[w]=="NO_OCEAN"){
                img.src = "../../assets/rectWater.svg";
            }

            divs[y].appendChild(img);
            w++;
        }
    }
}

function makeLevels(level, owners){
    for(let i=0;i<level.length;i++){
        levels.set(owners[i], level[i]);
    }
}

function useLevels(owners){
    for(let i=0;i<owners.length;i++){
        if(owners[i] == playerName){
            let text = playerName;
            document.getElementById("mainPlayer").innerHTML = text + " [" + levels.get(owners[i]) + "]" + " [" + userStates.get(owners[i]) +"]";
        }else{
            let text = owners[i];
            document.getElementById("playerName" + owners[i]).innerHTML = text + " [" + levels.get(owners[i]) + "]" + " [" + userStates.get(owners[i]) +"]";
        }
    }
}


function updateMainCard(){

}

function updateCard(){

}

document.getElementById("poligonPlayer").addEventListener("click", function(){
    document.getElementsByClassName("playersTab")[0].style.display = "flex";
    document.getElementsByClassName("playersMessage")[0].style.display = "none";
    document.getElementsByClassName("cardsTab")[0].style.display = "none";
});

document.getElementById("poligonChat").addEventListener("click", function(){
    document.getElementsByClassName("playersTab")[0].style.display = "none";
    document.getElementsByClassName("playersMessage")[0].style.display = "flex";
    document.getElementsByClassName("cardsTab")[0].style.display = "none";
});

document.getElementById("poligonCards").addEventListener("click", function(){
    document.getElementsByClassName("playersTab")[0].style.display = "none";
    document.getElementsByClassName("playersMessage")[0].style.display = "none";
    document.getElementsByClassName("cardsTab")[0].style.display = "flex";
});

selectPlayer.addEventListener("change", function(){
    updateCardConstainer(selectPlayer.value)
});

updateCardConstainer(playerName)

blueCardCheck.addEventListener("click", function(){
    blueCardCheck.classList.add("checkChoosen");
    redCardCheck.classList.remove("checkChoosen");
    greenCardCheck.classList.remove("checkChoosen");
    yourCardCheck.classList.remove("checkChoosen");
    selectPlayer.style.display = "inline";
    cardTypeSelect = "blue";
    updateCardConstainer()
});

redCardCheck.addEventListener("click", function(){
    blueCardCheck.classList.remove("checkChoosen");
    redCardCheck.classList.add("checkChoosen");
    greenCardCheck.classList.remove("checkChoosen");
    yourCardCheck.classList.remove("checkChoosen");
    selectPlayer.style.display = "inline";
    cardTypeSelect = "red";
    updateCardConstainer()
});

greenCardCheck.addEventListener("click", function(){
    blueCardCheck.classList.remove("checkChoosen");
    redCardCheck.classList.remove("checkChoosen");
    greenCardCheck.classList.add("checkChoosen");
    yourCardCheck.classList.remove("checkChoosen");
    cardTypeSelect = "green";
    selectPlayer.style.display = "inline";
    updateCardConstainer()
});

yourCardCheck.addEventListener("click", function(){
    blueCardCheck.classList.remove("checkChoosen");
    redCardCheck.classList.remove("checkChoosen");
    greenCardCheck.classList.remove("checkChoosen");
    yourCardCheck.classList.add("checkChoosen");
    selectPlayer.style.display = "none";
    cardTypeSelect = "your";
    updateCardConstainer();
});

document.getElementById("endRound").addEventListener("click", function(){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        sendToServer("NEXT_ROUND",[playerName])
    }
});

function updateCardConstainer(){
    let value = selectPlayer.value;
    let cardsContainer = document.getElementById("cardsContainer");
    cardsContainer.innerHTML = "";

    if(cardTypeSelect == "your"){
        for(let i=0;i<cardsTab.length;i++){
            let img = document.createElement("img");
            img.src = "data:image/png;base64," + cardsTab[i].image;
            img.addEventListener("dblclick", function(){
                sendToServer("USE_CARD", [cardsTab[i].index])
            });

            cardsContainer.appendChild(img);
        }
    }
    else if(cardTypeSelect == "green"){
        for(let i=0;i<cardUsedGreen.get(value).length;i++){
            let img = document.createElement("img");
            img.src = "data:image/png;base64," + cardUsedGreen.get(value)[i].image;
        

            cardsContainer.appendChild(img);
        }
    }
    else if(cardTypeSelect == "red"){
        for(let i=0;i<cardUsedRed.get(value).length;i++){
            let img = document.createElement("img");
            img.src = "data:image/png;base64," + cardUsedRed.get(value)[i].image;
            

            cardsContainer.appendChild(img);
        }
    }
    else if(cardTypeSelect == "blue"){
        for(let i=0;i<cardUsedBlue.get(value).length;i++){
            let img = document.createElement("img");
            img.src = "data:image/png;base64," + cardUsedBlue.get(value)[i].image;
    

            cardsContainer.appendChild(img);
        }
    }
}