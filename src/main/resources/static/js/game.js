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
var testTab;

const resources = new Map();
const levels = new Map();

var round = 0;
var tempWK = 0;
var o2WK = 0;
var oceanWK = 0;
var winTemp = 0;
var winO2 = 0;
var winOcean = 0;

var gameState;
const userStates = new Map();
const cardUsedBlue = new Map();
const cardUsedRed = new Map();
const cardUsedGreen = new Map();
const mainCard = new Map();
var cardsTab = [];
var namesOther = [];

var planet;

var clickableArea = [];
var heatIconAEL = false;
var plantsIconAEL = false;

var boardClick = "";
var treeMoney = "";

function startClient(){
    return new Promise((resolve, reject) => {
        //client = Stomp.client('ws://127.0.0.1:15674/ws');
        client = Stomp.client('ws://' + window.location.hostname + ':15674/ws');
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
                    updateColors(msg.msg);
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
                            console.log("dodajem !!!")
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
                    planet = msg.msg;
                    makePlanet();
                }
				else if(msg.recoverType == "LEVEL"){
                    console.log(msg)
                    makeLevels(msg.dataLong, msg.owners)
                    useLevels(msg.owners)
                }
				else if(msg.recoverType == "OTHERS"){

                    round = msg.msg[0];

                    winTemp = msg.msg[1];
                    tempWK = msg.msg[2];

                    winO2 = msg.msg[3];
                    o2WK = msg.msg[4];

                    winOcean = msg.msg[5];
                    oceanWK = msg.msg[6];

                    updateOthers()
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
                    planetRefresh()
                });

                cards.appendChild(img);
            }
            //else if(msg.messageType == "CARDS10" || msg.messageType == "CARDS"){
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
                        alert("za malo pieniedzy");
					}
					else{
						sendToServer(msg.messageType, ids)
                        planetRefresh();
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
                    console.log("dodajem push !!!")
                    cardsTab.push(msg.cards[i])
                }
                updateCardConstainer()
            }
            else if(msg.messageType == "OTHERS"){
                    
                round = msg.msg[0];

                winTemp = msg.msg[1];
                tempWK = msg.msg[2];

                winO2 = msg.msg[3];
                o2WK = msg.msg[4];

                winOcean = msg.msg[5];
                oceanWK = msg.msg[6];

                updateOthers()
            }
            else if(msg.messageType == "USER_STATE"){
                for(let i=0;i<msg.owners.length;i++){
                    userStates.set(msg.owners[i], msg.msg[i]);
                }

                
                useLevels(msg.owners)

                if(userStates.get(playerName) == "CHOSE_CARD"){
					main.style.display = 'none';
					cards.style.display = 'flex';
				}
                
                if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
                    document.getElementById("endRound").classList.add("endRoundActive");
                }else{
                    document.getElementById("endRound").classList.remove("endRoundActive");  
                }
            }
            else if(msg.messageType == "USE_CARD"){
                testTab = msg;
                if(msg.cards[0].typeCard == "BLUE"){
                    console.log("wwwww")
                    let list = cardUsedBlue.get(msg.about);
                    list.push(msg.cards[0]);
                    cardUsedBlue.set(msg.about, list);
                    console.log(cardUsedBlue)
                }
                else if(msg.cards[0].typeCard == "GREEN"){
                    console.log("wwww4w")
                    let list = cardUsedGreen.get(msg.about);
                    list.push(msg.cards[0]);
                    cardUsedGreen.set(msg.about, list);
                    console.log(cardUsedGreen)
                }
                else if(msg.cards[0].typeCard == "RED"){
                    console.log("wwwww5")
                    let list = cardUsedRed.get(msg.about);
                    list.push(msg.cards[0]);
                    cardUsedRed.set(msg.about, list);
                    console.log(cardUsedRed)
                }

                for(let i=0;i<cardsTab.length;i++){
                    if(cardsTab[i].index == msg.cards[0].index){
                        console.log("Wycinam !!!")
                        cardsTab.splice(i, 1);
                        console.log("Wycinam after!!!")
                    }
                }

                let img = document.createElement("img");
                img.src = "data:image/png;base64," + msg.cards[0].image;

                animateCard(img);
                updateCardConstainer()
            }
            else if(msg.messageType == "LEVELS"){
                console.log(msg)
                makeLevels(msg.dataLong, msg.owners)
                useLevels(msg.owners)
            }
            else if(msg.messageType == "BOARD_TREE"){
                planetRefresh();
                boardClick = "tree";
                planetTree(msg.msg);
            }
            else if(msg.messageType == "BOARD_CITY"){
                planetRefresh();
                boardClick = "city";
                planetTree(msg.msg);
            }
            else if(msg.messageType == "BOARD_OCEAN"){
                planetRefresh();
                boardClick = "ocean";
                planetTree(msg.msg);
            }
            else if(msg.messageType == "PLANET"){
                planet = msg.msg;
                makePlanet();
            }
            else if(msg.messageType == "TITLE"){
                title(msg);
            }
            else if(msg.messageType == "PRIZE"){
                prize(msg)
            }
            else if(msg.messageType == "ERROR"){
                if(msg.error == "NO_YOUR_MOVE"){
                    alert("Nie twój ruch")
                }
                else if(msg.error == "MONEY"){
                    alert("za mało pieniedzy");
                }
                else{
                    alert("błąd");
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
	let heatIcon = document.getElementById('heatIcon');
    let plantsIcon = document.getElementById('plantsIcon');

	document.getElementById('goldR').innerHTML = r.gold + "[+" + r.goldProd + "]";
	document.getElementById('metalR').innerHTML = r.metal + "[+" + r.metalProd + " :" + r.metaltg  +"]";
	document.getElementById('titaniumR').innerHTML = r.titanium + "[+" + r.titaniumProd + " :" + r.titaniumtg + "]";
	document.getElementById('plantsR').innerHTML = r.plants + "[+" + r.plantsProd + "/" + r.plantstf+ "]";
	document.getElementById('energyR').innerHTML = r.energy + "[+" + r.energyProd + "]";
	document.getElementById('heatR').innerHTML = r.heat + "[+" + r.heatProd + "/" + r.heattt + "]";

    if(r.heat >= r.heattt){
        heatIcon.classList.add("resourceIconHover");
        if(heatIconAEL == false){
            heatIcon.addEventListener("click", function(){
                if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
                    if(resources.get(playerName).heat >= resources.get(playerName).heattt){
                        sendToServer("TEMP_UP", []);
                        planetRefresh()
                    }
                }
            });
            heatIconAEL = true;
        }
    }else{
        heatIcon.classList.remove("resourceIconHover");
    }

    if(r.plants >= r.plantstf){
        plantsIcon.classList.add("resourceIconHover");

        if(plantsIconAEL == false){
            plantsIcon.addEventListener("click", function(){
                if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
                    if(resources.get(playerName).plants >= resources.get(playerName).plantstf){
                        treeMoney = "leaf";
                        sendToServer("BOARD_TREE", []);
                    }
                }
            });
            plantsIconAEL = true;
        }
    }else{
        plantsIcon.classList.remove("resourceIconHover");
    }

    for(let i=0;i<namesOther.length;i++){
        let r = resources.get(namesOther[i])
        let sections = document.getElementById("playerName" + namesOther[i]).parentElement.getElementsByClassName("infoPlayerContainer")[0].getElementsByTagName("section");

        sections[0].getElementsByTagName("span")[0].innerHTML = r.gold + "[+" + r.goldProd + "]";
        sections[1].getElementsByTagName("span")[0].innerHTML = r.metal + "[+" + r.metalProd + " :" + r.metaltg  + "]";
        sections[2].getElementsByTagName("span")[0].innerHTML = r.titanium + "[+" + r.titaniumProd + " :" + r.titaniumtg + "]";
        sections[3].getElementsByTagName("span")[0].innerHTML = r.plants + "[+" + r.plantsProd + "/" + r.plantstf+ "]";
        sections[4].getElementsByTagName("span")[0].innerHTML = r.energy + "[+" + r.energyProd + "]";
        sections[5].getElementsByTagName("span")[0].innerHTML = r.heat + "[+" + r.heatProd + "/" + r.heattt +"]";
    }
}

function updatePlayers(){
    
    let options = selectPlayer.getElementsByTagName("option");
    options[0].innerHTML =  playerName;
    options[0].value =  playerName;

    if(cardUsedGreen.get(playerName) == undefined){
        cardUsedGreen.set(playerName, []);
        cardUsedRed.set(playerName, []);
        cardUsedBlue.set(playerName, []);
    }

    let playerNameDiv = document.getElementsByClassName('playerName');

    for(let i=0;i<playerNameDiv.length;i++){
        playerNameDiv[i].innerHTML = namesOther[i];
        playerNameDiv[i].id = "playerName" + namesOther[i];
        options[i+1].innerHTML = namesOther[i];
        options[i+1].value = namesOther[i];

        if(cardUsedGreen.get(namesOther[i]) == undefined){
            cardUsedGreen.set(namesOther[i], []);
            cardUsedRed.set(namesOther[i], []);
            cardUsedBlue.set(namesOther[i], []);
        }
    }
}

function planetTree(tab){
    let rows = document.getElementsByClassName("row");
    var w=0;
    let i = 0;

    clickableArea = [];

    for(let x=0;x<rows.length;x++){
        let divs = rows[x].getElementsByTagName('div');
        for(let y=0;y<divs.length;y++){
            if(tab[w]=="TRUE"){
                divs[y].classList.add("boardAreaClick");
                divs[y].param = divs[y].getAttribute("index");
                clickableArea[i] = divs[y].getAttribute("index");
                divs[y].addEventListener("click", planetF);
                i++;
            }
            else if(tab[w]=="FALSE"){
                divs[y].classList.add("boardAreaNotClick");
            }

            w++;
        }
    }
}

function planetF(evt){
    planetFDo(evt);
}

function planetFDo(evt){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        if(clickableArea.includes(evt.currentTarget.param)){
            if(boardClick=="tree"){
                if(treeMoney == "money"){
                    sendToServer("PUT_TREE", [evt.currentTarget.param, "money"]);
                }else if(treeMoney == "leaf"){
                    sendToServer("PUT_TREE", [evt.currentTarget.param, "leaf"]);
                }
            }else if(boardClick=="city"){
                sendToServer("PUT_CITY", [evt.currentTarget.param]);
            }else if(boardClick == "ocean"){
                sendToServer("PUT_OCEAN", [evt.currentTarget.param]);
            }
            
            planetRefresh();
        }
    }
}

function planetRefresh(){
    let rows = document.getElementsByClassName("row");

    clickableArea = [];
    boardClick = "";

    for(let x=0;x<rows.length;x++){
        let divs = rows[x].getElementsByTagName('div');
        for(let y=0;y<divs.length;y++){
            divs[y].removeEventListener("click", planetF);
            divs[y].classList.remove("boardAreaNotClick");
            divs[y].classList.remove("boardAreaClick");
        }
    }
}

function makePlanet(){
    let rows = document.getElementsByClassName("row");
    let w=0;

    for(let x=0;x<rows.length;x++){
        let divs = rows[x].getElementsByTagName('div');
        for(let y=0;y<divs.length;y++){
            divs[y].innerHTML = "";
            divs[y].setAttribute('index', w);
            let img = document.createElement("img");
            
            let span = document.createElement("span");
            span.style.position = "absolute";
            span.style.width = "100%";
            span.style.height = "100%";
            span.style.textAlign = "center";
            span.style.lineHeight = "4";
            span.style.left = "0";

            img.style.width = "63px";
            img.style.height = "73px";
            
            if(planet[w]=="NOTHING"){
                img.src = "../../assets/rect.svg";
            }
            else if(planet[w]=="NO_OCEAN"){
                img.src = "../../assets/rectWater.svg";
            }
            else if(planet[w]=="TREE_P1"){
                img.src = "../../assets/rectTreeP1.svg";
            }
            else if(planet[w]=="TREE_P2"){
                img.src = "../../assets/rectTreeP2.svg"; 
            }
            else if(planet[w]=="TREE_P3"){
                img.src = "../../assets/rectTreeP3.svg";  
            }

            span.innerHTML = w;
            
            divs[y].appendChild(img);
            divs[y].appendChild(span);
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


document.getElementById("title1").addEventListener("click", function(){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        sendToServer("TITLE",["PZ"]);
    }
});

document.getElementById("title2").addEventListener("click", function(){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        sendToServer("TITLE",["CITY"]);
    }
});

document.getElementById("title3").addEventListener("click", function(){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        sendToServer("TITLE",["FOREST"]);
    }
});

document.getElementById("title4").addEventListener("click", function(){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        sendToServer("TITLE",["CARD"]);
    }
});

document.getElementById("title5").addEventListener("click", function(){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        sendToServer("TITLE",["SYMBOLS"]);
    }
});


document.getElementById("prize1").addEventListener("click", function(){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        sendToServer("PRIZE",["PZ"]);
    }
});

document.getElementById("prize2").addEventListener("click", function(){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        sendToServer("PRIZE",["GOLD"]);
    }
});

document.getElementById("prize3").addEventListener("click", function(){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        sendToServer("PRIZE",["LEAF"]);
    }
});

document.getElementById("prize4").addEventListener("click", function(){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        sendToServer("PRIZE",["ENERGY"]);
    }
});

document.getElementById("prize5").addEventListener("click", function(){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        sendToServer("PRIZE",["HEAT"]);
    }
});


document.getElementById("ps1").addEventListener("click", function(){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        sendToServer("PS",["ENERGY"]);
    }
});

document.getElementById("ps2").addEventListener("click", function(){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        sendToServer("BOARD_WATER", []);
    }
});

document.getElementById("ps3").addEventListener("click", function(){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        sendToServer("PS",["TEMP"]);
    }
});

document.getElementById("ps4").addEventListener("click", function(){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        treeMoney = "money";
        sendToServer("BOARD_TREE", []);
    }
});

document.getElementById("ps5").addEventListener("click", function(){
    if(userStates.get(playerName) == "FIRST_MOVE" || userStates.get(playerName) == "SECOND_MOVE"){
        sendToServer("BOARD_CITY", []);
    }
});


selectPlayer.addEventListener("change", function(){
    updateCardConstainer()
});


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
        planetRefresh()
    }
});


function prize(msg){
    let index = 0;
    let colors = [
        "#15616D",
        "#6D1515",
        "#6D4515"
    ];

    for(let i=0;i<msg.owners.length;i++){
        if(msg.owners[i] == msg.about){
            index = i+1;
        }
    }

    var prizeDiv;
    var img = document.createElement("img");
    var span = document.createElement("span");

    img.src = "../../assets/rectP" + index + ".svg";
    img.style.width = "60px";
    img.style.width = "69px";

    span.classList.add("material-symbols-outlined");
    span.style.color = colors[index];
    span.style.fontSize = "30px"

    if(msg.msg[0] == "PZ"){
        prizeDiv = document.getElementById("prize1");
        span.innerHTML = "token";
    }
    else if(msg.msg[0] == "GOLD"){
        prizeDiv = document.getElementById("prize2");
        span.innerHTML = "paid";
    }
    else if(msg.msg[0] == "LEAF"){
        prizeDiv = document.getElementById("prize3");
        span.innerHTML = "eco";
    }
    else if(msg.msg[0] == "ENERGY"){
        prizeDiv = document.getElementById("prize4");
        span.innerHTML = "bolt";
    }
    else if(msg.msg[0] == "HEAT"){
        prizeDiv = document.getElementById("prize5");
        span.innerHTML = "local_fire_department";
    }

    prizeDiv.parentElement.style.cursor = "inherit";
    prizeDiv.innerHTML = "";
    prizeDiv.appendChild(img);
    prizeDiv.appendChild(span);
}



function title(msg){
    let index = 0;
    let colors = [
        "#15616D",
        "#6D1515",
        "#6D4515"
    ];

    for(let i=0;i<msg.owners.length;i++){
        if(msg.owners[i] == msg.about){
            index = i;
        }
    }

    var titleDiv;
    var img = document.createElement("img");
    var span = document.createElement("span");

    img.src = "../../assets/rectP" + index + ".svg";
    img.style.width = "60px";
    img.style.width = "69px";

    span.classList.add("material-symbols-outlined");
    span.style.color = colors[index];
    span.style.fontSize = "30px"

    if(msg.msg[0] == "PZ"){
        titleDiv = document.getElementById("title1");
        span.innerHTML = "token";
    }
    else if(msg.msg[0] == "CITY"){
        titleDiv = document.getElementById("title2");
        span.innerHTML = "fort";
    }
    else if(msg.msg[0] == "FOREST"){
        titleDiv = document.getElementById("title3");
        span.innerHTML = "forest";
    }
    else if(msg.msg[0] == "CARD"){
        titleDiv = document.getElementById("title4");
        span.innerHTML = "style";
    }
    else if(msg.msg[0] == "SYMBOLS"){
        titleDiv = document.getElementById("title5");
        span.innerHTML = "emergency";
    }

    titleDiv.parentElement.style.cursor = "inherit";
    titleDiv.innerHTML = "";
    titleDiv.appendChild(img);
    titleDiv.appendChild(span);
}

function updateCardConstainer(){
    let value = selectPlayer.value;
    let cardsContainer = document.getElementById("cardsContainer");
    cardsContainer.innerHTML = "";

    if(cardTypeSelect == "your"){
        for(let i=0;i<cardsTab.length;i++){
            let img = document.createElement("img");
            img.src = "data:image/png;base64," + cardsTab[i].image;
            img.style.cursor = "pointer";
            img.id = "card" + cardsTab[i].id;
            img.addEventListener("dblclick", function(){
                //cardsContainer.removeChild(img);
                
                sendToServer("USE_CARD", [cardsTab[i].index])
                planetRefresh()
                //cardsTab.splice(i, 1);
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

function animateCard(img){
    let anim = document.createElement("div");
    let mainPart = document.getElementById("mainPart");

    anim.innerHTML = "";

    anim.classList.add("anim");
    anim.appendChild(img);
    mainPart.appendChild(anim);

    anim.addEventListener("animationend", function(){
        mainPart.removeChild(anim);
    });
}

function updateOthers(){
    document.getElementById("roundNumber").innerHTML = "Runda: " + round;
    
    let tempDiv = document.getElementById("tempSection");
    tempDiv.innerHTML = "";

    let tempSpan = document.createElement("span");
    tempSpan.innerHTML = "Temp. " + (-30 + tempWK*2) + "&#176;";
    tempDiv.appendChild(tempSpan);

    for(let i=0;i<winTemp;i++){
        let span = document.createElement("span");
        span.innerHTML = "device_thermostat";
        span.classList.add("material-symbols-outlined");

        if(i>tempWK-1){
            span.classList.add("tempNot");
        }

        tempDiv.appendChild(span);
    }


    let oxygenDiv = document.getElementById("oxygenSection");
    oxygenDiv.innerHTML = "";

    let oxygenSpan = document.createElement("span");
    oxygenSpan.innerHTML = "Tlen " + (o2WK)+ "%";
    oxygenDiv.appendChild(oxygenSpan);

    for(let i=0;i<winO2;i++){
        let span = document.createElement("span");
        span.innerHTML = "spo2";
        span.classList.add("material-symbols-outlined");

        if(i>o2WK-1){
            span.classList.add("oxygenStatNot");
        }

        oxygenDiv.appendChild(span);
    }


    let oceanDiv = document.getElementById("oceanSection");
    oceanDiv.innerHTML = "";

    let oceanSpan = document.createElement("span");
    oceanSpan.innerHTML = "Oceania " + (oceanWK);
    oceanDiv.appendChild(oceanSpan);

    for(let i=0;i<winOcean;i++){
        let span = document.createElement("span");
        span.innerHTML = "waves";
        span.classList.add("material-symbols-outlined");

        if(i>oceanWK-1){
            span.classList.add("waterStatNot");
        }

        oceanDiv.appendChild(span);
    }
}

function updateColors(user){
    let colors = [
        "#00DCFF",
        "#ff2300",
        "#ca5800"
    ];

    let colors2 = [
        "#05414D",
        "#991100",
        "#6a2800"
    ]


    for(let i=0;i<user.length;i++){
        if(user[i] == playerName){
            document.getElementById("mainPlayer").style.color = colors[i];
            document.getElementsByClassName("topbar")[0].style.boxShadow = "0px 3px 10px " + colors[i] ;
        }else{
            let div = document.getElementById("playerName" + user[i]).parentElement;
            div.style.backgroundColor = colors2[i];
            div.parentElement.style.backgroundColor = colors[i];
        }
    }
}