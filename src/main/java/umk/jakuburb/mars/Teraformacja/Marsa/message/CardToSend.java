package umk.jakuburb.mars.Teraformacja.Marsa.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Card;

import java.io.Serializable;

public class CardToSend implements Serializable {

    @JsonProperty("index")
    private long index;
    @JsonProperty("typeCard")
    private Card.TypeCard typeCard;

    @JsonProperty("image")
    private byte[] image;

    public CardToSend() {
    }

    public CardToSend(long index, Card.TypeCard typeCard, byte[] image) {
        this.index = index;
        this.typeCard = typeCard;
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
}
