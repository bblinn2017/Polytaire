const REQ_TYPE = {
	CONNECT: 0,
	SERVER: 1,
	CLIENT: 2,
	LINK: 3,
	JOIN: 4
};

const INFO_TYPE = {
	NEW: 0,
	MOVE: 1,
	OPP: 2,
	POW: 3,
	AI: 4,
	GAME_OVER: 5
};

let conn;
let myId = -1;
let myGroupId = -1;
let totalUsers = 1;
let opponentTextDiv = document.getElementById("opponentText");
//index for the current opponent X loc
let currXLocIndex = 0;
let allOpponentDicts = {}; //maps opponent IDs to opponent cardDicts

// Setup the WebSocket connection
function setup_live_solitaire() {
  let socketlocation = location.hostname.replace(/https?:\/\//i, "");

  let localPort = 4567;
  if(socketlocation.includes("heroku")) {
    conn = new WebSocket("wss://" + socketlocation + "/update");
  } else {
    conn = new WebSocket("ws://" + socketlocation + ":" + localPort + "/update");
  }


	conn.onerror = err => {
		console.log("Connection error", err);
	}

	conn.onmessage = msg => {
		const data = JSON.parse(msg.data)
		switch (data.reqType) {
			default:
				console.log("Unknown request type!", data.reqType);
				return;
				break;
			case REQ_TYPE.CONNECT:
				// Assign Id
				myId = JSON.parse(data.id)
				break;
			case REQ_TYPE.SERVER:
				// UPDATE DATA

				let payload = JSON.parse(data.payload)
				let inId = data.id;
				let payload2 = JSON.parse(data.oppPayload)
				parse_payloads(payload,payload2,inId)
				break;
			case REQ_TYPE.JOIN:
				receive_host_join(data)
				break;
		}
	}
};

function parse_payloads(payload1,payload2,id) {
	// Powerup
	let usePayload = payload1.infoType === INFO_TYPE.POW;
	let inType = payload1.infoType;
	usePayload = usePayload || inType === INFO_TYPE.NEW;
	usePayload = usePayload && inType !== INFO_TYPE.AI;
	usePayload = usePayload || inType === INFO_TYPE.GAME_OVER;

	if (usePayload || id === myId) {
		update(payload1,id)
	}

	// Always gets opponent payload
	update_opponent(payload2)
}

function update_opponent(payload) {
	switch (payload.infoType) {
		default:
			console.log("Unkown information type", payload.infoType)
		case INFO_TYPE.OPP:
			let allBoards = JSON.parse(payload.boards)
			let allScores = JSON.parse(payload.scores)
			totalUsers = Object.keys(allBoards).length
			updateSizes()
			drawOpponentLabel()
			let currXLocIndex = 0
			for(let board in allBoards){
				//only draw opponent board if this board setup is not main
				let oppDict;
				if(myId != board){
						if(board in allOpponentDicts){
								oppDict = allOpponentDicts[board] //old dictionary
								let cardsArray = oppDict.getAll()
								for(let i = 0; i < cardsArray.length; i++){
									cardsArray[i].removeImage();
								}
						 }
				 else {
						oppDict = new CardDict()
						allOpponentDicts[board] = oppDict;
				}
				oppDict.addAll(allBoards[board], true, opponentXLocs[currXLocIndex])
				updateOpponentScore(board, allScores[board], opponentXLocs[currXLocIndex])
				currXLocIndex++
			}
			}

			break;
	}
}

function handleGameOver(payload) {
	$(document.body).empty()
	clearInterval(scoreInterval)
	$.get("gameOver", function(response) {
		document.body.innerHTML = response

        scores = JSON.parse(payload.scores);
		leaderboard = document.getElementById("leaderboard");
		for(let id in scores){
	    	let leaderboard_entry = document.createElement("LI");
	    	leaderboard_entry.appendChild(document.createTextNode(
	        	`Player ${id}: ${scores[id]} Points`));
	    	leaderboard.appendChild(leaderboard_entry);
		}

    })
}

function display_new_game(payload) {
	$(canvasDiv).empty()
	align_images()
	let slotDiv = document.createElement("div")
	slotDiv.id = "slots"
	canvasDiv.appendChild(slotDiv);
	if (!setup) {
		drawStartingSetup();
		setup = true;
	}

		// Start timer
		scoreInterval = setInterval(function() {
			let timeElem = document.getElementById("time");
			let newTime = parseInt(timeElem.getAttribute("value"))+1
			changeField(timeElem,newTime)
			updatePowerupVisibility()
		}, 1000)

	//Add cards to dict
	cards = JSON.parse(payload.boardInfo)
	//3rd argument doesn't matter bc not an opponent
	cardsDict.addAll(cards, false, 0);
}

function update(payload,id) {
	switch (payload.infoType) {
		default:
			console.log("Unknown information type", payload.infoType);
			break;
		case INFO_TYPE.NEW:
			init = true;
			load_content("/play",display_new_game(payload));
			break;
		case INFO_TYPE.MOVE:
			success = payload.success;

			let cardInfo = JSON.parse(payload.cardInfo)
			let destInfo = JSON.parse(payload.destInfo)
			if (success) {
				// Update game
				move_cards(cardInfo, destInfo)
				score = JSON.parse(payload.score);
				scoreElem = document.getElementById("score")
				changeField(scoreElem,score)
			}
			else{
				// Move cards back
				move_back(cardsDict.get(cardInfo[0],cardInfo[1],cardInfo[2]))
			}
			break;
		case INFO_TYPE.POW:
			updatePowerups(payload,id);
			break;
		case INFO_TYPE.GAME_OVER:
			handleGameOver(payload);
			break;

	}
}

function start_new_game() {
	payload = {"infoType":INFO_TYPE.NEW}
	params = {"reqType":REQ_TYPE.CLIENT,"payload":payload,"id":myId,"groupId":myGroupId}
	conn.send(JSON.stringify(params))
}

function powerup_effect(type,messageType) {
	payload = {"infoType":INFO_TYPE.POW,"powType":type,"messageType":messageType}
	params = {"reqType":REQ_TYPE.CLIENT,"payload":payload,"id":myId,"groupId":myGroupId}
	conn.send(JSON.stringify(params))
}

/**
 *
 * @param cardInfo array of pileType, deckNum, cardNum
 * @param destInfo array of pileType, deckNum
 * @returns
 */
function check_move(cardInfo, destInfo){
	payload = {"infoType": INFO_TYPE.MOVE, "cardInfo": cardInfo, "destInfo": destInfo}
	params = {"reqType":REQ_TYPE.CLIENT,"payload":payload,"id":myId,"groupId":myGroupId}
	conn.send(JSON.stringify(params))
}

function drawOpponentLabel(){
	opponentTextDiv.className = "OpponentText show";

}

/**
* Updates card sizes
*/
function updateSizes(){
	let frac = 1 / (totalUsers - 1)
	if(totalUsers === 2){
		frac = 1 / 2
	}

	opponent_cardWidth = cardWidth * frac;
	opponent_cardHeight = cardHeight * frac;
	opponentSpace = space * frac;
	opponentCardSpaceWidth = opponent_cardWidth+opponentSpace
	opponent_stackOffset = stackOffset * frac
}
