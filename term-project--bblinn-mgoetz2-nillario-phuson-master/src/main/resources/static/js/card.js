let suits = [ "D", "H", "C", "S" ];
let ranks = [ "ACE", "TWO", "THREE", "FOUR", "FIVE", "SIX",
	"SEVEN", "EIGHT", "NINE", "TEN", "JACK", "QUEEN", "KING"]
const cardWidth = 80;
const cardHeight = 100;
let opponent_cardWidth = 30; //maybe change based on totalUsers
let opponent_cardHeight = 37.5; //maybe change based on totalUsers
//RIGHT NOW, opponent is scaled down 25%, maybe can make this a constant

const space = 40;
let opponentSpace = 10;
const cardSpaceWidth = cardWidth+space;
let opponentCardSpaceWidth = opponent_cardWidth+opponentSpace;
let opponentOffset = 0; //how far from the left the opponent boards will be shown
const foundation_height = 50;
const tableau_height = 200;
const opponent_height = 520;
const opponent_foundation_height = opponent_height + (10);
const opponent_tableau_height = opponent_height + (tableau_height / 4) + 10
const centerOffset = (cardWidth+cardSpaceWidth*6)/2;
const stackOffset = 20;
let opponent_stackOffset = stackOffset / 4;
let opponent_font_size = 30;


let isDraggable = false;
let dragXOffset;
let dragYOffset;
let mouseStartX;
let mouseStartY;
let dragCard = null;
let scoreDiv = null;
let scoreText = null;
let opponentStockDiv = document.getElementById("opponentStockNum");
//global reference to the card being moved
let clickedCard = null;
let scoreDivMap = {}; //map Ids to scoreDivs
let divTextMap = {};
let oppTableauDivMap = {};

let cardDict = {};
let locationDict = {}; //a dictionary that will map a location key to a card object if it exists
let tableauLengths = [0,0,0,0,0,0,0];
let stockLengths = [23, -1];
let foundationLengths = [0,0,0,0]; //when all of these reach 13, game is over

const PILE_TYPE = {
	TABLEAU: 0,
	STOCK: 1,
	FOUNDATIONS: 2
};

const POWERUP_TYPE = {
	FREEZE: 0,
	SHUFFLE: 1,
	SOLVE: 2,
	BLOCK: 3
};

class CardDict {
	constructor() {
		this.piles = {}
	}
	remove(card) {
		delete this.piles[card.pile][card.deck][card.num]
	}
	reverseDeck(pile,deck) {
		let temp = {}
		let d = this.piles[pile][deck]
		let keys = Object.keys(d)
		let length = keys.length
		for (let i = 0; i < length; i++) {
			let card = d[keys[i]];
			card.num = length-(i+1)
			temp[card.num] = card;

			card.updateClickable();
			card.updateDraggable();
			card.updateImage()
		}
		this.piles[pile][deck] = temp
	}
	rearrangeDeck(changedDeck,deckMap,p,d) {
		let temp = {}
		for (let index in changedDeck) {
			let item = changedDeck[index]

			let id = item[1]+"_"+item[0]
			let itemCard = deckMap[id]

			let itemNum = JSON.parse(item[4]);
			itemCard.num = itemNum;
			itemCard.updateFace(JSON.parse(item[5]))
			itemCard.updateImage()
			temp[itemNum] = itemCard
		}
		this.piles[p][d] = temp

		for (let key in temp) {
			let card = temp[key];
			card.updateClickable();
			card.updateDraggable();
		}
	}
	//takes in opponent boolean (false if main player, true if it is an opponent board)
	//takes in an xLoc that is only relevant for the opponent setup location
	addAll(cardsInfo, opponent, xLoc) {
		let cards = [];

		for (let i = 0; i < cardsInfo.length; i++) {
			let card = new Card(cardsInfo[i],opponent,xLoc)
			this.add(card)
			cards.push(card)
		}

		if (!opponent) {
			for (let i = 0; i < cards.length; i++) {
				cards[i].updateDraggable();
			}
		}
	}
	getAll() {
		let cards = []
		let ps = Object.keys(this.piles)
		for (let p = 0; p < ps.length; p++) {
			let ds = Object.keys(this.piles[ps[p]])
			for (let d = 0; d < ds.length; d++) {
				let cs = Object.keys(this.piles[ps[p]][ds[d]])
				for (let c = 0; c < cs.length; c++) {
					cards.push(this.piles[ps[p]][ds[d]][cs[c]])
				}
			}
		}
		return cards
	}
	get(pile,deck,num) {
		return this.piles[pile][deck][num];
	}
	add(card) {
		if (this.piles[card.pile] === undefined) {
			this.piles[card.pile] = {}
		}
		if (this.piles[card.pile][card.deck] === undefined) {
			this.piles[card.pile][card.deck] = {}
		}
		this.piles[card.pile][card.deck][card.num] = card;
	}
	getPileLen(pile) {
		let p = this.piles[pile]
		if (p !== undefined) {
			return Object.keys(p).length;
		}
		return 0
	}
	getDeckLen(pile,deck) {
		let p = this.piles[pile]
		if (p !== undefined) {
			let d = p[deck];
			if (d !== undefined) {
				return Object.keys(d).length;
			}
		}
		return 0
	}
}

let cardsDict = new CardDict();

function getLocationkey(pile, deck, num){
	return pile + "_" + deck + "_" + num
}

class Card {
	/**
	 * opponent boolean indicates if the xLoc needs to be considered when drawing cards
	 * xLoc refers to the top left corner of the opponent
	 */
	constructor(arr, opponent, xLoc){
		this.suit = arr[0]
		this.value = arr[1]
		this.pile = parseInt(arr[2])
		this.deck = parseInt(arr[3])
		this.num = parseInt(arr[4])
		this.draggable = false;

		// Give it a box attribute which determines its width height and its location
		this.opponent = opponent;
		this.xLoc = xLoc;
		if (!opponent) {
			this.isFace = JSON.parse(arr[5]);
		} else {
			this.isFace = false;
		}

		this.createImage()
		if(!this.opponent){
			this.updateClickable()
		}
	}
	freeze() {
		let card = this

		// Change card to frozen
		this.image.src = "images/frozen_card.png"

		let temp = Object.assign(this.draggable)
		card.draggable = false
		setTimeout(function() {
			card.draggable = temp.valueOf()
			card.image.src = card.getUrl()
		}, freeze_time*1000);
	}
	updateFace(newFace) {
		if (newFace != this.isFace) {
			this.flip()
		}
	}
	flip() {
		this.isFace = !this.isFace;
		this.image.src = this.getUrl();
	}
	getLocKey() {
		return `${this.pile}_${this.deck}_${this.num}`;
	}
	updateImage() {
		this.image.style.zIndex = this.num;
		this.image.style.left = getCardXLoc(this) + "px";
		this.image.style.top = getCardYLoc(this) + "px";
		this.image.style.transform = null
	}
	update(pile,deck,num) {
		cardsDict.remove(this);
		this.pile = pile;
		this.deck = deck;
		this.num = num;
		cardsDict.add(this);

		this.updateClickable();
		this.updateDraggable();
		this.updateImage();
	}
	createImage() {
		let img = document.createElement("img")
		this.image = img;
		img.src = this.getUrl();
		img.id = this.getId();
		img.classList.add("card");

		this.updateImage()
		if(!this.opponent){
			img.style.width = cardWidth + "px";
			img.style.height = cardHeight + "px";
		}
		else{
			img.style.width = opponent_cardWidth + "px";
			img.style.height = opponent_cardHeight + "px";
		}

			createDragging(img,this)
			createClicking(img,this)

			canvasDiv.appendChild(img)
			return img
	}
	removeImage(){
		this.image.remove()
	}
	getUrl(){
		if(this.isFace){
			return "images/PNG/" + this.value + "_" + this.suit + ".png";
		}
		else{
			//show cardBack only of opponents
			return "images/cardback.jpg";
		}
	}
	getId(){
		return this.value + "_" + this.suit;
	}
	updateClickable() {
		this.clickable = this.pile === PILE_TYPE.STOCK && this.deck === 0;
	}
	updateDraggable() {
		// Foundations
		if (this.pile === PILE_TYPE.FOUNDATIONS) {
			this.draggable = true;
			return;
		// Stock
		} else if (this.pile === PILE_TYPE.STOCK) {
			let deckLen = cardsDict.getDeckLen(PILE_TYPE.STOCK,1);
			this.draggable = (this.deck === 1) && (this.num+1 === deckLen);
			return;
		}
		// Tableau
		this.draggable = this.isFace
	}
}

function createClicking(img,card) {
	img.onclick = function() {
		if (!card.clickable) {
			return;
		}

		let cardInfo = [card.pile,card.deck,card.num];
		let destInfo = [PILE_TYPE.STOCK,1];

		check_move(cardInfo, destInfo);
	}
}

function createDragging(img,card) {
    img.addEventListener("mousedown", dragStart, false);
    img.addEventListener("mouseup", dragEnd, false);
    img.addEventListener("mousemove", drag, false);

    let active = false;
    let currentX;
    let currentY;
    let initialX;
    let initialY;
    let xOffset = 0;
    let yOffset = 0;

    function dragStart(e) {
    	// Change z-indexes
		let pile = card.pile;
		let deck = card.deck;
		let cardNum = card.num;

		let moveCard = card;
		let add = 0;
		while (moveCard !== undefined) {
			moveCard.image.style.zIndex = 52+add;
			cardNum += 1; add += 1;
			moveCard = cardsDict.get(pile,deck,cardNum)
		}

    	if (!card.draggable || active) {
    		return;
    	}
		if (e.type === "touchstart") {
			initialX = e.touches[0].clientX - xOffset;
			initialY = e.touches[0].clientY - yOffset;
		} else {
			initialX = e.clientX - xOffset;
			initialY = e.clientY - yOffset;
		}
		if (e.target === img) {
			active = true;
		}
    }

    function dragEnd(e) {
    	if (!card.draggable) {
    		return;
    	}
		active = false;

		// Change z-indexes back
		let pile = card.pile;
		let deck = card.deck;
		let cardNum = card.num;

		let moveCard = card;
		let add = 0;
		while (moveCard !== undefined) {
			moveCard.image.style.zIndex = moveCard.num;
			moveCard = cardsDict.get(pile,deck,cardNum)
			cardNum += 1; add += 1;
		}

		currentX = null;
	    currentY = null;
	    initialX = null;
	    initialY = null;
	    xOffset = 0;
	    yOffset = 0;

		reposition(e.pageX,e.pageY,card)
    }

    function drag(e) {
    	if (!card.draggable) {
    		return;
    	}
		if (active) {
			e.preventDefault();

			if (e.type === "touchmove") {
			  	currentX = e.touches[0].clientX - initialX;
			 	currentY = e.touches[0].clientY - initialY;
			} else {
			 	currentX = e.clientX - initialX;
			 	currentY = e.clientY - initialY;
			}

			xOffset = currentX;
			yOffset = currentY;

			// Translate all cards
			let pile = card.pile;
			let deck = card.deck;
			let cardNum = card.num;

			let moveCard = card;
			while (moveCard !== undefined) {
				setTranslate(currentX, currentY, moveCard.image);
				cardNum += 1;
				moveCard = cardsDict.get(pile,deck,cardNum)
			}
		}
    }
}

function setTranslate(xPos, yPos, el) {
    el.style.transform = "translate3d(" + xPos + "px, " + yPos + "px, 0)";
}

function withinBoxes(x,y) {
	let lowX; let highX;
	for (let i = 0; i < 7; i++) {
		// Check in foundation
		let withinFoundation = y >= foundation_height && y <= foundation_height+cardHeight;
		withinFoundation = withinFoundation && i !== 2;

		// Check in tableau
		let tableauDis = cardHeight+space*cardsDict.getDeckLen(PILE_TYPE.TABLEAU,i)
		let withinTableau = y >= tableau_height && y <= tableau_height+tableauDis;

		// Check in x range
		lowX = CANVAS_CENTER - centerOffset + i*cardSpaceWidth;
		highX = lowX + cardWidth;
		let withinX = x >= lowX && x <= highX;

		if (withinFoundation||withinTableau) {
			if (withinX) {
				return [true,i,withinTableau]
			}
		}
	}
	return [false];
}

function reposition(oldX,oldY,card) {
	let x = oldX - $(canvasDiv).offset().left;
	let y = oldY - $(canvasDiv).offset().top;

	let within = withinBoxes(x,y);
	if (!within[0]) {
		// Not within, put it back
		move_back(card)
		return;
	}

	// [true, deckNum, withinTableau]
	let tableau = within[2];
	let pileType = PILE_TYPE.TABLEAU;
	let num = within[1];

	if (!tableau) {
		if (within[1] < 2) {
			pileType = PILE_TYPE.STOCK
		} else {
			pileType = PILE_TYPE.FOUNDATIONS
			num -= 3
		}
	}

	let cardInfo = [card.pile,card.deck,card.num];
	let destInfo = [pileType,num];

	if (card.pile === pileType && card.deck === destInfo) {
		move_back(card);
		return
	}
	check_move(cardInfo,destInfo)
}

function move_cards(cardInfo, destInfo){
	let pile = cardInfo[0];
	let deck = cardInfo[1];
	let num = cardInfo[2];

	let card = cardsDict.get(pile,deck,num);
	if (destInfo[0] === PILE_TYPE.STOCK && destInfo[1] === 1) {
		card.flip();
	}

	let newNum = cardsDict.getDeckLen(destInfo[0],destInfo[1]);
	let cardNum = num;
	// This is here because there is a weird case where this can
  // run infinitely and crash the browser.
 	while (card !== undefined) {
		// If stock is being refreshed flip
		card.update(destInfo[0],destInfo[1],newNum);
		if (destInfo[0] === PILE_TYPE.STOCK && destInfo[1] === 0) {
			card.flip()
		}
		newNum += 1; cardNum += 1;
		card = cardsDict.get(pile,deck,cardNum)
	}

	// If stock is being refreshed reverse the order
	if (destInfo[0] === PILE_TYPE.STOCK && destInfo[1] === 0) {
		cardsDict.reverseDeck(PILE_TYPE.STOCK,0)
	}

	// Check Flip
	if (pile === PILE_TYPE.TABLEAU && num > 0) {
		let below = cardsDict.get(pile,deck,num-1);
		if (!below.isFace) {
			below.flip();
			below.draggable = true;
		}
	}
}

function move_back(card) {
	let pile = card.pile;
	let deck = card.deck;
	let num = card.num

	let cardNum = num;
	while (card !== undefined) {
		card.updateImage();
		cardNum += 1;
		card = cardsDict.get(pile,deck,cardNum)
	}
}

function getOpponentTableauXLoc(tableauNum, xLoc){
		return opponentOffset + xLoc + (tableauNum+3)*opponentCardSpaceWidth;
}

/**
 * Gets the card's pixel X location
 * @param card
 * @returns
 */
function getCardXLoc(card){
	let pile = card.pile
	let deck = card.deck

	let baseX = CANVAS_CENTER - centerOffset;
	if(!card.opponent){
		if(pile === PILE_TYPE.STOCK || pile === PILE_TYPE.TABLEAU){
			return baseX + deck*cardSpaceWidth;
		}
		else{
			//foundation
			return baseX + (deck+3)*cardSpaceWidth;
		}
	}
	else{
		if(pile === PILE_TYPE.STOCK || pile === PILE_TYPE.TABLEAU){
			return opponentOffset + card.xLoc + deck*opponentCardSpaceWidth;
		}
		else{
			//foundation
			return opponentOffset + card.xLoc + (deck+3)*opponentCardSpaceWidth;
		}
	}
}

/**
 * Gets a card's pixel y location
 * @param card
 * @returns
 */
function getCardYLoc(card){
	const cardPile = card.pile
	if(!card.opponent){
		if(cardPile === PILE_TYPE.TABLEAU){
			return tableau_height + card.num * stackOffset;
		}
		else {
			return foundation_height;
		}
	}
	else{
		if(cardPile === PILE_TYPE.TABLEAU){
			return opponent_tableau_height + card.num * opponent_stackOffset;
		}
		else {

			return opponent_foundation_height;
		}
	}
}

/**
* A helper method to update opponent scores underneath main board
*/
function updateOpponentScore(board, currScore, xLoc){

	 if(board in scoreDivMap){
		let currDivText = scoreDivMap[board]
		let currDiv = currDivText.div
		let currText = currDivText.scoreText
		currDiv.style.left = xLoc + 'px';

		currText.nodeValue =  'Opponent ID: ' + board + ' | Score: ' + currScore
	 }
	 else{
		 let scoreDiv = document.createElement("div");

		 scoreDiv.style.left = xLoc + 'px';
	 	 scoreDiv.style.top = '0px';
	 	 scoreDiv.style.zIndex = 1000
	 	 scoreDiv.style.position = "absolute"
	 	 let scoreText = document.createTextNode('Opponent ID: ' + board + '   |   Score: ' + currScore);
		 opponentTextDiv.appendChild(scoreDiv)
		 scoreDiv.appendChild(scoreText);

		 scoreDivMap[board] = new scoreDivText(scoreDiv, scoreText);

	 }
}

/**
* A class that contains score divs and their corresponding text
*/
class scoreDivText {
	constructor(div, scoreText){
		this.div = div
		this.scoreText = scoreText
	}
}
