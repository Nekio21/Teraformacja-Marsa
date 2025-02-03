package umk.jakuburb.mars.Teraformacja.Marsa.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Card;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CardToSend implements Serializable {

    @JsonProperty("index")
    private long index;
    @JsonProperty("typeCard")
    private Card.TypeCard typeCard;

    @JsonProperty("price")
    private int price;

    @JsonProperty("image")
    private byte[] image;

    public CardToSend() {
    }

    public static List<CardToSend> makeCardToSend(List<Card> cards){
        List<CardToSend> list = new ArrayList();

        for(Card card: cards){
            list.add(new CardToSend(card));
        }

        return list;
    }

    public static List<List<CardToSend>> makeListCardToSend(List<List<Card>> cards){
        List<List<CardToSend>> list = new ArrayList<>();

        for(List<Card> cardList: cards){
            list.add(makeCardToSend(cardList));
        }

        return list;
    }

    public CardToSend(Card card){
        this.index = card.getId();
        this.typeCard = card.getTypeCard();
        this.price = card.getPrice();
        this.image = card.getImage();
    }

    public CardToSend(long index, Card.TypeCard typeCard, byte[] image) {
        this.index = index;
        this.typeCard = typeCard;
        this.image = image;
    }

    public CardToSend(long index, Card.TypeCard typeCard, int price, byte[] image) {
        this.index = index;
        this.typeCard = typeCard;
        this.price = price;
        this.image = image;
    }

    public Card.TypeCard getTypeCard() {
        return typeCard;
    }

    public void setTypeCard(Card.TypeCard typeCard) {
        this.typeCard = typeCard;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
