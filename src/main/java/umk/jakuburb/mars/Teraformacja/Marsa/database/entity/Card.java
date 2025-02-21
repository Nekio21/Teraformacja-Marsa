package umk.jakuburb.mars.Teraformacja.Marsa.database.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Card {

    public enum TypeCard{
        MAIN, BLUE, GREEN, RED
    }

    public enum Symbol{
        LEAF, METAL, ATOM, CITY, ACTION, EARTH, STAR, MARS, ENERGY, BEAR, BACTERIA
    }

    @Id
    @SequenceGenerator(name="sekwencja4", sequenceName = "card_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sekwencja4")
    @Column(unique = true)
    private Long id;

    private TypeCard typeCard;
    private int price;
    private byte[] image;

    @OneToMany(mappedBy = "card", fetch=FetchType.EAGER)
    private List<CardSkills> cardSkillsList;

    private List<Symbol> symbolList;

    public Card() {
    }

    public Card(Long id, TypeCard typeCard, byte[] image, List<CardSkills> cardSkillsList) {
        this.id = id;
        this.typeCard = typeCard;
        this.image = image;
        this.cardSkillsList = cardSkillsList;
    }

    public Card(Long id, TypeCard typeCard, int price, byte[] image, List<CardSkills> cardSkillsList) {
        this.id = id;
        this.typeCard = typeCard;
        this.price = price;
        this.image = image;
        this.cardSkillsList = cardSkillsList;
    }

    public Card(TypeCard typeCard, byte[] image, List<CardSkills> cardSkillsList) {
        this.typeCard = typeCard;
        this.image = image;
        this.cardSkillsList = cardSkillsList;
    }

    public Card(TypeCard typeCard,int price, byte[] image) {
        this.typeCard = typeCard;
        this.image = image;
        this.price = price;
    }

    public Card(Long id, TypeCard typeCard, int price, byte[] image, List<CardSkills> cardSkillsList, List<Symbol> symbolList) {
        this.id = id;
        this.typeCard = typeCard;
        this.price = price;
        this.image = image;
        this.cardSkillsList = cardSkillsList;
        this.symbolList = symbolList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeCard getTypeCard() {
        return typeCard;
    }

    public void setTypeCard(TypeCard typeCard) {
        this.typeCard = typeCard;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public List<CardSkills> getCardSkillsList() {
        return cardSkillsList;
    }

    public void setCardSkillsList(List<CardSkills> cardSkillsList) {
        this.cardSkillsList = cardSkillsList;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public List<Symbol> getSymbolList() {
        return symbolList;
    }

    public void setSymbolList(List<Symbol> symbolList) {
        this.symbolList = symbolList;
    }
}
