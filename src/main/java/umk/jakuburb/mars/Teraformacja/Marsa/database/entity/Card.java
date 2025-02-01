package umk.jakuburb.mars.Teraformacja.Marsa.database.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Card {

    public enum TypeCard{
        MAIN, BLUE, GREEN, RED
    }

    @Id
    @SequenceGenerator(name="sekwencja4", sequenceName = "card_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sekwencja4")
    @Column(unique = true)
    private Long id;

    private TypeCard typeCard;
    //@Column(name = "image", columnDefinition="org.hibernate.type.BinaryType")
    private byte[] image;

    @OneToMany(mappedBy = "card", fetch=FetchType.EAGER)
    private List<CardSkills> cardSkillsList;

    public Card() {
    }

    public Card(Long id, TypeCard typeCard, byte[] image, List<CardSkills> cardSkillsList) {
        this.id = id;
        this.typeCard = typeCard;
        this.image = image;
        this.cardSkillsList = cardSkillsList;
    }

    public Card(TypeCard typeCard, byte[] image, List<CardSkills> cardSkillsList) {
        this.typeCard = typeCard;
        this.image = image;
        this.cardSkillsList = cardSkillsList;
    }

    public Card(TypeCard typeCard, byte[] image) {
        this.typeCard = typeCard;
        this.image = image;
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
}
