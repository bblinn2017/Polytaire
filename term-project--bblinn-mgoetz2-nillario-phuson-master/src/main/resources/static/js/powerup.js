let myPowerups = new Set()

let instantiated = false;
let blocked = false;
let blockable = false;

const faded = "0.4"
const pow_space = 20;
const pow_size = 40;
const pow_offset = -foundation_height/2
const freeze_time = 10
const block_time = 5

const MESSAGE_TYPE = {
	ADD: 0,
	USE: 1,
	INIT: 2
}

const POW_TYPE = {
	FREEZE: 0,
	SHUFFLE: 1,
	SOLVE: 2,
	BLOCK: 3,
	DOUBLESCORE: 4
}

const TARGET = {
	OTHER: 0,
	SELF: 1
}

const targetDict = {
	0: 0,
	1: 0,
	2: 1,
	3: 1,
	4: 1
}

function updatePowerupVisibility() {
	myPowerups.forEach(function(e) {
		let image = document.getElementById("powType"+e)
		if (instantiated) {
			image.style.opacity = "1"
		}
	})
}

function get_deck_map(p,d) {
	let len = cardsDict.getDeckLen(p,d)
	let deck = {}
	for (let i = 0; i < len; i++) {
		let card = cardsDict.get(p,d,i)
		deck[card.getId()] = card
	}
	return deck
}

function solve(info) {
	let p = JSON.parse(info.pileType);
	let d = JSON.parse(info.deckNum);
	let cardInfo = JSON.parse(info.movedCard)
	let changedDeck = JSON.parse(info.newDeck)
	let deckMap = get_deck_map(p,d)

	// Move the card
	let moveCard = deckMap[cardInfo[1]+"_"+cardInfo[0]]
	let newPile = JSON.parse(cardInfo[2]);
	let newDeck = JSON.parse(cardInfo[3]);
	let newNum = JSON.parse(cardInfo[4]);
	moveCard.update(newPile,newDeck,newNum)
	moveCard.updateFace(JSON.parse(cardInfo[5]))

	// Rearrage the deck
	cardsDict.rearrangeDeck(changedDeck,deckMap,p,d)
}

function shuffle(map) {
	let p = PILE_TYPE.TABLEAU
	let d = JSON.parse(map.deckNum)
	let changedDeck = JSON.parse(map.newDeck)
	let deckMap = get_deck_map(p,d)

	cardsDict.rearrangeDeck(changedDeck,deckMap,p,d)
}

function freeze(num) {
	// Freeze deck at num
	let deck = cardsDict.piles[PILE_TYPE.TABLEAU][num];
	for (let num in deck) {
		let card = deck[num];
		card.freeze()
	}
}

function animate_double_score() {
	let nyan = document.createElement("img");
	nyan.setAttribute("src","images/nyanCat.gif");
	nyan.setAttribute("id","doublescore");
	nyan.style.height = pow_size+"px";

	let time = document.getElementById("time");
	let top = document.getElementById("top");

	top.insertBefore(nyan, time);
	setTimeout(function() {
		nyan.className += "fade";
		setTimeout(function(){
			nyan.remove();
		}, 4000)
	}, 6000);
}

function self_powerup(type,object) {
	switch (type) {
		case POW_TYPE.SOLVE:
			solve(object);
			break;
		case POW_TYPE.BLOCK:
			// Do nothing
			break;
		case POW_TYPE.DOUBLESCORE:
			animate_double_score();
			break;
		default:
			break;
	}
}

function warn_player(type) {
	// Give a chance to block only if player has block powerup
	if (myPowerups.has(POW_TYPE.BLOCK)) {
		let blockDiv = document.createElement("div")
		blockDiv.classList.add("incoming")
		blockDiv.style.top = "-230px"
		blockDiv.style.right = "30px"
		blockDiv.style.color = "black"
		blockDiv.style.backgroundColor = "FireBrick"
		blockDiv.style.fontWeight = "bold"
		blockDiv.style.animation = "blinker 1s linear infinite"
		blockDiv.append("INCOMING POWERUP")
		canvasDiv.append(blockDiv)

		// Remove ability to block after some time
		b_image = document.getElementById("powType"+POW_TYPE.BLOCK)
		blockable = true;
		setTimeout(function() {
			blockable = false;
			if (blocked) {
				blocked = false;
			} else {
				powerup_effect(type,MESSAGE_TYPE.USE)
			}
			canvasDiv.removeChild(blockDiv)
		}, block_time*1000);
	} else {
		powerup_effect(type,MESSAGE_TYPE.USE)
	}
}

function against_powerup(type,object) {
	switch (type) {
		case POW_TYPE.FREEZE:
			freeze(object);
			break;
		case POW_TYPE.SHUFFLE:
			shuffle(object);
			break;
		default:
			break;
	}
}

function updatePowerups(payload,id) {
	let mType = payload.messageType;
	let pType = payload.powType;

	if (mType === MESSAGE_TYPE.ADD && id === myId) {
		myPowerups.add(pType)

		updatePowerupVisibility()
	} else if (mType == MESSAGE_TYPE.USE) {
		let object = JSON.parse(payload.object);
		if (targetDict[pType] === TARGET.SELF && id === myId) {
			self_powerup(pType,object);
		} else {
			// Other player's powerup used for themself
		}
		if (targetDict[pType] === TARGET.OTHER && id === myId) {
			against_powerup(pType,object);
		} else {
			// Used powerup to attack other players
		}
	} else if (id !== myId) {
		// Warning type
		warn_player(pType)
	}
}

function pow_click(type,image) {
	if (myPowerups.has(type)) {
		myPowerups.delete(type)
		powerup_effect(type,MESSAGE_TYPE.INIT);
		image.style.opacity = faded
	}
}

function set_images() {
	let images = document.getElementsByClassName("powerup");

	let start = (CANVAS_WIDTH - pow_size*images.length - pow_space*(images.length-1))/2
	for (let i = 0; i < images.length; i++) {
		let image = images[i]
		let x = start + (pow_space+pow_size)*i
		image.style.top = pow_offset+"px"
		image.style.left = x+"px";
	}
}

function align_images() {
	let images = ["ice.png","shuffle.png","solve.png","stop.png","double.png"]

	let start = (CANVAS_WIDTH - pow_size*images.length - pow_space*(images.length-1))/2


	function powerupHotkeys(e){
		let key = e.code;
		if (key == "KeyF"){
			let freezeimage = document.getElementById("powType0");
			pow_click(POW_TYPE.FREEZE, freezeimage);
		}
		if (key == "KeyR"){
			let shuffleimage = document.getElementById("powType1");
			pow_click(POW_TYPE.SHUFFLE, shuffleimage);
		}
		if (key == "KeyS"){
			let solveimage = document.getElementById("powType2");
			pow_click(POW_TYPE.SOLVE, solveimage);
		}
		if (key == "KeyB"){
			let blockimage = document.getElementById("powType3");
			if (blockable) {
				blocked = true;
				myPowerups.delete(POW_TYPE.BLOCK)
				blockimage.style.opacity = faded
			}
		}
		if (key == "KeyD"){
			let doubleimage = document.getElementById("powType4");
			pow_click(POW_TYPE.DOUBLESCORE, doubleimage);
		}
	}

	document.addEventListener('keypress', powerupHotkeys);

	for (let i = 0; i < images.length; i++) {
		let image = document.createElement("img")
		image.setAttribute("id","powType"+i)
		image.setAttribute("src","images/"+images[i])

		if (i !== POW_TYPE.BLOCK) {
			image.onclick = function() {pow_click(i,image)}
		} else {
			// Give ability to block
			image.onclick = function() {
				if (blockable) {
					blocked = true;
					myPowerups.delete(POW_TYPE.BLOCK)
					image.style.opacity = faded
				}
			}
		}
		image.classList.add("powerup")


		image.style.opacity = faded
		image.style.width = pow_size+"px"
		image.style.height = pow_size+"px"
		canvasDiv.appendChild(image)
	}
	set_images()
	instantiated = true;
}
